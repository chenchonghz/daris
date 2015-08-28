package daris.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window.Navigator;

import daris.client.util.BrowserUtil;

public class BrowserCheck {

    public static final String ROOT = GWT.getHostPageBaseURL() + "docs/";

    public static final String INSTALL_JAVA = ROOT + "install-java.html";

    public static final String INSTALL_CHROMEFRAME = ROOT
            + "install-chromeframe.html";

    public static final String INSTALL_DTI = ROOT + "install-dti.html";

    public static void openPage(String url, String name) {
        com.google.gwt.user.client.Window.open(url, "Troubleshooting DTI",
                "menubar=no," + "location=false," + "resizable=yes,"
                        + "scrollbars=yes," + "status=no," + "dependent=true");
    }

    public static void openInstallJavaPage() {
        openPage(INSTALL_JAVA, "Install Java");
    }

    public static void openInstallChromeframePage() {
        openPage(INSTALL_JAVA, "Install Chrome Frame for IE");
    }

    public static void openInstallDTIPage() {
        openPage(INSTALL_JAVA, "Install DTI");
    }

    public static void checkIE() throws Throwable {
        if (BrowserUtil.isIE() && !BrowserUtil.isChromeFrame()) {
            throw new Exception(
                    "This application uses features that are not available with the version of Internet Explorer you are using. <br/><br/> Internet Explorer must be version 10 or higher. <br/><br/> Detected browser version: "
                            + BrowserUtil.versionOfIE()
                            + ". <br/><br/> Luckily, Google have created a plugin to allow Internet Explorer to run this application properly. First, close this dialog and then click the Get Google Chrome Frame button in the Google installer. Once the installation is complete, this application will automatically reload.");
        }
    }

    public static void checkJava() throws Throwable {
        if (!Navigator.isJavaEnabled()) {
            BrowserCheck.openInstallJavaPage();
            throw new Exception(
                    "This application requires Java but it is not installed. Please <a href=\""
                            + BrowserCheck.INSTALL_JAVA
                            + "\">install Java</a>. ");
        }
    }

    public static void check(boolean noDTI) throws Throwable {
        checkIE();
        if (!noDTI) {
            checkJava();
        }
    }
}
