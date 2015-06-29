package daris.client.ui.exmethod;

import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.menu.MenuToolBar;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.xml.XmlElement;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;

import daris.client.Resource;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.exmethod.ExMethodStep;
import daris.client.model.exmethod.ExMethodStepRef;
import daris.client.model.object.DObjectRef;
import daris.client.ui.exmethod.graph.Graph;
import daris.client.ui.exmethod.graph.GraphListener;
import daris.client.ui.exmethod.graph.GraphWidget;
import daris.client.ui.exmethod.graph.Node;
import daris.client.ui.form.XmlMetaForm;
import daris.client.ui.object.DObjectDetails;

public class ExMethodDetails extends DObjectDetails {

	public static final String TAB_NAME_WORKFLOW = "Experimental Workflow";
	public static final String TAB_DESC_WORKFLOW = "Experimental Workflow";

	public static ImageResource ZOOM_IN_ICON = Resource.INSTANCE.zoomIn16();
	public static ImageResource ZOOM_OUT_ICON = Resource.INSTANCE.zoomOut16();

	public ExMethodDetails(DObjectRef po, ExMethod o, FormEditMode mode) {

		super(po, o, mode);

		updateWorkflowTab();

		setActiveTab();
	}

	protected void addToInterfaceForm(Form interfaceForm) {

		super.addToInterfaceForm(interfaceForm);

		final ExMethod emo = (ExMethod) object();
		XmlElement me = emo.methodElement();
		if (me != null && interfaceForm.editMode() == FormEditMode.READ_ONLY) {
			XmlMetaForm.addDocForView(interfaceForm, me);
			// List<XmlElement> es = new Vector<XmlElement>();
			// es.add(me);
			// XmlMetaForm.addToForm(interfaceForm, es);
		}
	}

	private void updateWorkflowTab() {

		final ExMethod em = (ExMethod) object();
		Graph graph = ExMethodGraph.graphFor(em);
		final GraphWidget gw = new GraphWidget(graph) {
			private boolean _initLayout = true;

			protected void doLayoutChildren() {
				if (_initLayout) {
					parent().fitToParent();
					_initLayout = false;
				}
				super.doLayoutChildren();
			}
		};
		// gw.fitToParent();

		// TODO: temp fix for zoom in/out.
		gw.setPreferredWidth(1024);
		gw.setPreferredHeight(600);
		// gw.setMinHeight(320);
		// gw.setWidth(480);
		// gw.setHeight(320);
		// gw.fitToParent();

		gw.addGraphListener(new GraphListener() {

			@Override
			public void select(Node node) {

			}

			@Override
			public void deselect(Node node) {

			}

			@Override
			public void open(Node node) {

				if (node.isAtomic()) {
					final MethodAndStep mas = node.data();
					ExMethodStepRef emsr = new ExMethodStepRef(mas.rootMethod()
							.id(), mas.rootMethod().proute(), mas.stepPath());
					emsr.resolve(new ObjectResolveHandler<ExMethodStep>() {

						@Override
						public void resolved(ExMethodStep ems) {

							if (ems != null) {
								// StepEditorDialog dlg = new
								// StepEditorDialog(mas, ems);
								// dlg.show();
								// new StepEditAction(mas, ems,
								// window()).execute();
								new ExMethodStepDialog(mas, ems).show(window());
							}
						}
					});
				}
			}
		});

		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();

		MenuToolBar toolBar = new MenuToolBar();
		toolBar.setHeight(25);
		toolBar.setWidth100();
		toolBar.setFontSize(11);
		toolBar.setBackgroundColour(new RGB(0xf0, 0xf0, 0xf0));
		toolBar.setPaddingTop(2);
		toolBar.setPaddingBottom(2);

		Button zoomInButton = new Button(
				"<div style=\"cursor: pointer\"><img width=\""
						+ ZOOM_IN_ICON.getWidth()
						+ "px\" height=\""
						+ ZOOM_IN_ICON.getHeight()
						+ "px\" src=\""
						+ ZOOM_IN_ICON.getSafeUri().asString()
						+ "\" style=\"vertical-align: top; padding-top: 0px;\"></img>&nbsp;Zoom in</div>",
				false);
		zoomInButton.setWidth(90);
		zoomInButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				gw.setWidth((int) (gw.width() * 1.1));
				gw.setHeight((int) (gw.height() * 1.1));
			}
		});
		toolBar.add(zoomInButton);

		Button zoomOutButton = new Button(
				"<div style=\"cursor: pointer\"><img width=\""
						+ ZOOM_OUT_ICON.getWidth()
						+ "px\" height=\""
						+ ZOOM_OUT_ICON.getHeight()
						+ "px\" src=\""
						+ ZOOM_OUT_ICON.getSafeUri().asString()
						+ "\" style=\"vertical-align: top; padding-top: 0px;\"></img>&nbsp;Zoom out</div>",
				false);
		zoomOutButton.setWidth(90);
		zoomOutButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				gw.setWidth((int) (gw.width() / 1.1));
				gw.setHeight((int) (gw.height() / 1.1));
			}
		});
		toolBar.add(zoomOutButton);

		vp.add(toolBar);

		vp.add(new ScrollPanel(gw, ScrollPolicy.AUTO));

		setTab(TAB_NAME_WORKFLOW, TAB_DESC_WORKFLOW, vp);
	}

}
