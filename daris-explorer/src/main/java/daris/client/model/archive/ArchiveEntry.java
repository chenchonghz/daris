package daris.client.model.archive;

import arc.mf.desktop.server.ServiceCall;
import arc.mf.desktop.server.ServiceResponseHandler;
import arc.mf.object.ObjectMessageResponse;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;

public class ArchiveEntry {

    private String _name;
    private int _idx;
    private long _size;

    public ArchiveEntry(XmlDoc.Element e) throws Throwable {
        _name = e.value();
        _idx = e.intValue("@idx", 1);
        _size = e.longValue("@size", -1);
    }

    public String name() {
        return _name;
    }

    public int ordinal() {
        return _idx;
    }

    public long size() {
        return _size;
    }

    public String fileName() {
        if (_name == null) {
            return null;
        }
        int idx = _name.lastIndexOf('/');
        if (idx == -1) {
            return _name;
        }
        return _name.substring(idx + 1);
    }

    public String fileExtension() {
        if (_name != null) {
            int idx = _name.lastIndexOf('.');
            if (idx >= 0) {
                String ext = _name.substring(idx + 1);
                if (ext != null && !ext.isEmpty()) {
                    return ext;
                }
            }
        }
        return null;
    }

    public boolean isViewAbleImage() {
        return isViewableImage(_name);
    }

    public void resolveMimeType(ObjectMessageResponse<String> rh) {
        String ext = fileExtension();
        if (ext == null) {
            if (rh != null) {
                rh.responded(null);
            }
            return;
        }
        new ServiceCall("type.ext.types")
                .setArguments("<extension>" + ext + "</extension>")
                .setResponseHandler(new ServiceResponseHandler() {
                    @Override
                    public boolean failed(Throwable ex) {
                        if (rh != null) {
                            rh.responded(null);
                        }
                        return false;
                    }

                    @Override
                    public void response(Element re) throws Throwable {
                        if (rh != null) {
                            rh.responded(re.value("extension/type"));
                        }
                    }
                }).execute();
    }

    private static boolean isViewableImage(String name) {
        if (name != null) {
            int idx = name.lastIndexOf('.');
            if (idx >= 0) {
                String ext = name.substring(idx + 1);
                if (ext != null && !ext.isEmpty()) {
                    return ext.equalsIgnoreCase("dcm")
                            || ext.equalsIgnoreCase("png")
                            || ext.equalsIgnoreCase("jpg")
                            || ext.equalsIgnoreCase("jpeg")
                            || ext.equalsIgnoreCase("bmp")
                            || ext.equalsIgnoreCase("tif")
                            || ext.equalsIgnoreCase("tiff")
                            || ext.equalsIgnoreCase("gif");
                }
            }
        }
        return false;
    }
}
