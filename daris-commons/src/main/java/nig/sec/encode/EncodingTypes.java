package nig.sec.encode;

/**
 * Class to hold an enum defining the types of reversible encoding that are available.
 * 
 * @author nebk
 *
 */
public class EncodingTypes
{

	private EncodingTypes() {
	};
	
	public enum EncodingType { BASE_64, ROT13 };
	public static final String ENCODED_ATTR = "pssd-encoded";   // Name of Document attribute
	public static final String ENCODED_VALUE = "encoded";   // Value of attribute when document encoded
}