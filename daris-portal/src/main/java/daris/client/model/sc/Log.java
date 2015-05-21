package daris.client.model.sc;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;

public class Log {

    public final Status status;
    public final Date changed;
    public final String message;

    public Log(XmlElement le) throws Throwable {
        status = Status.fromString(le.value("@status"));
        changed = le.dateValue("@changed");
        message = le.value();
    }

    public static List<Log> instantiate(List<XmlElement> les) throws Throwable {
        if (les != null) {
            List<Log> logs = new Vector<Log>();
            for (XmlElement le : les) {
                logs.add(new Log(le));
            }
            if (!logs.isEmpty()) {
                return logs;
            }
        }
        return null;
    }
}
