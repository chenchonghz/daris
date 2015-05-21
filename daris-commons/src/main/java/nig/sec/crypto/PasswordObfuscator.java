package nig.sec.crypto;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.xml.bind.DatatypeConverter;


/**
 * Simple obfuscator for reversible scrambling of passwords for storage in files.
 * <p>
 * Provides high level abstract encode and decode methods, which subclasses
 * can implement using any form of scrambling / encryption technique.
 * <p>
 * It's up to the subclass implementers to decide what level of encryption is
 * required.
 * <p>
 * Sample usage:
 * <pre>
 *    // obfuscate (encrypt + Base64 encode)
 *    PasswordObfuscator pobf = PasswordObfuscator.defaultXor();
 *    String encryptedString_Base64 = pobf.encode(passwordChars);
 * 
 *    // deobfuscate (Base64 decode + decrypt)
 *    PasswordObfuscator pobf = PasswordObfuscator.defaultXor();
 *    char[] decryptedPasswordChars = pobf.decode(encryptedString_Base64);
 * </pre>
 * The API is intentionally designed to accept and return passwords as char[]'s,
 * rather than Strings, so the passwords can be zeroed when no longer required.
 * <p>
 * If necessary, you can convert between Strings and char[]'s as follows:
 * <pre>
 *    char[] chars = someString.toCharArray();
 *    String passw = String.valueOf(chars);
 * </pre>
 * 
 * To see an example from the command-line, run:
 * <pre>
 *    java nig.sec.crypto.PasswordObfuscator -t &lt;passwordToObfuscate&gt;
 * </pre>
 * 
 * To safely generate obfuscated passwords from the command-line, run:
 * <pre>
 *    java nig.sec.crypto.PasswordObfuscator -e
 * </pre>
 * 
 * To decode obfuscated passwords from the command-line, run:
 * <pre>
 *    java nig.sec.crypto.PasswordObfuscator -d
 * </pre>
 * 
 * When packaged up inside of DaRIS, it's deployed in nig-commons.jar
 * SO usage is:
 * java -cp nig-commons.jar nig.sec.crypto.PasswordObfuscator -e
 * 
 * 
 * @author King Lung Chiu kinglung.chiu@gmail.com
 */
public abstract class PasswordObfuscator {

   /**
    * Encrypts the given password, then returns the resultant ciphertext as a
    * Base-64 string.
    * 
    * @param passwd  The password to encode, assumed to be in UTF-16BE.
    * @return        The encrypted password in Base-64 format.
    */
   public abstract String encode(char[] passwd);

   /**
    * Decodes the given Base-64 string, then decrypts the resultant ciphertext
    * back into the original password.
    * 
    * @param b64CipherTxt The encrypted password in Base-64 format.
    * @return The decrypted password chars, in UTF-16BE.
    */
   public abstract char[] decode(String b64CipherTxt);

   /**
    * @return An XOR-based obfuscator with default PRNG seed key.
    */
   public static PasswordObfuscator defaultXor() {
      return new StrongerXorPasswordObfuscator();
   }

   /**
    * @return An XOR-based obfuscator with user-supplied PRNG seed key.
    */
   public static PasswordObfuscator newXor(byte[] seedKey) {
      return new StrongerXorPasswordObfuscator(seedKey);
   }

   public static String base64Encode(byte[] rawBytes) {
      return DatatypeConverter.printBase64Binary(rawBytes);
   }
   
   public static byte[] base64Decode(String b64String) {
      return DatatypeConverter.parseBase64Binary(b64String);
   }


   /**
    * Sample app to show how the above is used. To see usage info, run:
    * <pre>
    *    java nig.sec.crypto.PasswordObfuscator
    * </pre>
    * 
    * @param args
    *    See inside {@link #showUsage()} for details.
    */
   public static void main(String[] args) {
      if(args.length == 0) {
         showUsage();
      } else switch(Choice.from(args[0])) {
         case ENCODE:
            runEncodingPrompt();
            break;
         case DECODE:
            runDecodingPrompt();
            break;
         case TEST:
            if(args.length == 2)
               runTests(args[1]);
            else
               showUsage();
            break;
         default:
            showUsage();
            break;
      }
   }
   
   private static void showUsage() {
      final String javaCmd = "java nig.sec.crypto.PasswordObfuscator ";
      StringBuilder usage = new StringBuilder();
      usage
         .append("\nUsage info:\n\n")
         .append("   # to encode passwords\n")
         .append("   % " + javaCmd)
         .append(Choice.ENCODE.FLAG)
         .append("\n\n")
         .append("   # to decode passwords\n")
         .append("   % " + javaCmd)
         .append(Choice.DECODE.FLAG)
         .append("\n\n")
         .append("   # to run sample tests\n")
         .append("   % " + javaCmd)
         .append(Choice.TEST.FLAG)
         .append(" <passwordToEncode>\n")
      ;

      System.out.println(usage.toString());
   }
   
