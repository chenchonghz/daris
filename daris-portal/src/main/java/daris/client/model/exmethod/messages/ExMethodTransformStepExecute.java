package daris.client.model.exmethod.messages;

import java.util.List;

import daris.client.model.exmethod.ExMethodTransformStep;
import daris.client.model.transform.Transform;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ExMethodTransformStepExecute extends ObjectMessage<List<String>> {

	private ExMethodTransformStep _step;
	private boolean _iterate;
	private List<Transform.Parameter> _params;

	public ExMethodTransformStepExecute(ExMethodTransformStep step, boolean iterate, List<Transform.Parameter> params) {
		_step = step;
		_iterate = iterate;
		_params = params;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("id", _step.exMethodId());
		w.add("step", _step.stepPath());
		if (_iterate) {
			w.add("iterate", _iterate);
		}
		if (_params != null) {
			for (Transform.Parameter param : _params) {
				w.add("parameter", new String[] { "name", param.name() }, param.value());
			}
		}
	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.ex-method.transform.step.execute";
	}

	@Override
	protected List<String> instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			return xe.values("tuid");
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return "Transform Step";
	}

	@Override
	protected String idToString() {
		return _step.exMethodId() + "_" + _step.stepPath();
	}

}
