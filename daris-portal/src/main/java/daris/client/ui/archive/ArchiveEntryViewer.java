package daris.client.ui.archive;

import com.google.gwt.user.client.ui.Widget;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.panel.SimplePanel;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;

public class ArchiveEntryViewer implements InterfaceComponent {
    private static String[] _supportedImageExtensions = new String[] { "bmp",
            "tif", "tiff", "gif", "png", "jpg", "jpeg", "dcm" };

    private SimplePanel _sp;
    private ArchiveEntryCollectionRef _arc;
    private ArchiveEntry _entry;

    public ArchiveEntryViewer(ArchiveEntryCollectionRef arc) {
        _arc = arc;
        _entry = null;

        _sp = new SimplePanel();
        _sp.fitToParent();

    }

    @Override
    public Widget gui() {
        return _sp;
    }

    public void setEntry(ArchiveEntry entry) {
        _entry = entry;
        if (_entry == null) {
            _sp.clear();
        } else {
            _sp.setContent(viewerWidgetFor(_arc, _entry));
        }
    }

    protected BaseWidget viewerWidgetFor(final ArchiveEntryCollectionRef arc,
            final ArchiveEntry entry) {
        if (isSupportedImage(entry)) {
            return new ArchiveEntryImagePanel(arc, entry);
        } else {
            return new ArchiveEntryDetailPanel(entry);
        }
    }

    private static boolean isSupportedImage(String ext) {
        boolean extSupported = false;
        for (int i = 0; i < _supportedImageExtensions.length; i++) {
            if (_supportedImageExtensions[i].equalsIgnoreCase(ext)) {
                extSupported = true;
                break;
            }
        }
        return extSupported;
    }

    private static boolean isSupportedImage(ArchiveEntry entry) {
        return isSupportedImage(entry.fileExtension());
    }
}
