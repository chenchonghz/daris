package daris.client;

import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.mf.client.xml.XmlElement;
import daris.client.ui.DObjectBrowser;

public class DaRISPluginApplicationInstance extends arc.mf.desktop.plugin.PluginApplicationInstance {

    private static DaRISPluginApplicationInstance _instance;

    public static DaRISPluginApplicationInstance get() {
        if (_instance == null) {
            DaRIS.initialise();
            _instance = new DaRISPluginApplicationInstance();
        }
        return _instance;
    }

    private DaRISPluginApplicationInstance() {
    }

    @Override
    public void construct(Window w, XmlElement ace) {
        w.addCloseListener(new WindowCloseListener() {

            @Override
            public void closed(Window w) {
                DObjectBrowser.reset();
                DaRIS.finalise();
            }
        });
        w.setContent(DObjectBrowser.get(true));
    }

}
