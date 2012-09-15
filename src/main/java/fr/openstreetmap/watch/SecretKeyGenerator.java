package fr.openstreetmap.watch;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;

/**
 * Stored alert access key generator.
 * We need to create cryptographically secure keys for accessing alert results with the following characteristics:
 *  - Strong guarantee against duplicates 
 *  - Hard to guess, hard to guess following ones from one
 *  - (nice to have) Ability to perform cheap validity checking  
 *  
 * The key has the following structure:
 *   - 32 chars : UUID
 *   - 58 to 78 chars: base64 representation of 
 *       - 42 random bytes
 *       - 16 bytes: MD5 of the 42 random bytes
 * The quick check verifies: length, first char, and that the MD5 matches the random bytes
 */
public class SecretKeyGenerator {
    public static String generate() {
        try {
            Base64 b64 = new Base64(0, new byte[0], true);
            StringBuilder sb = new StringBuilder();

            /* Add an UUID to ensure uniqueness */
            UUID uuid = UUID.randomUUID();
            sb.append(String.format("%16x%16x", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));

            /* Add random bytes to generate more entropy */
            byte[] bb = randomBytes(42);
            byte[] md5 = MessageDigest.getInstance("md5").digest(bb);
            byte[] payload = ArrayUtils.addAll(bb, md5);
            sb.append(b64.encodeToString(payload));

            return sb.toString();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /* Cheap validation */
    public static void validate(String key) throws Exception {
        // 32 (uuid) + (42+16) (minimum for base64 of 42 random bytes + MD5)
        if (key.length() < 90) {
            throw new Exception("Invalid key (too short)");
        }
        String remainder = key.substring(32);
        Base64 b64 = new Base64(0, new byte[0], true);
        byte[] data = b64.decode(remainder);
        if (data.length !=  (42+16)) {
            throw new Exception("Invalid key (unexpected payload length " + data.length);
        }
        byte[] payload = ArrayUtils.subarray(data, 0, 42);
        byte[] md5 = ArrayUtils.subarray(data, 42, 42+16);
        byte[] expected = MessageDigest.getInstance("md5").digest(payload);
        if (!ArrayUtils.isEquals(expected, md5)) {
            throw new Exception("Invalid key (checksum error)");
        }
    }

    protected synchronized static byte[] randomBytes(int size) {
        byte[] buf = new byte[size];
        random.nextBytes(buf);
        return buf;
    }
    static SecureRandom random; 
    static {
        // Automatically seeded with currentTimeMillis
        random = new SecureRandom();
    }
}