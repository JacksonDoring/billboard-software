package billboard.server;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Class for hashing
 */
public class PasswordHash {
    /**
     * Hashes a string and returns the hash string
     * @param input - String to hash
     * @return Hashed input
     * @throws NoSuchAlgorithmException if the hashing fails
     * @throws PasswordHashException if the input is empty
     */
    public static String hashString(String input) throws NoSuchAlgorithmException, PasswordHashException {
        if (input == "")
            throw new PasswordHashException("Input empty");

        byte[] hash = SHAHash(input);
        return hashToString(hash);
    }

    /**
     * Generates a random salt
     * @return Salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();

        byte bytes[] = new byte[20];

        random.nextBytes(bytes);

        return hashToString(bytes);
    }

    /**
     * Adds a salt to a hash and rehashes it
     * @param hash - Hash to add the salt to
     * @param salt - Salt to add to the hash
     * @return Salted hash
     * @throws NoSuchAlgorithmException if the hashing fails
     * @throws PasswordHashException if the hashing fails
     */
    public static String addSaltToHash(String hash, String salt) throws NoSuchAlgorithmException, PasswordHashException {
        // Add the salt to the hash
        hash += salt;

        // Hash it again
        hash = hashString(hash);

        // Return the salted hashed string
        return hash;
    }

    /**
     * Hashes a string using SHA-256
     * @param input - String to hash
     * @return Hashed string
     * @throws NoSuchAlgorithmException if the hashing fails
     */
    private static byte[] SHAHash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converts a hash to string
     * @param hash - Hash to convert to a string
     * @return String conversion of hash
     */
    private static String hashToString(byte[] hash)  {
        BigInteger number = new BigInteger(1, hash);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
}
