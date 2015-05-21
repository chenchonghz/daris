package nig.mf.plugin.pssd.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import nig.mf.plugin.pssd.ApplicationProperty;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcTempAssetCreate extends PluginService {

    public static enum Unit {
        year, month, week, day, hour, minute, second;
        public int calendarField() {
            switch (this) {
            case year:
                return Calendar.YEAR;
            case month:
                return Calendar.MONTH;
            case week:
                return Calendar.WEEK_OF_YEAR;
            case day:
                return Calendar.DAY_OF_MONTH;
            case hour:
                return Calendar.HOUR_OF_DAY;
            case minute:
                return Calendar.MINUTE;
            case second:
                return Calendar.SECOND;
            default:
                return -1;
            }
        }
    }

    private Interface _defn;

    public SvcTempAssetCreate() {
        _defn = new Interface();
        Interface.Element meta = new Interface.Element(
                "meta",
                XmlDocType.DEFAULT,
                "Document containing meta information documents for the asset. The elements must correspond to valid asset document types.",
                0, 1);
        meta.setIgnoreDescendants(true);
        _defn.add(meta);
        /*
         * schedule a job to destroy the asset when it expires
         */
        Interface.Element expire = new Interface.Element("expire", IntegerType.POSITIVE_ONE,
                "When it expires, the asset will be destoryed.", 0, 1);
        expire.add(new Interface.Attribute("unit", new EnumType(Unit.values()), "The unit of the value.", 1));
        _defn.add(expire);
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Creates a temporary asset. The asset will be created in the namespace returned by om.pssd.temp.namespace.get.";
    }

    @Override
    public int minNumberOfInputs() {
        return 0;
    }

    @Override
    public int maxNumberOfInputs() {
        return 1;
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        /*
         * create the asset
         */
        String namespace = ApplicationProperty.TemporaryNamespace.get(executor());
        XmlDoc.Element meta = args.element("meta");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("namespace", new String[] { "create", "true" }, namespace);
        if (meta != null) {
            dm.add(meta, true);
        }
        String id = executor().execute("asset.create", dm.root(), in, out).value("id");

        /*
         * schedule a job to destroy after access
         */
        int expire = args.intValue("expire", 0);
        Date date = null;
        Unit unit = Unit.valueOf(args.value("expire/@unit"));
        if (expire > 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(unit.calendarField(), expire);
            date = cal.getTime();
            dm = new XmlDocMaker("args");
            dm.add("name", "destory temporary asset " + id);
            dm.push("when");
            dm.add("date", date);
            dm.pop();
            dm.push("service", new String[] { "name", "asset.destroy" });
            dm.add("id", id);
            dm.pop();
            executor().execute("schedule.job.create", dm.root());
        }

        /*
         * output the asset id
         */
        w.add("id", new String[] { "namespace", namespace, "expire",
                date == null ? null : new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(date) }, id);

    }

    @Override
    public String name() {
        return "om.pssd.temp.asset.create";
    }

}
