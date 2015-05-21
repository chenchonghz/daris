package daris.client.ui.nifti.papaya.params;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class Params implements CanBeJSO {

    private boolean _worldSpace = true;
    private boolean _showOrientation = true;
    private boolean _smoothDisplay = true;
    private boolean _orthogonal = true;
    private MainView _mainView = MainView.axial;
    private boolean _koiskMode = false;
    private boolean _fullScreenPadding = true;
    private boolean _expandable = true;
    private Coordinate _coordinate = null;
    private boolean _canOpenInMango = false;
    private String _atlas = null;
    private List<String> _images = null;
    private List<Lut> _luts = null;

    public Params(String atlas, List<String> images, List<Lut> luts, boolean canOpenInMango, Coordinate coordinate,
            boolean expandable, boolean fullScreenPadding, boolean koiskMode, MainView mainView, boolean orthogonal,
            boolean smoothDisplay, boolean showOrientation, boolean worldSpace) {
        _atlas = atlas;
        _images = images;
        _luts = luts;
        _canOpenInMango = canOpenInMango;
        _coordinate = coordinate;
        _expandable = expandable;
        _fullScreenPadding = fullScreenPadding;
        _koiskMode = koiskMode;
        _mainView = mainView;
        _orthogonal = orthogonal;
        _smoothDisplay = smoothDisplay;
        _showOrientation = showOrientation;
        _worldSpace = worldSpace;
    }

    public Params() {
        this(null, null, null, false, null, true, true, false, MainView.axial, true, true, true, true);
    }

    public Params setAtlas(String atlas) {
        _atlas = atlas;
        return this;
    }

    public List<String> images() {
        return _images;
    }

    public boolean hasImages() {
        return _images != null && !_images.isEmpty();
    }

    public Params setImages(List<String> images) {
        _images = images;
        return this;
    }

    public Params addImage(String image) {
        if (image == null) {
            return this;
        }
        if (_images == null) {
            _images = new ArrayList<String>();
        }
        _images.add(image);
        return this;
    }

    public Params setLuts(List<Lut> luts) {
        _luts = luts;
        return this;
    }

    public Params setCanOpenInMango(boolean canOpenInMango) {
        _canOpenInMango = canOpenInMango;
        return this;
    }

    public Params setCoordinate(Coordinate coordinate) {
        _coordinate = coordinate;
        return this;
    }

    public Params setExpandable(boolean expandable) {
        _expandable = expandable;
        return this;
    }

    public Params setFullScreenPadding(boolean fullScreenPadding) {
        _fullScreenPadding = fullScreenPadding;
        return this;
    }

    public Params setKoiskMode(boolean koiskMode) {
        _koiskMode = koiskMode;
        return this;
    }

    public Params setMainView(MainView mainView) {
        _mainView = mainView;
        return this;
    }

    public Params setOrthogonal(boolean orthogonal) {
        _orthogonal = orthogonal;
        return this;
    }

    public Params setSmoothDisplay(boolean smoothDisplay) {
        _smoothDisplay = smoothDisplay;
        return this;
    }

    public Params setShowOrientation(boolean showOrientation) {
        _showOrientation = showOrientation;
        return this;
    }

    public Params setWorldSpace(boolean worldSpace) {
        _worldSpace = worldSpace;
        return this;
    }

    @Override
    public JavaScriptObject asJSO() {
        return makeJSO(_atlas, toJsArrayString(_images), toJsArray(_luts), _canOpenInMango, _coordinate == null ? null
                : _coordinate.asJSO(), _expandable, _fullScreenPadding, _koiskMode, _mainView.name(), _orthogonal,
                _smoothDisplay, _showOrientation, _worldSpace);
    }

    private static JsArrayString toJsArrayString(List<String> list) {
        JsArrayString array = null;
        if (list != null && !list.isEmpty()) {
            array = getNativeArray();
            for (String e : list) {
                array.push(e);
            }
        }
        return array;
    }

    private static native JsArrayString getNativeArray() /*-{
		return [];
    }-*/;

    private static <T extends CanBeJSO> JsArray<JavaScriptObject> toJsArray(List<T> list) {
        JsArray<JavaScriptObject> array = null;
        if (list != null && !list.isEmpty()) {
            array = JavaScriptObject.createArray().cast();
            for (T e : list) {
                array.push(e.asJSO());
            }
        }
        return array;
    }

    private final native JavaScriptObject makeJSO(String atlas, JsArrayString images, JsArray<JavaScriptObject> luts,
            boolean canOpenInMango, JavaScriptObject coordinate, boolean expandable, boolean fullScreenPadding,
            boolean koiskMode, String mainView, boolean orthogonal, boolean smoothDisplay, boolean showOrientation,
            boolean worldSpace) /*-{
		var params = [];
		if (atlas) {
			params['atlas'] = atlas;
		}
		if (images) {
			params['images'] = images;
		}
		if (luts) {
			params['luts'] = luts;
		}
		params['canOpenInMango'] = canOpenInMango;
		if (coordinate) {
			params['coordinate'] = coordinate;
		}
		params['expandable'] = expandable;
		params['fullScreenPadding'] = fullScreenPadding;
		params['koiskMode'] = koiskMode;
		if (mainView) {
			params['mainView'] = mainView;
		}
		params['orthogonal'] = orthogonal;
		params['smoothDisplay'] = smoothDisplay;
		params['showOrientation'] = showOrientation;
		params['worldSpace'] = worldSpace;
		return params;
    }-*/;

}
