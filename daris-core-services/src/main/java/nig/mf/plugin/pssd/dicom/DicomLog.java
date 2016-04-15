package nig.mf.plugin.pssd.dicom;

import arc.mf.plugin.PluginLog;

public class DicomLog {

    public static final String NAME = "dicom";

    public static void info(String message) {
        PluginLog.log(NAME).add(PluginLog.INBOUND, message);
    }

    public static void warn(String message) {
        PluginLog.log(NAME).add(PluginLog.WARNING, message);
    }

    public static void error(String message, Throwable t) {
        PluginLog.log(NAME).add(PluginLog.ERROR, message, t);
    }

    public static void error(String message) {
        PluginLog.log(NAME).add(PluginLog.ERROR, message);
    }
}
