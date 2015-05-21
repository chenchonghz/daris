package daris.client.ui.nifti.papaya;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.Div;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.ResizeListener;
import arc.mf.client.util.ActionListener;
import arc.mf.session.Session;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.Widget;

import daris.client.ui.nifti.papaya.params.Params;

public class PapayaViewer implements InterfaceComponent {

    public static final String PAPAYA_JS = "daris/papaya/papaya.js";
    public static final String PAPAYA_CSS = "daris/papaya/papaya.css";

    public static final String PARENT_ID_PREFIX = "daris-papaya-parent-";
    public static final String PAPAYA_CLASS = "papaya";

    private static int _pid = 0;

    private Params _params;
    private RatioPanel _root;
    private Div _containerDiv;
    private JavaScriptObject _containerJSO;
    private JavaScriptObject _viewerJSO;

    public PapayaViewer(Params params) {
        _params = params == null ? new Params() : params;
        _root = new RatioPanel(1.25);
        _root.fitToParent();
        _root.getElement().setId(PARENT_ID_PREFIX + (_pid++));
        _root.addResizeListener(new ResizeListener() {

            @Override
            public Widget widget() {
                return null;
            }

            @Override
            public void resized(long w, long h) {
                resizeComponents();
            }
        });
        _containerDiv = new Div() {
            protected void onDetach() {
                super.onDetach();
                destroyViewer(_containerJSO);
                _containerJSO = null;
            }
        };
        _containerDiv.fitToParent();
        _containerDiv.getElement().addClassName(PAPAYA_CLASS);
        _root.setContent(_containerDiv);
        loadPapayaCSS();
        loadPapayaJS(new ActionListener(){

            @Override
            public void executed(boolean succeeded) {
                if(succeeded){
                    _containerJSO = createViewer(_root.getElement(), _containerDiv.getElement(), _params.asJSO());            
                } else {
                    _root.setContent(new HTML("Failed to load " + PAPAYA_JS));
                }
            }});
        
    }

    private static void loadPapayaCSS() {
        if (isLinkLoaded(PAPAYA_CSS)) {
            return;
        }
        LinkElement link = Document.get().createLinkElement();
        link.setHref(PAPAYA_CSS);
        link.setType("text/css");
        link.setRel("stylesheet");
        Document.get().getElementsByTagName("head").getItem(0).appendChild(link);
    }

    private static boolean isLinkLoaded(String url) {
        Element head = Document.get().getElementsByTagName("head").getItem(0);
        NodeList<Element> links = head.getElementsByTagName("link");
        for (int i = 0; i < links.getLength(); i++) {
            Element link = links.getItem(i);
            String href = link.getAttribute("href");
            if (href != null && href.equals(url)) {
                return true;
            }
        }
        return false;
    }

    private static void loadPapayaJS(final ActionListener al) {
        if (isScriptLoaded(PAPAYA_JS)) {
            if (al != null) {
                al.executed(true);
            }
            return;
        }
        ScriptInjector.fromUrl(PAPAYA_JS).setWindow(ScriptInjector.TOP_WINDOW).setRemoveTag(false)
                .setCallback(new Callback<Void, Exception>() {

                    @Override
                    public void onFailure(Exception reason) {
                        if (al != null) {
                            al.executed(false);
                        }
                        Session.displayError("Error loading " + PAPAYA_JS, reason);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        if (al != null) {
                            al.executed(true);
                        }
                    }
                }).inject();
    }

    private static boolean isScriptLoaded(String url) {
        Element head = Document.get().getElementsByTagName("head").getItem(0);
        NodeList<Element> scripts = head.getElementsByTagName("script");
        for (int i = 0; i < scripts.getLength(); i++) {
            Element script = scripts.getItem(i);
            String src = script.getAttribute("src");
            if (src != null && src.equals(url)) {
                return true;
            }
        }
        return false;
    }

