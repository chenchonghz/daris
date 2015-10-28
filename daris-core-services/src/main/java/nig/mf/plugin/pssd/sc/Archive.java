package nig.mf.plugin.pssd.sc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import arc.xml.XmlDoc;

public class Archive {

    public static final String PARAMETER_ISO_TYPE = "iso-type";
    public static final String PARAMETER_ENABLE_ROCKRIDGE = "enable-rockridge";
    public static final String PARAMETER_ENABLE_JOLIET = "enable-joliet";
    public static final String PARAMETER_PUBLISHER = "publisher";
    public static final String PARAMETER_VOLUME_NAME = "volume-name";
    public static final String PARAMETER_COMPRESSION_LEVEL = "compression-level";
    public static final int DEFAULT_COMPRESSION_LEVEL = 6;
    public static final boolean DEFAULT_ENABLE_JOLIET = true;
    public static final boolean DEFAULT_ENABLE_ROCKRIDGE = false;

    public static enum Type {
        none, zip, aar, jar, tar, compressed_tar, iso9660;

        @Override
        public final String toString() {
            return super.toString().replace('_', '-');
        }

        public static Type fromString(String type) {
            Type[] vs = values();
            for (Type v : vs) {
                if (compressed_tar.toString().equals(type)
                        || compressed_tar.name().equals(type)) {
                    return compressed_tar;
                } else if (v.toString().equalsIgnoreCase(type)) {
                    return v;
                }
            }
            return none;
        }

        public static String[] stringValues() {
            Type[] vs = values();
            String[] svs = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                svs[i] = vs[i].toString();
            }
            return svs;
        }
    }

    public static enum ISOType {
        cd, dvd_single, dvd_double;
        @Override
        public String toString() {

            return super.toString().replace("_", "-");
        }

        public static ISOType instantiate(String mediaType) {

            if (mediaType != null) {
                if (mediaType.equalsIgnoreCase(dvd_single.toString())) {
                    return dvd_single;
                } else if (mediaType.equalsIgnoreCase(dvd_double.toString())) {
                    return dvd_double;
                }
            }
            return cd;
        }
    }

    private Type _type;
    private Map<String, String> _parameters;

    protected Archive(Type type) {
        _type = type;
    }

    protected void setParameter(String name, String value) {
        if (name == null || value == null) {
            return;
        }
        if (_parameters == null) {
            _parameters = new java.util.HashMap<String, String>();
        }
        _parameters.put(name, value);
    }

    protected void removeParameter(String name) {
        if (_parameters != null) {
            _parameters.remove(name);
        }
    }

    protected String getParameterValue(String name) {
        if (_parameters != null) {
            return _parameters.get(name);
        }
        return null;
    }

    public Set<String> getParameterNames() {
        if (_parameters != null) {
            return _parameters.keySet();
        }
        return null;
    }

    public boolean hasParameters() {
        if (_parameters != null) {
            return !_parameters.isEmpty();
        }
        return false;
    }

    public Type type() {
        return _type;
    }

    public static Archive instantiate(XmlDoc.Element arcElement)
            throws Throwable {

        Type type = Type.fromString(arcElement.value("type"));
        Archive arc = new Archive(type);
        List<XmlDoc.Element> pes = arcElement.elements("parameter");
        if (pes != null) {
            for (XmlDoc.Element pe : pes) {
                arc.setParameter(pe.value("@name"), pe.value());
            }
        }
        return arc;
    }

    public static Archive create(Type type) throws Throwable {

        Archive arc = new Archive(type);
        switch (type) {
        case none:
        case tar:
            break;
        case zip:
        case aar:
        case jar:
        case compressed_tar:
            arc.setParameter("compression-level", "6");
            break;
        case iso9660:
            arc.setParameter(PARAMETER_ENABLE_JOLIET,
                    Boolean.toString(DEFAULT_ENABLE_JOLIET));
            arc.setParameter(PARAMETER_ENABLE_ROCKRIDGE,
                    Boolean.toString(DEFAULT_ENABLE_ROCKRIDGE));
            arc.setParameter(PARAMETER_ISO_TYPE, ISOType.cd.toString());
            arc.setParameter(PARAMETER_PUBLISHER, (String) null);
            arc.setParameter(PARAMETER_VOLUME_NAME, (String) null);
            break;
        }
        return arc;
    }

}
