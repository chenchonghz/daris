package nig.mf.client.util;

import arc.mf.client.ServerClient;
import arc.xml.XmlStringWriter;

public class LogUtil {

	public static final int LOG_ERROR = 1;
	public static final int LOG_WARNING = 2;
	public static final int LOG_INFO = 3;

	/**
	 * Send a notification email from the mediaflux server to specified email address.
	 * 
	 * @param cxn
	 * @param to
	 * @param subject
	 * @param body
	 * @throws Throwable
	 */
	public static void mail(ServerClient.Connection cxn, String to, String subject, String body) throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("async", "true");
		w.add("to", to);
		w.add("subject", subject);
		w.add("body", body);
		cxn.execute("mail.send", w.document());

	}

	/**
	 * Write message to mediaflux server log.
	 * 
	 * @param cxn
	 * @param msg
	 * @param type
	 * @throws Throwable
	 */
	private static void log(ServerClient.Connection cxn, String logFile, String msg, int type) throws Throwable {
		if (logFile==null) return;
		
		String event = "info";
		if (type == LOG_ERROR) {
			event = "error";
		} else if (type == LOG_WARNING) {
			event = "warning";
		} else if (type == LOG_INFO) {
			event = "info";
		}
		XmlStringWriter w = new XmlStringWriter();
		w.add("app", logFile);
		w.add("event", event);
		w.add("msg", msg);
		cxn.execute("server.log", w.document());

	}

	/**
	 * Write error message into mediaflux server log.
	 * 
	 * @param cxn
	 * @param msg
	 * @throws Throwable
	 */
	public static void logError(ServerClient.Connection cxn, String logFile, String msg) throws Throwable {

		log(cxn, logFile, msg, LOG_ERROR);

	}

	/**
	 * write warning message into mediaflux server log.
	 * 
	 * @param cxn
	 * @param msg
	 * @throws Throwable
	 */
	public static void logWarning(ServerClient.Connection cxn, String logFile, String msg) throws Throwable {

		log(cxn, logFile, msg, LOG_WARNING);

	}

	/**
	 * write informational message into mediaflux server log.
	 * 
	 * @param cxn
	 * @param msg
	 * @throws Throwable
	 */
	public static void logInfo(ServerClient.Connection cxn, String logFile, String msg) throws Throwable {

		log(cxn, logFile, msg, LOG_INFO);

	}

}
