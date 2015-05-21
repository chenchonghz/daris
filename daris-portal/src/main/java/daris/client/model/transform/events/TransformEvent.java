package daris.client.model.transform.events;

import arc.mf.client.util.ObjectUtil;
import arc.mf.event.Filter;
import arc.mf.event.SystemEvent;
import daris.client.model.transform.TransformRef;

public class TransformEvent extends SystemEvent {

	public enum Action {
		CREATE, UPDATE, DESTROY;
	}

	public static final String SYSTEM_EVENT_NAME = "transform";

	private TransformRef _o;
	private Action _action;

	public TransformEvent(TransformRef o, Action action) {

		super(SYSTEM_EVENT_NAME, Long.toString(o.uid()));
		_o = o;
		_action = action;
	}

	public Action action() {
		return _action;
	}

	public TransformRef objectRef() {
		return _o;
	}

	@Override
	public boolean matches(Filter f) {

		if (!type().equals(f.type())) {
			return false;
		}

		if (f.object() != null) {
			if(!ObjectUtil.equals(object(), f.object())){
				return false;
			}
		}
		return true;
	}

	public String toString() {
		return _action + ": " + _o.uid();
	}

}