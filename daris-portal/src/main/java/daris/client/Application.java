package daris.client;

import arc.gui.gwt.widget.window.Window;
import arc.mf.client.plugin.Plugin;

public class Application {

    public static Window window() {
        if (Plugin.isStandaloneApplication()) {
            return DaRISStandAloneApplication.window();
        } else {
            return DaRISPluginApplication.get().window();
        }
    }

}
