package billboard.server;

import java.io.Serializable;

/**
 * Class for server responses
 */
public class ServerResponse implements Serializable {
    public String error = null;
    public Object data = null;
}
