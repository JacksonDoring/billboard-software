package billboard.server;

/**
 * Custom exception for password hashing
 */
public class PasswordHashException extends Exception {
    public PasswordHashException(String message) {
        super(message);
    }
}