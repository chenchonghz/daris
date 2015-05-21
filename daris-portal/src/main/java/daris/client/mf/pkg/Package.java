package daris.client.mf.pkg;

import java.util.Date;

import arc.mf.client.xml.XmlElement;

import com.google.gwt.i18n.client.DateTimeFormat;

public class Package {

    private String _name;
    private String _version;
    private Date _buildTime;
    private String _vendor;
    private String _vendorURL;
    private String _description;

    public Package(String name, String version) {
        _name = name;
        _version = version;
    }

    public Package(XmlElement pe) throws Throwable {
        _name = pe.value("@name");
        _version = pe.value("version");
        String buildTime = pe.value("build-time");
        if (buildTime != null) {
            if (buildTime.length() > 20) {
                buildTime = buildTime.substring(0, buildTime.lastIndexOf(' '));
            }
            try {
                _buildTime = DateTimeFormat.getFormat("dd-MMM-yyyy HH:mm:ss")
                        .parse(buildTime);
            } catch (IllegalArgumentException e) {
            }
        }
        _vendor = pe.value("vendor");
        _vendorURL = pe.value("vendorurl");
        _description = pe.value("description");
    }

    public String name() {
        return _name;
    }

    public String version() {
        return _version;
    }

    public Date buildTime() {
        return _buildTime;
    }

    public String vendor() {
        return _vendor;
    }

    public String vendorURL() {
        return _vendorURL;
    }

    public String description() {
        return _description;
    }

}
