package billboard.controlpanel;

/**
 * Exception class for a custom client-server exception
 */
public class BillboardServerException extends Exception {
    public BillboardServerException(String errorMessage) {
        super(errorMessage);
    }
}