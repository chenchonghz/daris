package daris.client;

import arc.gui.gwt.widget.dialog.Dialog;
import arc.mf.client.dti.DTIApplet;
import arc.mf.client.dti.DTIAppletStatusMonitor;

public class DTI {

	public static final String URL_JAVA_ISSUES = "http://nsp.nectar.org.au/wiki-its-r/doku.php?id=data_management:daris:admin:java";

	private static boolean _installing = false;

	/**
	 * Installs DTI. Must be inside a valid session. In other words, must run it
	 * after user logged in.
	 */
	public static void install() {

		_installing = true;
		arc.mf.client.dti.DTI.install(new DTIAppletStatusMonitor() {

			@Override
			public boolean appletIsNotReady(DTIApplet a) {
				System.out.println("appletIsNotReady: true");
				return true;
			}

			@Override
			public void appletIsReady(DTIApplet a) {
				System.out.println("appletIsReady");
			}

			@Override
			public void appletFailedToLoad(DTIApplet a, String reason) {
				System.out.println("agentFailedToLoad: " + reason);
				_installing = false;
				Dialog.warn(
						"DTI Agent",
						"Arcitecta DTI(Desktop Integration) applet failed to load.<br/> See <a href=\"http://nsp.nectar.org.au/wiki-its-r/doku.php?id=data_management:daris:admin:java\">this wiki page</a> for resolutions.");
				// now applet is ready still need to wait for the agent to be started.
			}

			@Override
			public boolean agentIsNotReady(DTIApplet a) {
				System.out.println("agentIsNotReady: true");
				return true;
			}

			@Override
			public void agentFailedToStart(DTIApplet a, String reason) {
				System.out.println("agentFailedToStart: " + reason);
				_installing = false;
				Dialog.warn(
						"DTI Agent",
						"Arcitecta DTI(Desktop Integration) agent failed to start.<br/> See <a href=\"http://nsp.nectar.org.au/wiki-its-r/doku.php?id=data_management:daris:admin:java\">this wiki page</a> for resolutions.");
				// now the dti is fully functional.
			}

			@Override
			public void agentIsReady(DTIApplet a, int port, int securePort) {
				System.out.println("agentIsReady on port: " + port
						+ ", secure port: " + securePort);
				_installing = false;
			}
		});
	}

	public static boolean installing() {
		return _installing;
	}

}
