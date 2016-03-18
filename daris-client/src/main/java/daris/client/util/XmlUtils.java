package daris.client.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import arc.xml.XmlDoc;

public class XmlUtils {

    public static void saveToFile(XmlDoc.Element e, File f) throws Throwable {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
        try {
            os.write(e.toString().getBytes("UTF-8"));
            os.flush();
        } finally {
            os.close();
        }
    }

}
