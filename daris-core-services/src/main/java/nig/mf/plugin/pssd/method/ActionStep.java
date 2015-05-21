package nig.mf.plugin.pssd.method;

import arc.xml.*;
import java.util.*;

/**
 * Action step does something -- an activity generating some form of data Action
 * steps may generate Studies or Subject state meta-data or
 * 
 * @author Jason Lohrey
 * 
 */
public class ActionStep extends Step {
	private List<XmlDoc.Element> _subjectActions;
	private List<XmlDoc.Element> _studyActions;
	private List<XmlDoc.Element> _transformActions;

	public ActionStep(int id) {
		super(id);

		_subjectActions = null;
		_studyActions = null;
		_transformActions = null;
	}

	public List<XmlDoc.Element> subjectActions() {
		return _subjectActions;
	}

	public List<XmlDoc.Element> studyActions() {
		return _studyActions;
	}

	public List<XmlDoc.Element> transformActions() {
		return _transformActions;
	}

	public void restoreStepBody(XmlDoc.Element se) throws Throwable {
		_subjectActions = se.elements("subject");
		_studyActions = se.elements("study");
		_transformActions = se.elements("transform");
	}

	public void saveStepBody(XmlWriter w) throws Throwable {
		if (_subjectActions != null) {
			for (XmlDoc.Element se : _subjectActions) {
				w.add(se);
			}
		}

		if (_studyActions != null) {
			for (XmlDoc.Element se : _studyActions) {
				w.add(se);
			}
		}
		
		if (_transformActions != null) {
			for (XmlDoc.Element te : _transformActions) {
				w.add(te);
			}
		}
	}

}
