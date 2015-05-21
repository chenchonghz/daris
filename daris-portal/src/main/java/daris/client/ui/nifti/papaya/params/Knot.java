package daris.client.ui.nifti.papaya.params;

import com.google.gwt.core.client.JavaScriptObject;

public class Knot implements CanBeJSO {
    public final double distance;
    public final double red;
    public final double green;
    public final double blue;

    public Knot(double distance, double red, double green, double blue) {
        assert distance >= 0.0 && distance <= 1.0;
        assert red >= 0.0 && red <= 1.0;
        assert green >= 0.0 && green <= 1.0;
        assert blue >= 0.0 && blue <= 1.0;
        this.distance = distance;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public final JavaScriptObject asJSO() {
        return makeJSO(this.distance, this.red, this.green, this.blue);
    }

    private final native JavaScriptObject makeJSO(double distance, double red, double green, double blue) /*-{
		return [ distance, red, green, blue ];
    }-*/;
}