package daris.client;

import arc.gui.gwt.widget.window.Window;

public class DaRISPluginApplication extends arc.mf.desktop.plugin.PluginApplication {

    private static DaRISPluginApplication _app;

    public static DaRISPluginApplication get() {
        if (_app == null) {
            _app = new DaRISPluginApplication();
        }
        return _app;
    }

    private DaRISPluginApplication() {
        super("DaRIS", 0.9, 0.9);
    }

    @Override
    public arc.mf.desktop.plugin.PluginApplicationInstance instantiate() {
        return DaRISPluginApplicationInstance.get();
    }

    public Window window() {
        return DaRISPluginApplicationInstance.get().window();
    }

}
