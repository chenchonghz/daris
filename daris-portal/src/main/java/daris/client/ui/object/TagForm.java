package daris.client.ui.object;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.list.ListGridHeader;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.object.DObjectRef;
import daris.client.model.object.TagDictionary;
import daris.client.ui.dictionary.TermGrid;

public class TagForm extends ValidatedInterfaceComponent {

    private HorizontalSplitPanel _hsp;

    public TagForm(DObjectRef o, boolean projectAdmin) {
        
        _hsp = new HorizontalSplitPanel();
        _hsp.fitToParent();

        VerticalPanel tagsVP = new VerticalPanel();
        tagsVP.fitToParent();
        _hsp.add(tagsVP);

        HTML tagsLabel = new HTML("Tags");
        tagsLabel.setFontSize(12);

        CenteringPanel tagsCP = new CenteringPanel(Axis.BOTH);
        tagsCP.setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM,
                ListGridHeader.HEADER_COLOUR_LIGHT, ListGridHeader.HEADER_COLOUR_DARK));
        tagsCP.setHeight(22);
        tagsCP.setWidth100();
        tagsCP.add(tagsLabel);
        tagsVP.add(tagsCP);

        TagGrid tagsGrid = new TagGrid(o, ScrollPolicy.AUTO);
        tagsGrid.fitToParent();
        tagsVP.add(tagsGrid);

        VerticalPanel termsVP = new VerticalPanel();
        termsVP.setPreferredWidth(0.3);
        termsVP.setHeight100();
        _hsp.add(termsVP);

        HTML termsLabel = new HTML("Available Tags");
        termsLabel.setFontSize(12);

        CenteringPanel termsCP = new CenteringPanel(Axis.BOTH);
        termsCP.setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM,
                ListGridHeader.HEADER_COLOUR_LIGHT, ListGridHeader.HEADER_COLOUR_DARK));
        termsCP.setHeight(22);
        termsCP.setWidth100();
        termsCP.add(termsLabel);
        termsVP.add(termsCP);

        TermGrid termsGrid = new TermGrid(TagDictionary.tagDictionaryFor(o), projectAdmin) {
            protected String titleForTerm() {
                return "name";
            }

            protected String titleForDefinition() {
                return "description";
            }
        };
        termsGrid.fitToParent();
        termsVP.add(termsGrid);

    }

    @Override
    public Widget gui() {

        return _hsp;
    }

}
