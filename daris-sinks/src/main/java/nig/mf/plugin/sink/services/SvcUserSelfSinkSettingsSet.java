package nig.mf.plugin.sink.services;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nig.mf.plugin.sink.settings.UserSelfSinkSettings;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserSelfSinkSettingsSet extends PluginService {

    public static final String SERVICE_NAME = "nig.user.self.sink.settings.set";

    public static enum Action {
        merge, replace
    }

    private Interface _defn;

    public SvcUserSelfSinkSettingsSet() {
        _defn = new Interface();

        Interface.Element se = new Interface.Element("sink", XmlDocType.DEFAULT, "The sink.", 1, Integer.MAX_VALUE);
        se.add(new Interface.Attribute(
                "action",
                new EnumType(Action.values()),
                "Action to perform when modifying the sink settings. Defaults to 'merge'. 'merge' means combine, 'replace' means to replace the whole sink settings. If action is replace and no arg is not given it will remove the whole settings.",
                0));
        se.add(new Interface.Element("name", StringType.DEFAULT, "The sink name.", 1, 1));

        Interface.Element ae = new Interface.Element("arg", StringType.DEFAULT, "The sink argument.", 0,
                Integer.MAX_VALUE);
        ae.add(new Interface.Attribute("name", StringType.DEFAULT, "The name of the argument.", 1));
        se.add(ae);

        _defn.add(se);
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
        return "Set the user specific settings for the given sink. The argument must be defined by sink type. To check the available sink args: run service: "
                + SvcSinkDescribe.SERVICE_NAME + ".";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        List<XmlDoc.Element> ses = args.elements("sink");
        if (ses != null) {
            for (XmlDoc.Element se : ses) {
                String sinkName = se.value("name");
                Action action = Action.valueOf(se.stringValue("@action", Action.merge.name()));
                List<XmlDoc.Element> aes = se.elements("arg");
                setSinkSettings(executor(), sinkName, action, aes);
            }
        }

    }

    private static boolean hasCustomizableArgs(XmlDoc.Element sde) throws Throwable {
        List<XmlDoc.Element> aes = sde.elements("arg");
        boolean has = false;
        if (aes != null) {
            for (XmlDoc.Element ae : aes) {
                if (!"constant".equals(ae.value("@type"))) {
                    has = true;
                    break;
                }
            }
        }
        return has;
    }

    private static void setSinkSettings(ServiceExecutor executor, String sinkName, Action action,
            List<XmlDoc.Element> aes) throws Throwable {

        if (aes == null || aes.isEmpty()) {
            if (action == Action.replace) {
                UserSelfSinkSettings.remove(executor, sinkName);
            }
            return;
        }

        XmlDoc.Element sde = executor.execute(SvcSinkDescribe.SERVICE_NAME,
                "<args><name>" + sinkName + "</name></args>", null, null).element("sink");
        if (sde == null || !sde.elementExists("arg")) {
            throw new IllegalArgumentException("The sink: " + sinkName + " does not accept any argument.");
        }

        if (!hasCustomizableArgs(sde)) {
            throw new IllegalArgumentException("The sink: " + sinkName
                    + " does not have any user customizable argument.");
        }
        XmlDocMaker dm = new XmlDocMaker("settings");
        dm.addAll(aes);
        XmlDoc.Element se = dm.root();

        Map<String, String> sas = new LinkedHashMap<String, String>();
        Map<String, Set<String>> mas = new LinkedHashMap<String, Set<String>>();
        for (XmlDoc.Element ae : aes) {
            String name = ae.value("@name");
            String value = ae.value();
            String type = sde.value("arg[@name='" + name + "']/@type");
            if ("enumeration".equals(type)) {
                String enumValues = sde.value("arg[@name='" + name + "']/@enumerated-values");
                if (enumValues != null && !enumContains(enumValues, value)) {
                    throw new IllegalArgumentException("Sink argument: " + name + " has a invalid value: '" + value
                            + "'. It should be one of the enumerated values: " + enumValues + ". Run service "
                            + SvcSinkDescribe.SERVICE_NAME + " to check the valid enumerated values.");
                }
            }
            int occurs = se.count("arg[@name='" + name + "']");
            if (!sde.elementExists("arg[@name='" + name + "']")) {
                throw new IllegalArgumentException("Sink argument: '" + name + "' does not exist in sink: " + sinkName
                        + ". Run service " + SvcSinkDescribe.SERVICE_NAME + " to check the available arguments.");
            }
            if ("constant".equals(sde.value("arg[@name='" + name + "']/@type"))) {
                throw new IllegalArgumentException("Sink argument: '" + name + "' in sink: '" + sinkName
                        + "' is not user-customizable. Run service " + SvcSinkDescribe.SERVICE_NAME
                        + " and see the 'editable' arguments.");
            }
            int maxOccursIUS = sde.intValue("arg[@name='" + name + "']/@max-occurs-in-user-settings", 1);
            if (occurs > maxOccursIUS) {
                throw new IllegalArgumentException("Sink argument: '" + name + "' in sink: '" + sinkName + "' occurs "
                        + occurs + " times, which exceeds the maximum limit: " + maxOccursIUS + " in user settings.");
            }

            if (maxOccursIUS > 1) {
                if (!mas.containsKey(name)) {
                    mas.put(name, new LinkedHashSet<String>(se.values("arg[@name='" + name + "']")));
                }
            } else {
                if (!sas.containsKey(name)) {
                    sas.put(name, value);
                }
            }
        }
        if (action == Action.replace) {
            UserSelfSinkSettings.set(executor, sinkName, se);
        } else if (action == Action.merge) {
            XmlDoc.Element ose = UserSelfSinkSettings.get(executor, sinkName);
            if (ose == null || !ose.elementExists("arg")) {
                UserSelfSinkSettings.set(executor, sinkName, se);
                return;
            } else {
                List<XmlDoc.Element> oaes = ose.elements("arg");
                for (XmlDoc.Element oae : oaes) {
                    String name = oae.value("@name");
                    int maxOccursIUS = sde.intValue("arg[@name='" + name + "']/@max-occurs-in-user-settings", 1);
                    if (maxOccursIUS > 1) {
                        Set<String> oavs = new LinkedHashSet<String>(ose.values("arg[@name='" + name + "']"));
                        if (!mas.containsKey(name)) {
                            mas.put(name, oavs);
                        } else {
                            Set<String> avs = mas.get(name);
                            if (avs.size() < maxOccursIUS && !avs.containsAll(oavs)) {
                                avs.addAll(oavs);
                                mas.put(name, avs);
                            }
                        }
                    } else {
                        if (!sas.containsKey(name)) {
                            sas.put(name, oae.value());
                        }
                    }
                }
                dm = new XmlDocMaker("settings");
                for (String name : sas.keySet()) {
                    dm.add("arg", new String[] { "name", name }, sas.get(name));
                }
                for (String name : mas.keySet()) {
                    Set<String> values = mas.get(name);
                    int maxOccursIUS = sde.intValue("arg[@name='" + name + "']/@max-occurs-in-user-settings", 1);
                    int i = 0;
                    for (String value : values) {
                        if (i >= maxOccursIUS) {
                            break;
                        }
                        dm.add("arg", new String[] { "name", name }, value);
                        i++;
                    }
                }
                UserSelfSinkSettings.set(executor, sinkName, dm.root());
            }
        }

    }

    private static boolean enumContains(String enumValues, String value) {
        String[] vs = enumValues.split(",");
        for (String v : vs) {
            if (v.trim().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
