package daris.client.ui.form.item;

import arc.gui.form.Field;
import arc.gui.form.Form;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.form.FormItemFactory;
import arc.gui.gwt.form.item.FormItemFocusListener;
import arc.gui.gwt.form.item.FormItemStyle;
import arc.gui.gwt.form.item.FormSubmitOnEnter;
import arc.gui.gwt.widget.input.TextBox;
import arc.mf.client.util.ObjectUtil;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;

public class CiteableIdTypeFormItem implements arc.gui.gwt.form.item.FormItem {

    @SuppressWarnings("rawtypes")
    @Override
    public Widget create(Form form, final Field f, final FormItemFocusListener fl, FormSubmitOnEnter fse) {
        final TextBox text = new TextBox() {
            public void onAttach() {
                super.onAttach();

                if (f.focus()) {
                    super.setFocus(true);
                }
            }
        };

        FormItemStyle.applyReadWriteTo(text);

        text.setEnabled(f.enabled());
        text.setVisible(f.visible());

        if (f.value() != null) {
            text.setValue(f.value().toString());
        }

        text.setWidth(20 * 10);

        if (fl != null) {
            text.addFocusHandler(new FocusHandler() {
                @Override
                public void onFocus(FocusEvent event) {
                    fl.focusOn(f);
                }
            });

            text.addBlurHandler(new BlurHandler() {
                @Override
                public void onBlur(BlurEvent event) {
                    fl.focusOff(f);
                }
            });
        }

        text.addValueChangeHandler(new ValueChangeHandler<String>() {
            @SuppressWarnings("unchecked")
            public void onValueChange(ValueChangeEvent<String> event) {
                String v = event.getValue();
                if (v == null || v.equals("")) {
                    f.setValue(null);
                } else {
                    if (arc.mf.dtype.CiteableId.isValid(v)) {
                        f.setValue(v);
                    } else {
                        f.markInvalid("Invalid citeable id:" + v);
                    }
                }
            }
        });

        // Enable value monitoring so that typing will fire change listeners.
        text.enableValueMonitoring();

        form.addRenderListener(f, new FormItemListener() {
            public void itemPropertyChanged(arc.gui.form.FormItem f, arc.gui.form.FormItem.Property property) {

                switch (property) {
                case VISIBILITY:
                    text.setVisible(f.visible());
                    break;

                case ENABLED:
                    text.setEnabled(f.enabled());
                    FormItemFactory.updateStyle(text, f);
                    break;

                case FOCUS:
                    text.setFocus(f.focus());
                    break;

                case SELECT:
                    text.selectAll();
                    break;
                default:
                    break;
                }
            }

            public void itemValueChanged(arc.gui.form.FormItem f) {
                if (!ObjectUtil.equals(f.value(), text.value())) {
                    if (!text.monitoredValueChanged()) {
                        text.setValue(f.valueAsString());
                    }
                }
            }

        });

        FormSubmitOnEnter.apply(text, fse);

        return text;
    }

}
