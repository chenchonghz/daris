package daris.client.ui.archive;

import java.util.List;

import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.type.messages.TypesFromExt;

public class ArchiveEntryDetailPanel extends ContainerWidget {

    private ArchiveEntry _entry;
    private HTML _detail;

    private CenteringPanel _cp;

    public ArchiveEntryDetailPanel(ArchiveEntry entry) {
        _entry = entry;
        _cp = new CenteringPanel(Axis.BOTH);
        initWidget(_cp);
        _detail = new HTML();
        add(_detail);
        if (_entry.fileExtension() != null) {
            new TypesFromExt(_entry.fileExtension())
                    .send(new ObjectMessageResponse<List<String>>() {

                        @Override
                        public void responded(List<String> types) {
                            String type = (types == null || types.isEmpty())
                                    ? null : types.get(0);
                            updateDetail(_entry, type);
                        }
                    });
        } else {
            updateDetail(_entry, null);
        }
    }

    private void updateDetail(ArchiveEntry entry, String mimeType) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table>");
        sb.append("<tr><td align=\"right\">Name</td><td>");
        sb.append(entry.name());
        sb.append("</td></tr>");
        sb.append("<tr><td align=\"right\">Size</td><td>");
        sb.append(entry.size() < 0 ? "unknown" : entry.size());
        sb.append(" bytes");
        sb.append("</td></tr>");
        if (mimeType != null) {
            sb.append("<tr><td align=\"right\">MIME Type</td><td>");
            sb.append(mimeType);
            sb.append("</td></tr>");
        }
        sb.append("</table");
        _detail.setHTML(sb.toString());
    }
}
