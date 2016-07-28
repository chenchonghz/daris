package daris.client.ui.dicom.action;

import com.google.gwt.user.client.ui.Widget;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import daris.client.Resource;
import daris.client.model.dicom.sr.StructuredReportRef;
import daris.client.model.dicom.sr.messages.StructuredReportExport;
import daris.client.model.dicom.sr.messages.StructuredReportExport.Format;

public class DicomSRExportForm extends ValidatedInterfaceComponent implements AsynchronousAction {

	private StructuredReportExport _msg;

	private VerticalPanel _vp;
	private Form _form;
	private HTML _status;

	public DicomSRExportForm(StructuredReportRef sr) {
		_msg = new StructuredReportExport(sr);

		_vp = new VerticalPanel();
		_vp.fitToParent();

		_form = new Form();
		_form.setPadding(25);
		_form.setSpacing(20);
		_form.setShowDescriptions(false);
		_form.setShowHelp(false);

		Field<StructuredReportExport.Format> format = new Field<StructuredReportExport.Format>("Format",
				new EnumerationType<StructuredReportExport.Format>(StructuredReportExport.Format.values()), null, 1, 1);
		format.setInitialValue(_msg.format(), false);
		format.addListener(new FormItemListener<StructuredReportExport.Format>() {

			@Override
			public void itemValueChanged(FormItem<Format> f) {
				_msg.setFormat(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<Format> f, Property property) {

			}
		});
		_form.add(format);

		StringType stringType = new StringType(1, 255, 32);
		stringType.setPattern("^[\\w\\-. ]+$");
		Field<String> outputFileName = new Field<String>("Output File Name", stringType, null, 1, 1);
		outputFileName.setInitialValue(_msg.outputFileName(), false);
		outputFileName.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {
				_msg.setOutputFileName(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f, Property property) {

			}
		});
		_form.add(outputFileName);
		_form.render();
		addMustBeValid(_form);
		_vp.add(_form);

		_status = new HTML();
		_status.setHeight(22);
		_status.setWidth100();
		_status.setPaddingLeft(15);
		_status.setPaddingRight(15);
		_status.setFontSize(11);
		_status.setColour(RGB.RED);

		_vp.add(_status);

		addChangeListener(new StateChangeListener() {

			@Override
			public void notifyOfChangeInState() {
				Validity v = valid();
				if (v.valid()) {
					_status.setHTML(null);
				} else {
					_status.setHTML("<table width=\"100%\"><tr><td align=\"right\"><img src=\""
							+ Resource.INSTANCE.exclamation16().getSafeUri().asString() + "\"></td><td align=\"left\">"
							+ v.reasonForIssue() + "</td></tr></table>");
				}
			}
		});

	}

	@Override
	public Widget gui() {
		return _vp;
	}

	public BaseWidget widget() {
		return _vp;
	}

	@Override
	public void execute(final ActionListener l) {
		_msg.send(new ObjectMessageResponse<Null>() {

			@Override
			public void responded(Null r) {
				l.executed(r != null);
			}
		});

	}

}
