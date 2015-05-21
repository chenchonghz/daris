package daris.client.ui.nifti.papaya.params;

import com.google.gwt.core.client.JavaScriptObject;

public class Coordinate implements CanBeJSO {
    public final int x;
    public final int y;
    public final int z;

    public Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public final JavaScriptObject asJSO() {
        return makeJSO(this.x, this.y, this.z);
    }

    private final native JavaScriptObject makeJSO(int x, int y, int z) /*-{
		return [ x, y, z ];
    }-*/;
}