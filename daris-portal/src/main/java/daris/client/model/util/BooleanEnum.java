package daris.client.model.util;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.EnumerationType;


/**
 * Class to wrap a boolean into an enum so that the portal can not save the element
 * if it is not set (the simpler Boolean type is always saved, true or false). 
 * 
 * @author nebk
 *
 */
public enum BooleanEnum {

	TRUE, FALSE;
	@Override
	public String toString() {

		return super.toString().toLowerCase();
	}

	public static EnumerationType<BooleanEnum> asEnumerationType() {

		List<EnumerationType.Value<BooleanEnum>> evs = new Vector<EnumerationType.Value<BooleanEnum>>(
				values().length);
		for (int i = 0; i < values().length; i++) {
			evs.add(new EnumerationType.Value<BooleanEnum>(values()[i].toString(),
					values()[i].toString(), values()[i]));
		}
		return new EnumerationType<BooleanEnum>(evs);
	}

	public static BooleanEnum parse(String processed) {

		if (processed != null) {
			if (processed.equalsIgnoreCase(TRUE.toString())) {
				return TRUE;
			}
			if (processed.equalsIgnoreCase(FALSE.toString())) {
				return FALSE;
			}
		}
		return null;
	}
}