    private static native JavaScriptObject createViewer(com.google.gwt.dom.client.Element parentElement,
            com.google.gwt.dom.client.Element containerElement, JavaScriptObject params)/*-{

		// convert dom element to jquery object
		var parentHtml = $wnd.$(parentElement);

		// disable click event for the parent element
		parentHtml[0].onclick = '';
		parentHtml.off('click');

		// convert dom element to jquery object
		var containerHtml = $wnd.$(containerElement);

		// fill container 
		containerHtml.attr("id", $wnd.PAPAYA_DEFAULT_CONTAINER_ID
				+ $wnd.papayaContainers.length);

		if (!params || (params.kioskMode === undefined) || !params.kioskMode) {
			containerHtml
					.append("<div id='"
							+ ($wnd.PAPAYA_DEFAULT_TOOLBAR_ID + $wnd.papayaContainers.length)
							+ "' class='" + $wnd.PAPAYA_TOOLBAR_CSS
							+ "'></div>");
		}

		containerHtml
				.append("<div id='"
						+ ($wnd.PAPAYA_DEFAULT_VIEWER_ID + $wnd.papayaContainers.length)
						+ "' class='" + $wnd.PAPAYA_VIEWER_CSS + "'></div>");
		containerHtml
				.append("<div id='"
						+ ($wnd.PAPAYA_DEFAULT_DISPLAY_ID + $wnd.papayaContainers.length)
						+ "' class='" + $wnd.PAPAYA_DISPLAY_CSS + "'></div>");

		if (params && (params.orthogonal !== undefined) && !params.orthogonal) {
			if ($wnd.isInputRangeSupported()) {
				containerHtml
						.append("<div id='"
								+ ($wnd.PAPAYA_DEFAULT_SLIDER_ID + $wnd.papayaContainers.length)
								+ "' class='" + $wnd.PAPAYA_SLIDER_CSS
								+ "'><input type='range' /></div>");
			}
		}

		var viewerHtml = containerHtml.find("." + $wnd.PAPAYA_VIEWER_CSS);
		$wnd.removeCheckForJSClasses(containerHtml, viewerHtml);

		// build container
		var container = null;
		var error = $wnd.checkForBrowserCompatibility();
		if (error !== null) {
			containerHtml.addClass($wnd.PAPAYA_UTILS_UNSUPPORTED_CSS);
			viewerHtml.addClass($wnd.PAPAYA_UTILS_UNSUPPORTED_MESSAGE_CSS);
			viewerHtml.html(error);
			return null;
		}
		container = new $wnd.papaya.Container(containerHtml);
		container.containerIndex = $wnd.papayaContainers.length;
		container.preferences = new $wnd.papaya.viewer.Preferences();

		if (params) {
			container.params = $wnd.$.extend(container.params, params);
		}
		container.nestedViewer = true;
		container.kioskMode = (container.params.kioskMode === true);

		if (container.params.fullScreenPadding !== undefined) { // default is true
			container.fullScreenPadding = container.params.fullScreenPadding;
		}

		if (container.params.orthogonal !== undefined) { // default is true
			container.orthogonal = container.params.orthogonal;
		}
		container.buildViewer(container.params);
		container.buildDisplay();
		container.buildToolbar();

		if (!container.orthogonal) {
			container.buildSliderControl();
		}

		container.setUpDnD();

		if (container.params.images) {
			container.viewer.loadImage(container.params.images[0], true, false);
		}
		container.resizeViewerComponents(false);

		$wnd.papayaContainers.push(container);

		return container;
    }-*/;

    private static native void destroyViewer(JavaScriptObject container) /*-{
		if (container && $wnd.papayaContainers) {
			var idx = $wnd.papayaContainers.indexOf(container);
			if (idx >= 0) {
				$wnd.papayaContainers.splice(idx, 1);
			}
			delete container;
		}
    }-*/;

    private static native void loadImage(JavaScriptObject container, String url) /*-{
		if (container && container.viewer) {
			container.viewer.loadImage(url, true, false)
			container.resizeViewerComponents(true);
		}
    }-*/;

    private static native void resizeViewerComponents(JavaScriptObject container) /*-{
		if (container) {
			container.resizeViewerComponents(true)
		}
    }-*/;

    public void resizeComponents() {
        resizeViewerComponents(_containerJSO);
    }

    public void loadImage(String url) {
        loadImage(_containerJSO, url);
    }

    @Override
    public Widget gui() {
        return _root;
    }

    public BaseWidget widget() {
        return _root;
    }

}
