package daris.client.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class UserStatistics {

    public static Set<String> getUserEmails(ServerClient.Connection cxn, String role) throws Throwable {
        XmlStringWriter w = new XmlStringWriter();
        if (role != null) {
            w.add("role", new String[] { "type", "role" }, role);
        }
        w.add("size", "infinity");
        XmlDoc.Element re = cxn.execute("user.describe", w.document());
        List<XmlDoc.Element> ues = re.elements("user");
        if (ues != null) {
            Set<String> emails = new TreeSet<String>();
            for (XmlDoc.Element ue : ues) {
                if (ue.elementExists("e-mail")) {
                    emails.add(ue.value("e-mail").trim().toLowerCase());
                }
                if (ue.elementExists("asset/meta/mf-user/email")) {
                    emails.add(ue.value("asset/meta/mf-user/email").trim().toLowerCase());
                }
            }
            if (!emails.isEmpty()) {
                return emails;
            }
        }
        return null;
    }

    public static Map<String, Integer> countUserEmailsByDomain(Set<String> emails) {
        Map<String, Integer> map = new TreeMap<String, Integer>();
        for (String email : emails) {
            int idx = email.indexOf('@');
            if (idx < 0) {
                throw new AssertionError("Invalid email: " + email);
            }
            String domain = email.substring(idx + 1).trim().toLowerCase();
            int count = map.containsKey(domain) ? map.get(domain) : 0;
            count++;
            map.put(domain, count);
        }
        if (map.isEmpty()) {
            return null;
        }
        return map;
    }

    public static void main(String[] args) throws Throwable {
        //@formatter:off
//        RemoteServer rs = new RemoteServer("localhost", 8080, true, true);
//        ServerClient.Connection cxn = rs.open();
//        try {
//            cxn.connect("domain", "user", "password");
//            Set<String> emails = getUserEmails(cxn, "daris:pssd.model.user");
//            if (emails != null && !emails.isEmpty()) {
//                Map<String, Integer> counts = countUserEmailsByDomain(emails);
//                if (counts != null && !emails.isEmpty()) {
//                    Set<String> domains = counts.keySet();
//                    for (String domain : domains) {
//                        System.out.println(String.format("%s, %d,", domain, counts.get(domain)));
//                    }
//                }
//            }
//        } finally {
//            cxn.close();
//        }
        //@formatter:on
    }

}
