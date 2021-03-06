package daris.client.ui.dataset;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.Frame;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.LongType;
import arc.mf.dtype.StringType;
import daris.client.model.dataset.DataSet;
import daris.client.model.dataset.DataSet.Transform;
import daris.client.model.dataset.DerivedDataSet;
import daris.client.model.dataset.DicomDataSet;
import daris.client.model.dataset.PrimaryDataSet;
import daris.client.model.dataset.SourceType;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.MimeTypes;
import daris.client.ui.archive.ArchiveExplorer;
import daris.client.ui.dicom.DicomStructuredReportViewer;
import daris.client.ui.object.DObjectDetails;
import daris.client.ui.object.DataContentFieldGroup;

public class DataSetDetails extends DObjectDetails {

	public static final String TAB_NAME_DICOM3D = "DICOM View(Papaya)";
	public static final String TAB_DESC_DICOM3D = "Papaya DICOM Viewer";
	public static final String TAB_NAME_DICOM2D = "DICOM View(Simple 2D)";
	public static final String TAB_DESC_DICOM2D = "Simple 2D DICOM Viewer";
	public static final String TAB_NAME_DICOMSR = "DICOM Structured Report";
	public static final String TAB_DESC_DICOMSR = "DICOM Structured Report";

	public static final String TAB_NAME_NIFTI = "NIFTI";
	public static final String TAB_DESC_NIFTI = "NIFTI";
	public static final String TAB_NAME_CONTENT = "Content";
	public static final String TAB_DESC_CONTENT = "Content";

	public DataSetDetails(DObjectRef po, DataSet o, FormEditMode mode) {

		super(po, o, mode);

		updateDicomTab();

		updateNiftiTab();

		updateContentTab();

		setActiveTab();
	}

	private void updateDicomTab() {

		if (mode() != FormEditMode.READ_ONLY) {
			return;
		}
		DataSet ds = (DataSet) object();
		if (ds instanceof DicomDataSet) {
			if (((DicomDataSet) ds).containsViewableImage()) {
				// 3d viewer tab
				SimplePanel sp3d = new SimplePanel();
				final Frame frame3d = new Frame();
				frame3d.getElement().getStyle().setBorderWidth(0, Unit.PX);
				frame3d.setSize("100%", "100%");
				sp3d.setContent(frame3d);
				sp3d.fitToParent();
				sp3d.addAttachHandler(new Handler() {
					@Override
					public void onAttachOrDetach(AttachEvent event) {
						// load dicom image until the sp is attached (tab is
						// activated).
						if (event.isAttached() && (frame3d.getUrl() == null || frame3d.getUrl().isEmpty())) {
							frame3d.setUrl(((DicomDataSet) object()).papayaViewerUrl());
						}
					}
				});
				setTab(TAB_NAME_DICOM3D, TAB_DESC_DICOM3D, sp3d);
			}
			if ("SR".equalsIgnoreCase(((DicomDataSet) ds).modality())) {
				// dicom structured report viewer (dose report)
				DicomStructuredReportViewer srView = new DicomStructuredReportViewer((DicomDataSet) ds);
				setTab(TAB_NAME_DICOMSR, TAB_DESC_DICOMSR, srView.widget());
			}
			// 2d viewer tab
			SimplePanel sp2d = new SimplePanel();
			final Frame frame2d = new Frame();
			frame2d.getElement().getStyle().setBorderWidth(0, Unit.PX);
			frame2d.setSize("100%", "100%");
			sp2d.setContent(frame2d);
			sp2d.fitToParent();
			sp2d.addAttachHandler(new Handler() {
				@Override
				public void onAttachOrDetach(AttachEvent event) {
					// load dicom image until the sp is attached (tab is
					// activated).
					if (event.isAttached() && (frame2d.getUrl() == null || frame2d.getUrl().isEmpty())) {
						frame2d.setUrl(((DicomDataSet) object()).simpleViewerUrl());
					}
				}
			});
			setTab(TAB_NAME_DICOM2D, TAB_DESC_DICOM2D, sp2d);

		}
	}

