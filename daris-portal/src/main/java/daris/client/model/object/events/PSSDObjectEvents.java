package daris.client.model.object.events;

import arc.mf.client.xml.XmlElement;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventFactory;
import arc.mf.event.SystemEventRegistry;
import daris.client.model.object.DObjectRef;

public class PSSDObjectEvents {
	private static boolean _init = false;

	public static void initialize() {
		if (_init) {
			return;
		}

		SystemEventRegistry.add(PSSDObjectEvent.SYSTEM_EVENT_NAME, new SystemEventFactory() {
			public SystemEvent instantiate(String type, XmlElement ee) throws Throwable {
				String object = ee.value("object");
				PSSDObjectEvent.Action action = PSSDObjectEvent.Action.valueOf(ee.value("action").toUpperCase());
				int nbc = ee.intValue("object/@nbc", -1);
				return new PSSDObjectEvent(new DObjectRef(object, nbc), action);
			}
		});

		_init = true;
	}
}