   private static void runEncodingPrompt() {
      Console stdin = System.console();
      PasswordObfuscator encoder = defaultXor();

      boolean done = false;
      while(!done) {
         char[] pwd = stdin.readPassword("Please enter password (empty line to quit): ");
         if(pwd.length > 0) {
            System.out.printf("Encoded form: %s\n\n", encoder.encode(pwd));
            Arrays.fill(pwd, (char) 0);
         } else {
            done = true;            
         }
      }
   }
   
   private static void runDecodingPrompt() {
      BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
      PasswordObfuscator decoder = defaultXor();

      boolean done = false;
      while(!done) {
         String b64String;
         try {
            System.out.print("Please enter encoded string (empty line to quit):\n ");
            b64String = stdin.readLine();
            if(b64String.length() > 0) {
               System.out.printf(
                  "Decoded form: %s\n\n",
                  String.valueOf(decoder.decode(b64String)));
            } else {
               done = true;            
            }
         } catch (IOException ex) {
            done = true;
         }
      }
   }
   
   private static void runTests(String testString) {
      ((StrongerXorPasswordObfuscator) defaultXor()).test(testString.toCharArray());
   }
   
   private enum Choice {
      ENCODE("-e"),
      DECODE("-d"),
      TEST("-t"),
      UNKNOWN("");
      
      public static Choice from(String flag) {
         if(flag != null && choices.containsKey(flag))
            return choices.get(flag);
         else
            return UNKNOWN;
      }
      
      Choice(String flag) {
         FLAG = flag;
      }      
      public final String FLAG;
      private static final Map<String, Choice> choices = new HashMap<String, Choice>();
      static {
         for(Choice c: values()) {
            choices.put(c.FLAG, c);
         }
      }      
   }
}


/**
 * An implementation of {@link PasswordObfuscator} using a very simple
 * XOR encryption scheme with seeded random keys. This scheme is not strong
 * enough against a motivated attacker, but is still much more secure than
 * storing passwords in the clear.
 * <p>
 * This implementation is an improvement on the now deprecated
 * {@link SimpleXorPasswordObfuscator}. Specifically, the PRNG seed is now
 * generated using {@link SecureRandom} and changes on every call to encode,
 * so that the same password will still encode to a different string on each
 * invocation. This implementation also tries to use SecureRandom, instead of
 * the basic Random PRNG, where available.
 * 
 * @author King Lung Chiu kinglung.chiu@gmail.com
 */
final class StrongerXorPasswordObfuscator extends PasswordObfuscator {
   // protect these: random keys for protecting PRNG seed
   // - default value is arbitrary: adjust as required
   // - key length must == seed length
   // - seed length should be >= 160 bits (20 bytes) if using SecureRandom
   // - encrypt() will generate seeds of the same length as SEED_KEY
   // - to generate 32 secure random bytes in Scala REPL:
   //   (1 to 4).map(n=> (new SecureRandom).generateSeed(8).map("%4d" format _).mkString(", ")).mkString(",\n")
   private static final byte[] DEFAULT_SEED_KEY = {
      -105,  -88,  115,  -15,  -94,  -67,  -75,  105,
       -99,  -96,   32,  -10,  -61,  -60,   38,  103,
       108,  111,  -12, -103,  107,   52,   -7,   55,
        79,  -96,    8,   40,   95,   91, -117, -121
   };
   private final byte[] SEED_KEY;
   

   public static final String DEFAULT_CHARSET = "UTF-16BE";
   
   public StrongerXorPasswordObfuscator() { this(DEFAULT_SEED_KEY); }
   public StrongerXorPasswordObfuscator(byte[] seedKey) {
      SEED_KEY = seedKey;
   }
   
   @Override public String encode(char[] passwd) {
      return base64Encode(encrypt(bytesFrom(passwd)));
   }
   @Override public char[] decode(String b64CipherTxt) {
      return charsFrom(decrypt(base64Decode(b64CipherTxt)));
   }

   byte[] encrypt(byte[] rawBytes) {
      byte[] seed = new SecureRandom().generateSeed(SEED_KEY.length);
      byte[] keys = randomBytes(rawBytes.length, seed);
      byte[] encryptedBytes = new byte[seed.length + rawBytes.length];
      
      // copy & encrypt PRNG seed
      System.arraycopy(seed, 0, encryptedBytes, 0, seed.length);
      for(int i = 0; i < seed.length; i++) {
         encryptedBytes[i] ^= SEED_KEY[i];
      }
      
      // copy & encrypt rawBytes
      final int seedSize = seed.length;
      for(int i = 0; i < rawBytes.length; i++) {
         encryptedBytes[i + seedSize] = (byte) (rawBytes[i] ^ keys[i]);
      }
      
      destroy(keys);
      destroy(seed);

      return encryptedBytes;
   }
   