	private void updateNiftiTab() {

		if (mode() != FormEditMode.READ_ONLY) {
			return;
		}
		final DataSet ds = (DataSet) object();
		if (ds.data() != null && (MimeTypes.NIFTI_SERIES.equals(ds.mimeType())
				|| MimeTypes.NIFTI_SERIES.equals(ds.data().logicalMimeType()))) {
			String niftiViewerUrl = DataSet.niftiViewerUrl(ds);
			Frame frame = new Frame(niftiViewerUrl);
			frame.getElement().getStyle().setBorderWidth(0, Unit.PX);
			frame.setSize("100%", "100%");
			SimplePanel sp = new SimplePanel();
			sp.fitToParent();
			sp.setContent(frame);
			setTab(TAB_NAME_NIFTI, TAB_DESC_NIFTI, sp);
			// @formatter:off
			// TODO: we should include analyzer in the pssd/daris all-in-one
			// package so that we do not have to maintain the package
			// dependencies. It is annoying.
			/*
			 * Session.execute("package.exists",
			 * "<package>nig-analyzer</package>", 0, new
			 * ServiceResponseHandler() {
			 * 
			 * @Override public void processResponse(XmlElement xe, List<Output>
			 * outputs) throws Throwable { boolean exists =
			 * xe.booleanValue("exists", false); if (exists) {
			 * setTab(TAB_NAME_NIFTI, TAB_DESC_NIFTI, new NiftiSeriesViewer(ds,
			 * false) .widget()); } else { setTab(TAB_NAME_NIFTI,
			 * TAB_DESC_NIFTI, new NiftiSeriesViewer(ds, true) .widget()); } }
			 * });
			 */
			// @formatter:on
		}
	}

	private void updateContentTab() {

		if (mode() != FormEditMode.READ_ONLY) {
			return;
		}
		if (isContentArchiveSupported(object())) {
			setTab(TAB_NAME_CONTENT, TAB_DESC_CONTENT, new ArchiveExplorer(object()).widget());
		}
	}

