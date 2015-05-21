package daris.client.ui.nifti.papaya.params;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class Lut implements CanBeJSO {
    public final String name;
    public final List<Knot> data;

    public Lut(String name, List<Knot> data) {
        this.name = name;
        this.data = data;
    }

    @Override
    public final JavaScriptObject asJSO() {

        JsArray<JavaScriptObject> array = null;
        if (this.data != null && !this.data.isEmpty()) {
            array = JavaScriptObject.createArray().cast();
            for (Knot knot : this.data) {
                array.push(knot.asJSO());
            }
        }
        return makeJSO(this.name, array);
    }

    private final native JavaScriptObject makeJSO(String name, JsArray<JavaScriptObject> data) /*-{
		return {
			'name' : name,
			'data' : data
		};
    }-*/;
}