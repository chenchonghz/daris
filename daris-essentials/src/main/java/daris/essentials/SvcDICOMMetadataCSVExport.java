package daris.essentials;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.StringType;
import arc.streams.StreamCopy;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDICOMMetadataCSVExport extends PluginService {

    public static final String SERVICE_NAME = "dicom.metadata.csv.export";
    public static final String VALUE_DELIMITER = ";";
    public static final String CSV_DELIMITER = ",";

    private Interface _defn;

    public SvcDICOMMetadataCSVExport() {
        _defn = new Interface();
        _defn.add(new Interface.Element("tag", new StringType(Pattern.compile("\\d{8}")), "The DICOM element tag.", 1,
                Integer.MAX_VALUE));
        _defn.add(new Interface.Element("where", StringType.DEFAULT,
                "The seletion query - all DICOM assets matched this query will be included.", 1, 1));
        _defn.add(new Interface.Element("ignore-errors", BooleanType.DEFAULT,
                "Set to true to ignore the errors and continue to process the rest. Defaults to true.", 0, 1));
        _defn.add(new Interface.Element("with-column-header", BooleanType.DEFAULT,
                "Set to true to include column header as the first record. Defaults to true.", 0, 1));
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
        return "Export the given list of DICOM elements (by tags) of the DICOM data-sets returned by the specified query to .csv format.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        boolean ignoreErrors = args.booleanValue("ignore-errors", true);
        String[] tags = args.values("tag").toArray(new String[0]);
        boolean withColumnHeader = args.booleanValue("with-column-header", true);

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", "(" + args.value("where") + ") and (mf-dicom-series has value) and (asset has content)");
        dm.add("size", "infinity");
        dm.add("action", "get-cid");
        List<XmlDoc.Element> es = executor().execute("asset.query", dm.root()).elements("cid");
        if (es == null || es.isEmpty()) {
            w.add("exported", new String[] { "total", "0", "error", "0" }, 0);
            return;
        }

        int total = es.size();
        int nbErrors = 0;
        int nbExported = 0;

        String[] headers = null;
        if (withColumnHeader) {
            headers = new String[tags.length + 2];
            headers[0] = "id";
            headers[1] = "cid";
        }

        // create a csv file in tmp
        File csvFile = PluginTask.createTemporaryFile(".csv");
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(csvFile)));
        try {
            PluginTask.threadTaskBeginSetOf(es.size());
            for (XmlDoc.Element e : es) {
                StringBuilder sb = new StringBuilder();
                String id = e.value("@id");
                sb.append(id);
                sb.append(CSV_DELIMITER);
                String cid = e.value();
                sb.append(cid);
                sb.append(CSV_DELIMITER);
                PluginTask.checkIfThreadTaskAborted();
                PluginTask.setCurrentThreadActivity("Retrieving DICOM metadata from asset (id=" + id + ",cid=" + cid
                        + ")");
                dm = new XmlDocMaker("args");
                dm.add("id", id);
                dm.add("defn", withColumnHeader ? true : false);
                XmlDoc.Element re = null;
                try {
                    re = executor().execute("dicom.metadata.get", dm.root());
                } catch (Throwable t) {
                    nbErrors++;
                    t.printStackTrace(System.out);
                    if (ignoreErrors) {
                        continue;
                    } else {
                        throw t;
                    }
                }

                PluginTask.clearCurrentThreadActivity();
                for (int i = 0; i < tags.length; i++) {
                    if (withColumnHeader) {
                        String defn = re.value("de[@tag=\'" + tags[i] + "\']/defn");
                        if (headers[2 + i] == null && defn != null) {
                            headers[2 + i] = "(" + tags[i] + "): " + defn;
                        }
                    }
                    appendCsvValue(sb, joinValues(re.values("de[@tag=\'" + tags[i] + "\']/value")));
                }
                PluginTask.setCurrentThreadActivity("Saving DICOM metadata of asset (id=" + id + ",cid=" + cid
                        + ") to file");
                out.println(sb.toString());
                nbExported++;
                PluginTask.threadTaskCompletedMultipleOf(nbExported, total);
            }
        } finally {
            out.close();
        }
        if (withColumnHeader) {
            File csvFile2 = PluginTask.createTemporaryFile(".csv");
            prependHeaders(csvFile, headers, csvFile2);
            PluginTask.deleteTemporaryFile(csvFile);
            csvFile = csvFile2;
        }
        outputs.output(0).setData(PluginTask.deleteOnCloseInputStream(csvFile), csvFile.length(), "text/csv");

        w.add("exported", new String[] { "total", Integer.toString(total), "error", Integer.toString(nbErrors) },
                nbExported);
    }

    private void prependHeaders(File inFile, String[] headers, File outFile) throws Throwable {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(inFile));
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
        try {
            for (String header : headers) {
                out.print(header == null ? "" : header);
                out.print(CSV_DELIMITER);
            }
            out.println();
            StreamCopy.copy(in, out);
        } finally {
            in.close();
            out.close();
        }
    }

    private void appendCsvValue(StringBuilder sb, String value) {
        if (value == null) {
            sb.append("");
        } else {
            if (value.indexOf(CSV_DELIMITER) != -1) {
                String sv = value;
                if (value.indexOf('"') != -1) {
                    sv = value.replace('\"', '\'');
                }
                sb.append("\"");
                sb.append(sv);
                sb.append("\"");
            } else {
                sb.append(value);
            }
        }
        sb.append(CSV_DELIMITER);
    }

    private String joinValues(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(VALUE_DELIMITER);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString().trim();
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public boolean canBeAborted() {
        return true;
    }

    @Override
    public int maxNumberOfOutputs() {
        return 1;
    }

    @Override
    public int minNumberOfOutputs() {
        return 1;
    }

}
