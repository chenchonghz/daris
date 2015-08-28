package nig.mf.plugin.pssd.services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.Exec;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.UrlType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlStreamWriter;
import arc.xml.XmlStringWriter;
import arc.xml.XmlWriter;

/*
 * The production process is
 * 1. Call this service with domain specific helper and do not transform XML. Do write out the untransformed
 *     XML to the $MFLUX/plugin/bin/daris2rdr/scratch/darisExport_raw.xml file.
 *    aterm> om.pssd.project.metadata.harvest :transform 0 :service nig.pssd.project.metadata.harvest :transfer 1
 * 2. Call the pipeline in $MFLUX/plugin/bin/daris2rdr/local2rdr.sh  which transforms and transfers via scp (public
 *     key must be generated and sent to recipient)
 * 3. Do this with a regular scheduled job
 *     
 */
public class SvcProjectMetaDataHarvest extends PluginService {

    private static final String TRANSFER_FILE = "/daris2rdr/scratch/darisExport_raw.xml";
    private static final String TRANSFER_CMD = "./daris2rdr/local2rdr.sh";

    private Interface _defn;

    public SvcProjectMetaDataHarvest() {
        _defn = new Interface();
        _defn.add(new Interface.Element(
                "id",
                CiteableIdType.DEFAULT,
                "The citeable asset id of the local Project to harvest. Default means all.",
                0, 1));
        _defn.add(new Interface.Element(
                "service",
                StringType.DEFAULT,
                "An additional research domain-specific service to run that will populate more of the harvested meta-data. It should take one argument 'id' (project citable id) and produce an XML output with parent element 'meta'.  The structure must conform to what the XSLT is looking for.  See nig.pssd.project.metadata.harvest as an example",
                0, 1));
        _defn.add(new Interface.Element("transform", BooleanType.DEFAULT,
                "Transform to RIFCS (default true).", 0, 1));
        _defn.add(new Interface.Element(
                "transfer",
                BooleanType.DEFAULT,
                "Transfer to destination (default false).  Writes out the raw XML to the plugin/bin/daris2rdr/scratch directory, transforms and transfers to destination host.",
                0, 1));
        _defn.add(new Interface.Element(
                "originating-source",
                UrlType.DEFAULT,
                "The originating source for any meta-data harvested from this repository; over-rides element value supplied by daris.repository.describe",
                0, 1));
        _defn.add(new Interface.Element(
                "over-ride",
                BooleanType.DEFAULT,
                "Harvest the meta-data for this project regarldess of the directive in daris:pssd-project-harvest. Only for use in testing; no operational process should set this.  Defaults to false.",
                0, 1));
    }

    public String name() {

        return "om.pssd.project.metadata.harvest";
    }

    public String description() {

        return "Harvest repository meta-data for ANDS registry. The Mediaflux XML is returned to the terminal.  Specify the transformed and output (RIF-CS) file with :out";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_MODIFY;
    }

    @Override
    public int maxNumberOfOutputs() {

        return 1;
    }

    @Override
    public int minNumberOfOutputs() {

        return 0;
    }

