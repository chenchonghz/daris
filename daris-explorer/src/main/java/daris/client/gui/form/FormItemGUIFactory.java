package daris.client.gui.form;

import arc.mf.dtype.DataType;
import javafx.scene.Node;

@SuppressWarnings("rawtypes")
public class FormItemGUIFactory {

    static Node createFormItemGUI(FormItem item) {
        DataType dataType = item.dataType();
        if (dataType instanceof arc.mf.dtype.StringType) {

        } else if (dataType instanceof arc.mf.dtype.TextType) {

        } else if (dataType instanceof arc.mf.dtype.EnumerationType) {

        }
        throw new UnsupportedOperationException(
                "Unsupported data type: " + dataType.name());
    }

}
