package billboard.server;

/**
 * Exception class for a custom server-database exception
 */
public class DBException extends Exception {
    public DBException(String errorMessage) {
        super(errorMessage);
    }
}