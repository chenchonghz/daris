package nig.util;

public class ObjectUtil {

	/**
	 * Checks whether two object values are equal. Takes account of nulls and
	 * collections.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equals(Object a, Object b) {

		if (a == null) {
			if (b == null) {
				return true;
			}

			return false;
		}

		if (b == null) {
			return false;
		}

		return a.equals(b);
	}

}
