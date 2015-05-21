package daris.client;

import arc.gui.gwt.widget.dialog.Dialog;
import arc.mf.client.dti.DTIApplet;
import arc.mf.client.dti.DTIAppletStatusMonitor;

public class DTI {

    public static final String URL_JAVA_ISSUES = "http://nsp.nectar.org.au/wiki-its-r/doku.php?id=data_management:daris:admin:java";

    /**
     * Installs DTI. Must be inside a valid session. In other words, must run it
     * after user logged in.
     */
    public static void install() {

        arc.mf.client.dti.DTI.install(new DTIAppletStatusMonitor() {

            @Override
            public boolean appletIsNotReady(DTIApplet a) {
                return true;
            }

            @Override
            public void appletIsReady(DTIApplet a) {
            }

            @Override
            public void appletFailedToLoad(DTIApplet a, String reason) {
                Dialog.warn(
                        "Error",
                        "Arcitecta Desktop Integration applet failed to load. Reason: "
                                + reason
                                + "<br/><br/>"
                                + " Without Arcitecta Desktop Integration Java applet, data importing and a few other functions will not be funtional. See <a href=\""
                                + URL_JAVA_ISSUES + "\"  target=\"_blank\">how to resolve Java issues.</a>");
            }

            @Override
            public boolean agentIsNotReady(DTIApplet a) {
                return true;
            }

            @Override
            public void agentFailedToStart(DTIApplet a, String reason) {
                Dialog.warn(
                        "Error",
                        "Arcitecta Desktop Integration agent failed to start. Reason: "
                                + reason
                                + "<br/><br/>"
                                + " Without Arcitecta Desktop Integration Java applet, data importing and a few other functions will not be funtional. See <a href=\""
                                + URL_JAVA_ISSUES + "\" target=\"_blank\">how to resolve Java issues.</a>");
            }

            @Override
            public void agentIsReady(DTIApplet a, int port, int securePort) {
                // TODO Auto-generated method stub

            }
        });
    }
}