   byte[] decrypt(byte[] encryptedBytes) {
      // retrieve PRNG seed
      byte[] seed = Arrays.copyOfRange(encryptedBytes, 0, SEED_KEY.length);
      for(int i = 0; i < seed.length; i++) {
         seed[i] ^= SEED_KEY[i];
      }

      byte[] keys = randomBytes(encryptedBytes.length - seed.length, seed);
      byte[] rawBytes = new byte[keys.length];
      for(int i = 0; i < rawBytes.length; i++) {
         rawBytes[i] = (byte) (encryptedBytes[i + seed.length] ^ keys[i]);
      }
      destroy(keys);
      destroy(seed);

      return rawBytes;
   }

   /**
    * Generates an array of random bytes to use as encryption / decryption key.
    * Note:
    * <pre>
    *    cipherText == clearText  XOR randomBytes
    *    clearText  == cipherText XOR randomBytes
    * </pre>
    * 
    * @param len  The number of random bytes to generate.
    * @param seed The seed to use for the PRNG.
    * @return     The array of generated random bytes.
    */
   static byte[] randomBytes(int len, byte[] seed) {
      Random rand = prng(seed);
      byte[] bytes = new byte[len];
      rand.nextBytes(bytes);

      return bytes;
   }
   
   
   /**
    * Attempts to create a SecurRandom using the SHA1PRNG algorithm, with the
    * SUN provider and with the supplied seed.
    * If this fails, then a basic Random is created with the supplied seed.
    * If a basic Random is created, only the first 64 bits of the supplied seed
    * are used (b/c Random only accepts 1 long as a seed).
    * 
    * @param seed The seed for the prng.
    * @return A new prng using the supplied seed.
    */
   static Random prng(byte[] seed) {
      Random sr;
      try {
         sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
         ((SecureRandom) sr).setSeed(seed);
      } catch (Exception e) {
         final int LONG_SIZE = Long.SIZE / Byte.SIZE;
         sr = seed.length >= LONG_SIZE
            ? new Random(ByteBuffer.allocate(seed.length).put(seed).getLong())
            : new Random(ByteBuffer.allocate(LONG_SIZE).put(seed).getLong());
      }
      
      return sr;
   }
   
   static byte[] bytesFrom(char[] chars) {
      return Charset.forName(DEFAULT_CHARSET).encode(CharBuffer.wrap(chars)).array();
   }
   
   static char[] charsFrom(byte[] bytes) {
      return Charset.forName(DEFAULT_CHARSET).decode(ByteBuffer.wrap(bytes)).array();
   }

   /**
    * Resets an array of bytes to zero. Useful for destroying an encryption key.
    * 
    * @param data The array of bytes to clear.
    */
   static void destroy(byte[] data) { Arrays.fill(data, (byte) 0); }


   public void test(char[] rawChars) {
      System.out.printf("\n[%s]\n", this.getClass().getSimpleName());
      
      
      final byte[] rawBytes = bytesFrom(rawChars);
      
      String b64String = base64Encode(rawBytes);
      
      byte[] decodedBytes = base64Decode(b64String);
      char[] decodedChars = charsFrom(decodedBytes);
      
      System.out.println("--");
      System.out.println("input      : " + String.valueOf(rawChars));
      System.out.println("base64     : " + b64String);
      System.out.println("decoded    : " + String.valueOf(decodedChars));


      byte[] encryptedBytes = encrypt(rawBytes);
      String encryptedString = base64Encode(encryptedBytes);
      byte[] encryptedeBytesDecoded = base64Decode(encryptedString);
      byte[] decryptedBytes = decrypt(encryptedeBytesDecoded);
      char[] decryptedChars = charsFrom(decryptedBytes);
      
      System.out.println("--");
      System.out.println("encrypted  : " + encryptedString);
      System.out.println("decrypted  : " + String.valueOf(decryptedChars));
      System.out.println("raw=d'bytes: " + Arrays.equals(rawBytes, decryptedBytes));
      System.out.println("raw = dec'd: " + Arrays.equals(rawChars, decryptedChars));
      System.out.println("--");
      
      String encoded = encode(rawChars);
      char[] decoded = new StrongerXorPasswordObfuscator(SEED_KEY).decode(encoded);
      System.out.println("in == out? : " + Arrays.equals(rawChars, decoded));
      System.out.println("--");
      
      Map<String, Integer> uniques = new HashMap<String, Integer>();
      int successes = 0,
          failures  = 0;
      
      final int tries = 9000;
      for(int i = 0; i < tries; i++) {
         encoded = encode(rawChars);
         uniques.put(
            encoded,
            uniques.containsKey(encoded)? uniques.get(encoded) + 1: 1);
         if(Arrays.equals(new StrongerXorPasswordObfuscator(SEED_KEY).decode(encoded), rawChars)) {
            successes += 1;
         } else {
            failures += 1;
         }
      }
      System.out.println("itr'n count: " + tries);
      System.out.println("uniques    : " + uniques.entrySet().size());
      System.out.println("matches    : " + successes);
      System.out.println("mismatches : " + failures);
      System.out.println("==\n");
   }
}