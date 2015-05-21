package nig.mf.plugin.pssd;

import arc.xml.XmlWriter;

public class PSSDObjectEvent extends arc.event.Event {

	public static final String EVENT_TYPE = "pssd-object";

	public static class Filter implements arc.event.Filter {

		private String _id;

		public Filter(String id) {

			_id = id;
		}

		public Filter() {

			this(null);
		}

		public boolean accept(arc.event.Event e) {

			if (!(e instanceof PSSDObjectEvent)) {
				return false;
			}
			if (!(e.type().equals(PSSDObjectEvent.EVENT_TYPE))) {
				return false;
			}
			PSSDObjectEvent poe = (PSSDObjectEvent) e;
			if (_id != null) {
				return _id.equals(poe.id());
			} else {
				// if (poe.id() == null) {
				return true;
				// } else {
				// return false;
				// }
			}
		}
	}

	public enum Action {
		CREATE, MODIFY, MEMBERS, DESTROY
	}

	private Action _action;
	private String _id;
	private int _nbChildren;

	public PSSDObjectEvent(Action action, String id, int nbChildren) {

		super(EVENT_TYPE, true);
		_action = action;
		_id = id;
		_nbChildren = nbChildren;
	}

	public Action action() {

		return _action;
	}

	public String id() {

		return _id;
	}

	public int nbChildren() {
		return _nbChildren;
	}

	@Override
	public boolean equals(arc.event.Event e) {

		if (!super.equals(e)) {
			return false;
		}
		if (!(e instanceof PSSDObjectEvent)) {
			return false;
		}
		PSSDObjectEvent poe = (PSSDObjectEvent) e;
		return _action.equals(poe.action()) && _id.equals(poe.id()) && _nbChildren == poe.nbChildren();
	}

	@Override
	protected void saveState(XmlWriter w) throws Throwable {

		w.add("action", _action.toString());
		w.add("object", new String[] { "nbc", Integer.toString(_nbChildren) }, _id);
	}
}
