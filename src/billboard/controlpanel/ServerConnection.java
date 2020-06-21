package billboard.controlpanel;

import billboard.server.ServerResponse;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

/**
 * Class for connecting to the server
 */
public class ServerConnection {
    private static String url;
    private static String port;

    /**
     * Gets the server socket using data from server.props
     * @return Server socket
     * @throws IOException if starting the socket fails
     */
    public static Socket getSocket() throws IOException {
        // Load in server props
        Properties props = new Properties();
        FileInputStream in = null;

        // Opening the server.props file to get the server connection information
        try {
            in = new FileInputStream("resources/server.props");
            props.load(in);
            in.close();

            // Get the data from the props file
            url = props.getProperty("url");
            port = props.getProperty("port");

        }
        catch (Exception e){
            e.printStackTrace();
        }

        // set up socket (or HTTP) connection with server
        return new Socket(url, Integer.parseInt(port));
    }

    /**
     * Sends a server request with data
     * @param requestType - Type of request
     * @param data - Object containing request data
     * @return Server response
     */
    public static ServerResponse sendRequest(String requestType, Object data) {
        try {
            Socket socket = ServerConnection.getSocket();

            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // Set the request type
            oos.writeUTF(requestType);

            // Add the token
            String token = Token.getToken();
            oos.writeUTF(token);

            // Add the data
            if (data != null) {
                oos.writeObject(data);
            }

            // Send request
            oos.flush();

            // Get response
            ServerResponse outputData = (ServerResponse) ois.readObject();

            // Finish up
            ois.close();
            oos.close();
            socket.close();

            return outputData;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();

            ServerResponse failedResponse = new ServerResponse();
            failedResponse.error = "Failed to connect to server";
            return failedResponse;
        }
    }

    /**
     * Sends a server request without data
     * @param requestType - Type of request
     * @return Server response
     */
    public static ServerResponse sendRequest(String requestType) {
        return sendRequest(requestType, null);
    }
}