	@Override
	protected void addToInterfaceForm(Form interfaceForm) {

		super.addToInterfaceForm(interfaceForm);

		DataSet dso = (DataSet) object();
		Field<String> mimeTypeField = new Field<String>(
				new FieldDefinition("type", StringType.DEFAULT, "MIME Type", null, 1, 1));
		mimeTypeField.setValue(dso.mimeType());
		interfaceForm.add(mimeTypeField);

		FieldGroup fg = new FieldGroup(new FieldDefinition("source", ConstantType.DEFAULT, null, null, 1, 1));
		Field<SourceType> sourceTypeField = new Field<SourceType>(
				new FieldDefinition("type", StringType.DEFAULT, "Source Type", null, 1, 1));
		sourceTypeField.setValue(dso.sourceType());
		fg.add(sourceTypeField);
		interfaceForm.add(fg);
		Field<String> vidField = new Field<String>(new FieldDefinition("vid", StringType.DEFAULT, null, null, 1, 1));
		vidField.setValue(dso.vid());
		interfaceForm.add(vidField);
		if (dso instanceof PrimaryDataSet) {
			FieldGroup fgAcquisition = new FieldGroup(
					new FieldDefinition("acquisition", ConstantType.DEFAULT, null, null, 1, 1));
			FieldGroup fgSubject = new FieldGroup(
					new FieldDefinition("subject", ConstantType.DEFAULT, null, null, 1, 1));
			Field<String> subjectIdField = new Field<String>(
					new FieldDefinition("id", StringType.DEFAULT, null, null, 1, 1));
			subjectIdField.setValue(((PrimaryDataSet) dso).subjectId());
			fgSubject.add(subjectIdField);
			Field<String> subjectStateField = new Field<String>(
					new FieldDefinition("state", StringType.DEFAULT, null, null, 1, 1));
			subjectStateField.setValue(((PrimaryDataSet) dso).subjectState());
			fgSubject.add(subjectStateField);
			fgAcquisition.add(fgSubject);
			interfaceForm.add(fgAcquisition);
		}
		if (dso instanceof DerivedDataSet) {
			DerivedDataSet ddso = (DerivedDataSet) dso;
			FieldGroup fgDerivation = new FieldGroup(
					new FieldDefinition("derivation", ConstantType.DEFAULT, null, null, 1, 1));
			Field<Boolean> processedField = new Field<Boolean>(
					new FieldDefinition("processed", BooleanType.DEFAULT_TRUE_FALSE, null, null, 0, 1));
			processedField.setValue(ddso.processed());
			fgDerivation.add(processedField);
			//
			Field<Boolean> anonymizedField = new Field<Boolean>(
					new FieldDefinition("anonymized", BooleanType.DEFAULT_TRUE_FALSE, null, null, 0, 1));
			anonymizedField.setValue(ddso.anonymized());
			fgDerivation.add(anonymizedField);

			List<DerivedDataSet.Input> inputs = ddso.inputs();
			if (inputs != null && !inputs.isEmpty()) {
				for (DerivedDataSet.Input input : inputs) {
					FieldGroup fgInput = new FieldGroup(
							new FieldDefinition("input", ConstantType.DEFAULT, null, null, 1, 1));
					Field<String> inputVidField = new Field<String>(
							new FieldDefinition("vid", ConstantType.DEFAULT, null, null, 1, 1));
					inputVidField.setInitialValue(input.vid(), false);
					Field<String> inputIdField = new Field<String>(
							new FieldDefinition(null, ConstantType.DEFAULT, null, null, 1, 1));
					inputIdField.setInitialValue(input.id(), false);
					fgInput.add(inputVidField);
					fgInput.add(inputIdField);
					fgDerivation.add(fgInput);
				}
			}
			FieldGroup fgMethod = new FieldGroup(new FieldDefinition("method", ConstantType.DEFAULT, null, null, 1, 1));
			Field<String> methodIdField = new Field<String>(
					new FieldDefinition("id", StringType.DEFAULT, "Method id", null, 1, 1));
			methodIdField.setValue(((DerivedDataSet) dso).methodId());
			fgMethod.add(methodIdField);
			Field<String> methodStepField = new Field<String>(
					new FieldDefinition("step", StringType.DEFAULT, "Method step", null, 1, 1));
			methodStepField.setValue(((DerivedDataSet) dso).methodStep());
			fgMethod.add(methodStepField);
			fgDerivation.add(fgMethod);
			interfaceForm.add(fgDerivation);
		}
		if (dso.data() != null) {
			interfaceForm.add(DataContentFieldGroup.fieldGroupFor(dso.data()));
		}
		if (dso.transform() != null) {

			FieldGroup fgTransform = new FieldGroup(
					new FieldDefinition("transform", DocType.DEFAULT, null, null, 0, 1));
			Field<String> midField = new Field<String>(new FieldDefinition("mid", StringType.DEFAULT,
					"The id of the method.", "The id of the method.", 0, 1));
			midField.setValue(dso.transform().mid());
			fgTransform.add(midField);
			Field<Long> tuidField = new Field<Long>(new FieldDefinition("tuid", LongType.POSITIVE,
					"The unique id of the transform.", "The unique id of the transform.", 0, 1));
			tuidField.setValue(dso.transform().tuid());
			fgTransform.add(tuidField);
			if (dso.transform().software() != null) {
				for (Transform.Software s : dso.transform().software()) {
					FieldGroup fgSoftware = new FieldGroup(
							new FieldDefinition("software", DocType.DEFAULT, null, null, 0, 1));
					// software name
					Field<String> snameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT,
							"The name of the software.", "The name of the software.", 1, 1));
					snameField.setValue(s.name());
					fgSoftware.add(snameField);

					// software version
					Field<String> sversionField = new Field<String>(new FieldDefinition("version", StringType.DEFAULT,
							"The version of the software.", "The version of the software.", 0, 1));
					sversionField.setValue(s.version());
					fgSoftware.add(sversionField);

					// software commands
					if (s.commands() != null) {
						for (Transform.Software.Command cmd : s.commands()) {
							FieldGroup fgCommand = new FieldGroup(new FieldDefinition("command", DocType.DEFAULT,
									"The command used to perform the transform.",
									"The command used to perform the transform.", 0, 1));

							// command name
							Field<String> cnameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT,
									"The name of the command.", "The name of the command.", 1, 1));
							cnameField.setValue(cmd.name());
							fgCommand.add(cnameField);

							// command arguments
							if (cmd.hasArgs()) {
								for (String argName : cmd.args().keySet()) {
									FieldGroup fgArgument = new FieldGroup(new FieldDefinition("argument",
											DocType.DEFAULT, "The argument for the command.",
											"The argument for the command.", 0, Integer.MAX_VALUE));

									// argument name
									Field<String> cargNameField = new Field<String>(
											new FieldDefinition("name", StringType.DEFAULT, "The name of argument.",
													"The name of the argument.", 1, 1));
									cargNameField.setValue(argName);
									fgArgument.add(cargNameField);

									// argument value
									Field<String> cargValueField = new Field<String>(
											new FieldDefinition("value", StringType.DEFAULT, "The value of argument.",
													"The value of the argument.", 1, 1));
									cargValueField.setValue(cmd.argValue(argName));
									fgArgument.add(cargValueField);
									fgCommand.add(fgArgument);
								}
								fgSoftware.add(fgCommand);
							}
						}
						fgTransform.add(fgSoftware);
					}
				}
			}
			interfaceForm.add(fgTransform);
		}
	}

	private static boolean isContentArchiveSupported(DObject o) {
		if (!(o instanceof DataSet)) {
			return false;
		}
		DataSet ds = (DataSet) o;
		if (ds.data() == null || ds.data().mimeType() == null) {
			return false;
		}
		String type = ds.data().mimeType();
		return type.equals("application/arc-archive") || type.equals("application/zip")
				|| type.equals("application/x-zip") || type.equals("application/x-zip-compressed")
				|| type.equals("application/zip") || type.equals("application/java-archive")
				|| type.equals("application/x-tar");
	}
}
