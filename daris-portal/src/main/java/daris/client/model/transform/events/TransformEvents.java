package daris.client.model.transform.events;

import arc.mf.client.xml.XmlElement;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventFactory;
import arc.mf.event.SystemEventRegistry;
import daris.client.model.transform.TransformRef;

public class TransformEvents {
	private static boolean _init = false;

	public static void initialize() {
		if (_init) {
			return;
		}

		SystemEventRegistry.add(TransformEvent.SYSTEM_EVENT_NAME, new SystemEventFactory() {
			public SystemEvent instantiate(String type, XmlElement ee) throws Throwable {
				long tuid = ee.longValue("tuid");
				TransformEvent.Action action = TransformEvent.Action.valueOf(ee.value("action").toUpperCase());
				return new TransformEvent(new TransformRef(tuid), action);
			}
		});

		_init = true;
	}
}