    public void execute(XmlDoc.Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {

        // Parse arguments
        boolean transform = args.booleanValue("transform", true);
        String projId = args.stringValue("id");
        String domainService = args.stringValue("service");
        boolean transfer = args.booleanValue("transfer", false);
        XmlDoc.Element origSrc = args.element("originating-source");
        boolean overRide = args.booleanValue("over-ride", false);

        // Harvest meta-data
        XmlDocMaker docOut = new XmlDocMaker("repository-harvest");
        fetchMetaData(executor(), projId, domainService, origSrc, overRide,
                docOut);

        // Write RAW XML to destination for transformation and transfer
        if (transfer) {
            transfer(docOut);
        }

        // If not transforming, tidy up
        if (!transform) {
            // Fill result with untransformed meta-data
            w.add(docOut.root());

            // Fill the output with the untransformed XML
            if (outputs != null) {
                PluginService.Output o = outputs.output(0);
                String s = docOut.root().toString();
                byte[] xmla = s.getBytes("UTF-8");
                InputStream is = new ByteArrayInputStream(xmla);
                o.setData(is, xmla.length, "text/xml");
            }
            return;
        }

        // Transform meta-data to the RIFCS schema and write to a temporary file
        File f = transform(executor(), outputs, docOut);

        // Fill writer from the temporary output file (I'm sure I could do all
        // this file/stream stuff better)
        fillResult(w, f);

        // If output given, write result out to local file stream (client side)
        // as specified on command line
        // E.g. nig.pssd.project.metadata-harvest :out
        // file:/Users/nebk/rifcs.xsl
        if (outputs != null) {
            PluginService.Output o = outputs.output(0);
            o.setData(PluginService.deleteOnCloseInputStream(f), f.length(),
                    "text/xml");
        }

    }

    private void transfer(XmlDocMaker docOut) throws Throwable {
        // Get XML into Input STream
        String s = docOut.root().toString();
        byte[] xmla = s.getBytes("UTF-8");
        InputStream is = new ByteArrayInputStream(xmla);

        // Get hold of the plugin directory
        File pluginDir = Exec.pluginDirectory();
        String name = pluginDir.getAbsolutePath() + TRANSFER_FILE;
        File f = new File(name);
        OutputStream os = new FileOutputStream(f);
        byte buf[] = new byte[1024];
        int len;

        // Write file
        while ((len = is.read(buf)) > 0)
            os.write(buf, 0, len);

        // Cleanup
        os.close();
        is.close();

        // Now execute the pipeline to transform and send to collection point
        // Let it fail if it's going to
        String cmd = TRANSFER_CMD;
        Exec.exec(cmd, null);
    }

    private void fetchMetaData(ServiceExecutor executor, String projId,
            String domainService, XmlDoc.Element origSrc, Boolean overRide,
            XmlDocMaker docOut) throws Throwable {

        // Get repository description and add to output. This bit is domain
        // independent as the meta-data is set
        // via the pssd package
        XmlDoc.Element re = executor.execute(
                SvcRepositoryDescriptionGet.SERVICE_NAME).element("repository");
        // Over-write/add originating-source if desired
        if (re != null) {
            if (origSrc != null) {
                XmlDoc.Element origSrcOld = re.element("originating-source");
                if (origSrcOld == null) {
                    re.add(origSrc);
                } else {
                    // This assignment (or setting the value) should update
                    // 'repos' by
                    // reference but it doesn't. So remove and add
                    // origSrcOld = origSrc;
                    re.removeInstance(origSrcOld);
                    re.add(origSrc);
                }
                XmlDoc.Element startDateElement = re
                        .element("data-holdings/start-date");
                if (startDateElement != null) {
                    // convert to ANDS date format
                    Date startDate = startDateElement.dateValue();
                    startDateElement.setValue(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
                }
            }
            //
            docOut.add(re);
        }

        // Specify projects
        Collection<String> ids = null;
        if (projId != null) {
            ids = new ArrayList<String>();
            ids.add(projId);
        } else {
            // Find them all
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("where", "daris:pssd-project-harvest has value");
            dm.add("action", "get-cid");
            dm.add("pdist", 0); // Local only
            XmlDoc.Element r2 = executor.execute("asset.query", dm.root());
            ids = r2.values("cid");
        }
        if (ids == null)
            return;

        docOut.push("projects");
        for (String id : ids) {

            // Fetch research domain independent harvest meta-data for this
            // Project
            XmlDocMaker meta = harvest(executor, overRide, id);

            if (meta != null) {

                // Now add some generic counters (no. subjects, studies,
                // datasets)
                // if we found something above. Nothing domain dependent here
                addCounters(executor, id, meta);

                // Now add on the additional research domain-specific meta-data
                if (domainService != null) {
                    XmlDocMaker dm2 = new XmlDocMaker("args");
                    dm2.add("id", id);
                    XmlDoc.Element r2 = executor.execute(domainService,
                            dm2.root());
                    if (r2 != null) {
                        XmlDoc.Element r3 = r2.element("meta");
                        if (r3 != null)
                            meta.addAll(r3.elements());
                    }
                }
                //
                docOut.add(meta.root());
            }
        }
        docOut.pop();
    }

    private File transform(ServiceExecutor executor, Outputs outputs,
            XmlDocMaker docOut) throws Throwable {

        // Now proceed to the XSLT transformation step.
        // Get the XML into a StreamSource .
        StreamSource xmlIn = packXML(docOut);

        // Get hold of the XSLT stored in an asset
        InputStream xsltStream = getXSLT(executor);
        StreamSource xslt = new StreamSource(xsltStream);

        // Make a Transformer. We need the saxon implementation, so the saxon
        // jar must be in the build path
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer(xslt);

        // Transform to temporary file
        File f = PluginService.createTemporaryFile();
        FileOutputStream fos = new FileOutputStream(f);
        transformer.transform(xmlIn, new StreamResult(fos));
        fos.close();
        return f;
    }

    /**
     * Read the output XML via the file it is stored in and fill the writer
     * 
     * @param w
     * @param f
     * @throws Throwable
     */
    private void fillResult(XmlWriter w, File f) throws Throwable {

        XmlDoc.Element e = new XmlDoc().parse(new java.io.FileReader(f));
        e.removeExtraWhiteSpace();
        w.add(e);
    }

    /**
     * Convert input XML into StreamSOurce
     */
    private StreamSource packXML(XmlDocMaker docOut) throws Throwable {

        // Association an XmlStreamWriter with a ByteStream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XmlStreamWriter xsw = new XmlStreamWriter(bos, XmlStreamWriter.UTF_8);

        // Fill the Writer thus populating the ByteStream
        xsw.add(docOut.root());
        xsw.flush();
        xsw.close();

        // Now wrap the ByteStream into a StreamSource by getting hold
        // of its raw byte array. SO many hoops
        return new StreamSource(new ByteArrayInputStream(bos.toByteArray()));
    }

    /**
     * Get the XSLT from its asset
     * 
     * @param executor
     * @return
     * @throws Throwable
     */
    private InputStream getXSLT(ServiceExecutor executor) throws Throwable {

        // Find the XSLT asset (installed by pssd package)
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("profile", "PSSD-TO-RIF-CS");
        XmlDoc.Element r = executor.execute(
                "asset.meta.transform.profile.describe", dm.root());
        if (r == null)
            return null;
        String id = r.value("profile/@assetId");
        if (id == null) {
            throw new Exception("Could not locate PSSD-TO-RIF-CS XSLT asset");
        }

        // Get the content
        InputStream is = AssetUtil.getContentInStream(executor, id);
        return is;
    }

    // Fetch the meta-data for ANDS utilising meta-data defined by the nig-pssd
    // package
    private XmlDocMaker harvest(ServiceExecutor executor, Boolean overRide,
            String id) throws Throwable {

        XmlDocMaker docOut = null;

        // Get meta-data attached to project
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);

        XmlDoc.Element meta = executor.execute("om.pssd.object.describe",
                dm.root());
        if (meta != null) {

            // See if project has the authorisation meta-data. If not present,
            // opt out
            XmlDoc.Element auth = meta
                    .element("object/meta/daris:pssd-project-harvest");

            // Get some other documents
            XmlDoc.Element pubs = meta
                    .element("object/meta/daris:pssd-publications");
            XmlDoc.Element services = meta
                    .element("object/meta/daris:pssd-related-services");
            XmlDoc.Element governance = meta
                    .element("object/meta/daris:pssd-project-governance");
            XmlDoc.Element category = meta
                    .element("object/meta/daris:pssd-project-research-category");
            //
            if (auth != null || overRide) {
                Boolean allowInstitutional = null;
                Boolean allowANDS = null;
                if (overRide) {
                    allowInstitutional = true;
                    allowANDS = false;
                } else {
                    allowInstitutional = auth
                            .booleanValue("allow-institutional");
                    allowANDS = auth.booleanValue("allow-ANDS");
                }

                // Populated if authorised
                if (allowInstitutional || overRide) {

                    docOut = new XmlDocMaker("project");
                    docOut.add("id", id);
                    docOut.add("name", meta.value("object/name"));
                    docOut.add("description", meta.value("object/description"));
                    docOut.add("allow-institutional", allowInstitutional);
                    docOut.add("allow-ANDS", allowANDS);

                    // Fish out the project owner if exists. Could be in two
                    // places.
                    Collection<XmlDoc.Element> projectOwners = meta
                            .elements("object/meta/daris:pssd-project-owner/project-owner");
                    if (projectOwners == null) {
                        if (auth != null)
                            projectOwners = auth.elements("project-owner");
                    }
                    if (projectOwners != null) {
                        for (XmlDoc.Element projectOwner : projectOwners) {
                            docOut.add(projectOwner);
                        }
                    }

                    // Find publications
                    if (pubs != null) {
                        addDocument(docOut, "publications", pubs);
                    }

                    // Related services
                    if (services != null) {
                        addDocument(docOut, "related-services", services);
                    }

                    // FOR code may be in daris:pssd-project-harvest or
                    // daris:pssd-project-research-category
                    // wqhich holds generic information about the field of
                    // research
                    // TBD make list unique
                    Collection<String> FORs = null;
                    if (auth != null) {
                        FORs = auth.values("field-of-research");
                        addFORs(docOut, FORs);
                    }

                    if (category != null) {
                        FORs = category.values("ANZSRC-11");
                        addFORs(docOut, FORs);
                        //
                        Collection<String> keywords = category
                                .values("keyword");
                        if (keywords != null) {
                            for (String keyword : keywords) {
                                docOut.add("keyword", keyword);
                            }
                        }
                    }

                    // Generic infor about funding/ethics etc
                    if (governance != null) {
                        Collection<XmlDoc.Element> funding = governance
                                .elements("funding-id");
                        if (funding != null) {
                            // Dereference the type from
                            // nig.funding.organization
                            for (XmlDoc.Element fund : funding) {
                                docOut.add(fund);
                            }
                        }
                        //
                        Collection<XmlDoc.Element> ethics = governance
                                .elements("ethics-id");
                        if (ethics != null) {
                            // Dereference the type from
                            // nig.funding.organization
                            for (XmlDoc.Element ethic : ethics) {
                                docOut.add(ethic);
                            }
                        }
                    }
                }
            }
        }
        return docOut;
    }

    private void addFORs(XmlDocMaker docOut, Collection<String> FORs)
            throws Throwable {
        if (FORs == null)
            return;
        for (String FOR : FORs) {
            docOut.add(
                    "field-of-research",
                    new String[] { "source",
                            "Australian and New Zealand Standard Research Classifications" },
                    FOR);
        }
    }

    private void addDocument(XmlDocMaker docOut, String parent,
            XmlDoc.Element docIn) throws Throwable {
        Collection<XmlDoc.Element> els = docIn.elements();
        if (els != null) {
            docOut.push(parent);
            for (XmlDoc.Element el : els) {
                docOut.add(el);
            }
            docOut.pop();
        }
    }

    private void addCounters(ServiceExecutor executor, String id,
            XmlDocMaker meta) throws Throwable {

        meta.add("number-of-subjects", +nSubjects(executor, id));
        meta.add("number-of-studies", +nStudies(executor, id, null));

        // FInd children study types
        XmlWriter w = new XmlStringWriter();
        Collection<String> studyTypes = SvcStudyTypesFind.findStudyTypes(
                executor, id, w);
        if (studyTypes != null) {
            if (studyTypes.size() > 0) {
                meta.push("study-details");
                for (String studyType : studyTypes) {
                    int n = nStudies(executor, id, studyType);
                    meta.add("type", new String[] { "number", "" + n },
                            studyType);
                }
                meta.pop();
            }
        }

        meta.add("number-of-datasets", +nDataSets(executor, id));
        //
        float s = sizeOfContent(executor(), id, "dataset");
        XmlDoc.Attribute att = new XmlDoc.Attribute("units", "bytes");
        XmlDoc.Element sizeEl = new XmlDoc.Element("size-of-content", s);
        sizeEl.add(att);
        meta.add(sizeEl);
    }

    private int nSubjects(ServiceExecutor executor, String id) throws Throwable {
        return count(executor, id, "subject");
    }

    private int nStudies(ServiceExecutor executor, String id, String studyType)
            throws Throwable {
        return countStudies(executor, id, studyType);
    }

    private int nDataSets(ServiceExecutor executor, String id) throws Throwable {
        return count(executor, id, "dataset");
    }

    private int count(ServiceExecutor executor, String id, String type)
            throws Throwable {

        int n = 0;
        XmlDocMaker dm = new XmlDocMaker("args");
        String query = "cid starts with '" + id
                + "' and xpath(daris:pssd-object/type)='" + type + "'";
        dm.add("where", query);
        dm.add("action", "count");
        dm.add("size", "infinity");
        dm.add("pdist", 0);
        XmlDoc.Element r = executor.execute("asset.query", dm.root());
        if (r != null) {
            if (r.element("value").hasValue())
                n = r.intValue("value");
        }
        return n;

    }

    private int countStudies(ServiceExecutor executor, String id,
            String studyType) throws Throwable {

        int n = 0;
        XmlDocMaker dm = new XmlDocMaker("args");
        String query = "cid starts with '" + id
                + "' and xpath(daris:pssd-object/type)='study'";
        if (studyType != null)
            query += " and xpath(daris:pssd-study/type)='" + studyType + "'";
        dm.add("where", query);
        dm.add("action", "count");
        dm.add("size", "infinity");
        dm.add("pdist", 0);
        XmlDoc.Element r = executor.execute("asset.query", dm.root());
        if (r != null) {
            if (r.element("value").hasValue())
                n = r.intValue("value");
        }
        return n;

    }

    private float sizeOfContent(ServiceExecutor executor, String id, String type)
            throws Throwable {

        float s = (float) 0.0;
        XmlDocMaker dm = new XmlDocMaker("args");
        String query = "cid starts with '" + id
                + "' and xpath(daris:pssd-object/type)='" + type + "'";
        dm.add("where", query);
        dm.add("action", "sum");
        dm.add("xpath", "content/size");
        dm.add("size", "infinity");
        dm.add("pdist", 0);
        XmlDoc.Element r = executor.execute("asset.query", dm.root());
        if (r != null) {
            if (r.element("value").hasValue())
                s = r.floatValue("value");
        }
        return s;
    }

    public void printStream(InputStream is) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        System.out.println(writer.toString());
    }

}
