package daris.client.model.sc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.util.CollectionUtil;

public class Archive {

    public static final String PARAMETER_ISO_TYPE = "iso-type";
    public static final String PARAMETER_ENABLE_ROCKRIDGE = "enable-rockridge";
    public static final String PARAMETER_ENABLE_JOLIET = "enable-joliet";
    public static final String PARAMETER_PUBLISHER = "publisher";
    public static final String PARAMETER_VOLUME_NAME = "volume-name";
    public static final String PARAMETER_COMPRESSION_LEVEL = "compression-level";

    public static final int DEFAULT_COMPRESSION_LEVEL = 6;

    public static enum Type {

        // @formatter:off
        none("none", null, false, null), 
        zip("zip", Arrays.asList(PARAMETER_COMPRESSION_LEVEL), true, "zip"), 
        aar("aar",Arrays.asList(PARAMETER_COMPRESSION_LEVEL), true, "aar"), 
        jar("jar", Arrays.asList(PARAMETER_COMPRESSION_LEVEL), true, "jar"), 
        tar("tar", null, false, "tar"), 
        compressed_tar("compressed-tar", Arrays.asList(PARAMETER_COMPRESSION_LEVEL), true,"tar.gz"), 
        iso9660("iso9660",Arrays.asList(PARAMETER_ISO_TYPE, PARAMETER_ENABLE_ROCKRIDGE,PARAMETER_ENABLE_JOLIET, PARAMETER_PUBLISHER, PARAMETER_VOLUME_NAME), false, "iso");
        // @formatter:on

        private String _typeName;
        private Set<String> _params = null;
        private boolean _compressible = false;
        private String _extension = null;

        Type(String typeName, Collection<String> params, boolean compressible,
                String extension) {
            _typeName = typeName;
            _compressible = compressible;
            _extension = extension;
            if (params != null && !params.isEmpty()) {
                _params = new HashSet<String>(params);
            }
        }

        public boolean compressible() {
            return _compressible;
        }

        @Override
        public final String toString() {
            return _typeName;
        }

        public final String typeName() {
            return _typeName;
        }

        public String extension() {
            return _extension;
        }

        public Set<String> parameters() {
            if (_params != null && !_params.isEmpty()) {
                return Collections.unmodifiableSet(_params);
            } else {
                return null;
            }
        }

        public static Type fromString(String type) {

            if (type != null) {
                Type[] vs = values();
                for (Type v : vs) {
                    if (v.toString().equals(type)) {
                        return v;
                    }
                }
            }
            return none;
        }

        public static Type[] archiveTypes() {
            return new Type[] { zip, aar, jar, tar, compressed_tar, iso9660 };
        }

    }

    public static enum IsoType {
        cd, dvd_single, dvd_double;

        @Override
        public final String toString() {

            return super.toString().replace("_", "-");
        }

        public static IsoType fromString(String isoType) {

            if (isoType != null) {
                IsoType[] vs = values();
                for (IsoType v : vs) {
                    if (v.toString().equals(isoType)) {
                        return v;
                    }
                }
            }
            return cd;
        }

    }

    private Type _type;
    private Map<String, String> _params;

    public Archive(Type type) {

        setType(type);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Archive)) {
            return false;
        }
        Archive ao = (Archive) o;
        return _type == ao.type()
                && CollectionUtil.mapEquals(_params, ao.params());
    }

    public void setParam(String name, Object value) {
        setParam(name, String.valueOf(value));
    }

    public void setParam(String name, String value) {

        if (name == null) {
            return;
        }
        if (_params == null) {
            _params = new java.util.HashMap<String, String>();
        }
        _params.put(name, value);
    }

    protected void removeParam(String name) {

        if (_params != null) {
            _params.remove(name);
        }
    }

    public String paramValue(String name) {
        if (_params != null) {
            return _params.get(name);
        }
        return null;
    }

    public boolean hasParams() {

        if (_params != null) {
            return !_params.isEmpty();
        }
        return false;
    }

    public Map<String, String> params() {
        if (hasParams()) {
            return Collections.unmodifiableMap(_params);
        } else {
            return null;
        }
    }

    public Type type() {

        return _type;
    }

    public void setType(Type type) {

        if (_type != type) {
            _type = type;
            resetDefaultParameters(_type);
        }
    }

    private void resetDefaultParameters(Type type) {
        if (_params != null && !_params.isEmpty()) {
            _params.clear();
        }
        switch (type) {
        case zip:
        case aar:
        case jar:
        case compressed_tar:
            setParam(PARAMETER_COMPRESSION_LEVEL, DEFAULT_COMPRESSION_LEVEL);
            break;
        case iso9660:
            setParam(PARAMETER_ENABLE_JOLIET, Boolean.TRUE);
            setParam(PARAMETER_ENABLE_ROCKRIDGE, Boolean.FALSE);
            setParam(PARAMETER_ISO_TYPE, IsoType.cd);
            setParam(PARAMETER_PUBLISHER, (String) null);
            setParam(PARAMETER_VOLUME_NAME, (String) null);
            break;
        default:
            break;
        }
    }

    public void saveUpdateArgs(XmlWriter w) {
        w.push("packaging");
        w.add("package-method", _type.name());
        if (hasParams()) {
            Set<String> names = _params.keySet();
            for (String name : names) {
                w.add("parameter", new String[] { "name", name },
                        _params.get(name));
            }
        }
        w.pop();
    }

    // @formatter:off
    /**
     * Instantiate an archive object from the xml element in the form of:
     *    :cart
     *        :packaging zip
     *            :parameter -name compression-level 6
     * @param ce the XML element represents the shopping cart.
     * @return
     * @throws Throwable
     */
    // @formatter:on
    public static Archive instantiate(XmlElement ce) throws Throwable {

        XmlElement pe = ce.element("packaging");
        if (pe == null) {
            return null;
        } else {
            Type type = Type.fromString(pe.value());
            Archive archive = new Archive(type);
            List<XmlElement> paramElems = pe.elements("parameter");
            if (paramElems != null) {
                for (XmlElement paramElem : paramElems) {
                    archive.setParam(paramElem.value("@name"),
                            paramElem.value());
                }
            }
            return archive;
        }
    }

}
