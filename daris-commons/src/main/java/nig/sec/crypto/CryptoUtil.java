package nig.sec.crypto;



public class CryptoUtil {
	
	/**
	 * Convenience function to decrypt a string with the Obfuscator
	 * 
	 * @param pw
	 * @return
	 */
	public static String decryptString (String pw) {
	    PasswordObfuscator encoder = PasswordObfuscator.defaultXor();
	    char[] pc = encoder.decode(pw);
	    return String.valueOf(pc);
	}
}
