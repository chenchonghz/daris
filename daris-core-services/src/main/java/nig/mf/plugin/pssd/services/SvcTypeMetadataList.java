package nig.mf.plugin.pssd.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nig.mf.pssd.ProjectRole;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcTypeMetadataList extends PluginService {

    private Interface _defn;

    public SvcTypeMetadataList() {
        _defn = new Interface();
        _defn.add(new Interface.Element("project", CiteableIdType.DEFAULT, "The citeable id of the project.", 0, 1));
        _defn.add(new Interface.Element("type", new EnumType(new String[] { "project", "subject", "study" }),
                "The object type.", 1, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "List the metadata (doc types) associated with the specified object type.";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        String type = args.value("type");
        String projectCid = args.value("project");
        if (("subject".equals(type) || "study".equals(type)) && projectCid == null) {
            throw new Exception("The citeable id of the project the " + type + " belongs to is required.");
        }

        Map<String, Boolean> map = new HashMap<String, Boolean>();

        /*
         * Add doc types registered in asset model.
         */
        List<XmlDoc.Element> des = executor().execute("asset.model.describe",
                "<args><name>om.pssd." + type + "</name></args>", null, null).elements(
                "model/entity/template/metadata/definition");
        addDocTypes(map, des);

        /*
         * Add doc types registered in methods
         */
        if ("study".equals(type) || "subject".equals(type)) {
            Collection<String> methodIds = executor()
                    .execute(
                            "asset.query",
                            "<args><where>cid='"
                                    + projectCid
                                    + "'</where><action>get-values</action><xpath ename=\"method\">meta/daris:pssd-project/method/id</xpath></args>",
                            null, null).values("asset/method");
            if (methodIds != null) {
                for (String methodId : methodIds) {
                    boolean isSubjectAdmin = isSubjectAdmin(projectCid);
                    parseMethod(map, type, isSubjectAdmin, methodId);
                }
            }

        }

        List<String> mandatoryDocTypes = new ArrayList<String>();
        List<String> optionalDocTypes = new ArrayList<String>();
        for (String docType : map.keySet()) {
            if (map.get(docType)) {
                mandatoryDocTypes.add(docType);
            } else {
                optionalDocTypes.add(docType);
            }
        }
        Collections.sort(mandatoryDocTypes);
        Collections.sort(optionalDocTypes);

        for (String docType : mandatoryDocTypes) {
            w.push("metadata");
            w.add("definition", new String[] { "requirement", "mandatory" }, docType);
            w.pop();
        }

        for (String docType : optionalDocTypes) {
            w.push("metadata");
            w.add("definition", new String[] { "requirement", "optional" }, docType);
            w.pop();
        }
    }

    private void parseMethod(Map<String, Boolean> map, String type, boolean isSubjectAdmin, String methodId)
            throws Throwable {
        XmlDoc.Element me = executor().execute("om.pssd.method.describe",
                "<args><expand>true</expand><id>" + methodId + "</id></args>", null, null);
        if (type.equals("subject")) {
            addDocTypes(map, me.elements("method/subject/project/public/metadata/definition"));
            if (isSubjectAdmin) {
                addDocTypes(map, me.elements("method/subject/project/private/metadata/definition"));
            }
        }
        List<XmlDoc.Element> ses = me.elements("method/step");
        if (ses != null) {
            for (XmlDoc.Element se : ses) {
                parseStep(map, type, isSubjectAdmin, se);
            }
        }
    }

    private void parseStep(Map<String, Boolean> map, String type, boolean isSubjectAdmin, XmlDoc.Element se)
            throws Throwable {
        if (se.elementExists("branch")) {
            List<XmlDoc.Element> cses = se.elements("branch/method/step");
            if (cses != null) {
                for (XmlDoc.Element cse : cses) {
                    parseStep(map, type, isSubjectAdmin, cse);
                }
            }
        } else if (se.elementExists("method")) {
            List<XmlDoc.Element> cses = se.elements("method/step");
            if (cses != null) {
                for (XmlDoc.Element cse : cses) {
                    parseStep(map, type, isSubjectAdmin, cse);
                }
            }
        } else if (se.elementExists(type)) {
            addDocTypes(map, se.elements(type + "/metadata/definition"));
        }
    }

    private void addDocTypes(Map<String, Boolean> map, List<XmlDoc.Element> des) throws Throwable {
        if (des != null) {
            for (XmlDoc.Element de : des) {
                String requirement = de.value("@requirement");
                map.put(de.value(), "mandatory".equals(requirement));
            }
        }
    }

    private boolean isSubjectAdmin(String projectCid) throws Throwable {
    	ProjectRole pr = new ProjectRole(ProjectRole.Type.subject_administrator, projectCid);
    	String rn = pr.name();
        return executor().execute("actor.self.have",
                "<args><role type=\"role\">" + rn + "</role></args>", null, null)
                .booleanValue("role");
    }

    @Override
    public String name() {
        return "om.pssd.type.metadata.list";
    }

}
