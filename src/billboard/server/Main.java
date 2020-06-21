package billboard.server;

import billboard.controlpanel.ServerUsers;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Main class for starting the server
 */
public class Main {
    /**
     * Starts the server
     * @param args - Main args
     * @throws IOException if socket functions fail
     * @throws SQLException if an SQL exception occurs
     */
    public static void main(String[] args) throws IOException, SQLException {
        Server.startServer();
    }
}
