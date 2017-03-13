package daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcCollectionDicomDatasetCount extends PluginService {

    public static final String SERVICE_NAME = "daris.collection.dicom.dataset.count";

    private Interface _defn;

    public SvcCollectionDicomDatasetCount() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The root citeable id of the collection. Should not be specified together with 'where' at the same time. If both 'cid' and 'where' are not specified, all the DICOM datasets in the repository will be counted.",
                0, 1));
        _defn.add(new Interface.Element("where", StringType.DEFAULT,
                "A query to find the DICOM datasets. Should not be specified together with 'cid' at the same time. If both 'cid' and 'where' are not specified, all the DICOM datasets in the repository will be counted.",
                0, 1));
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

        return "Count the number of DICOM datasets in the specified DaRIS collection.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {

        String cid = args.value("cid");
        String where = args.value("where");

        if (cid != null && where != null) {
            throw new IllegalArgumentException(
                    "Both cid and where argument are specified. Expecting only one.");
        }
        int count = 0;
        if (cid != null) {
            count = countDicomDatasets(executor(), cid);
        } else {
            count = countDicomDatasets(where, executor());
        }
        w.add("value", count);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static int countDicomDatasets(ServiceExecutor executor, String cid)
            throws Throwable {
        if (cid != null) {
            StringBuilder sb = new StringBuilder("cid='").append(cid)
                    .append("' or cid starts with '").append(cid).append("'");
            return countDicomDatasets(sb.toString(), executor);
        } else {
            return countDicomDatasets(null, executor);
        }
    }

    public static int countDicomDatasets(String where, ServiceExecutor executor)
            throws Throwable {
        StringBuilder sb = new StringBuilder();
        if (where != null) {
            sb.append("(").append(where).append(")").append(" and ");
        }
        sb.append("(asset has content)")
                .append(" and (mf-dicom-series has value)")
                .append(" and (model='om.pssd.dataset')");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("action", "count");
        dm.add("where", sb.toString());
        return executor.execute("asset.query", dm.root()).intValue("value", 0);
    }

}
