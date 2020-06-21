package billboard.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Main server class
 */
public class Server {
    /**
     * Main server function, starts the server and handles requests
     * @throws IOException if socket functions fail
     * @throws SQLException if an SQL exception occurs
     */
    public static void startServer() throws IOException, SQLException {
        int port = 4000; // Default port

        // Opening the server.props file to get the server hosting information
        try {
            FileInputStream in = new FileInputStream("resources/server.props");
            Properties props = new Properties();
            props.load(in);
            in.close();

            // Get the data from the props file
            port = Integer.parseInt(props.getProperty("port"));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // Create the server socket
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Server started on port " + serverSocket.getLocalPort());

        // Initial connect to the database
        Connection connection = DbConnection.getInstance();

        try {
            //Adding a user if none already exist
            DbUser.addInitialUser();
        } catch (NoSuchAlgorithmException | DBException | PasswordHashException e) {
            e.printStackTrace();
        }

        while (true) {
            //opening the socket
            Socket socket = serverSocket.accept();

            //NOTE: input stream gets information and Output stream sends out information
            //code for handling requests to the server
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(inputStream);

            //for sending data back to the client
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);

            // first request from the server will always be this
            String request = ois.readUTF();

            // Second is the token
            String token = ois.readUTF();

            // Print the request
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String timeString = dtf.format(now);
            System.out.printf("[%s] Request: %s\n", timeString, request);

            // Handle request
            ServerResponse returnData = new ServerResponse();
            try {
                switch (request){
                    case "getUsernames": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        // Permissions required: "Edit Users".
                        if (!permissions.get("editUsers")) {
                            throw new DBException("Insufficient permissions");
                        }

                        HashMap<Integer, String> usernames = DbUser.getUsernames();

                        returnData.data = usernames;

                        break;
                    }

                    case "getUserData": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        int requestingUserId = (int) ois.readObject();

                        // Permissions required: if a user is requesting their own details, none. To get details for other users, "Edit Users" permission is required.
                        if (requestingUserId != userId) {
                            if (!permissions.get("editUsers")) {
                                throw new DBException("Insufficient permissions");
                            }
                        }

                        var userData = DbUser.getUserData(requestingUserId);

                        returnData.data = userData;

                        break;
                    }

                    case "listBillboards": {
                        // All users will be able to access a list of all billboards on the system and preview their contents.
                        var billboards = DbBillboard.listBillboards();

                        returnData.data = billboards;

                        break;
                    }

                    case "createBillboard": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        // Users with the "Create Billboards" permission can create new billboards
                        if (!permissions.get("createBillboards")) {
                            throw new DBException("Insufficient permissions");
                        }

                        HashMap data = (HashMap) ois.readObject();
                        String billboardName = (String) data.get("billboardName");
                        String billboardData = (String) data.get("billboardData");

                        DbBillboard.createBillboard(billboardName, billboardData, userId);

                        break;
                    }

                    case "updateBillboard": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        // To edit own billboard, as long as it is not currently scheduled, must have
                        // "Create Billboards" permission. To edit another user’s billboard or edit a billboard
                        // that is currently scheduled, must have "Edit All Billboards" permission.)

                        HashMap data = (HashMap) ois.readObject();
                        String billboardName = (String) data.get("billboardName");
                        String billboardData = (String) data.get("billboardData");
                        Integer billboardId = (Integer) data.get("billboardId");

                        int creatorId = DbBillboard.getBillboardCreatorId(billboardId);

                        // Permissions required:
                        if (userId == creatorId) { // To edit own billboard
                            ArrayList<HashMap> billboardSchedule = DbSchedule.getBillboardSchedule(billboardId);
                            if (billboardSchedule.isEmpty()) { // as long as it is not currently scheduled, must have "Create Billboards" permission.
                                if (!permissions.get("createBillboards")) {
                                    throw new DBException("Insufficient permissions");
                                }
                            } else {
                                if (!permissions.get("editBillboards")) { // edit a billboard that is currently scheduled, must have "Edit All Billboards" permission.)
                                    throw new DBException("Insufficient permissions");
                                }
                            }
                        } else { // To edit another user’s billboard, must have "Edit All Billboards" permission.)
                            if (!permissions.get("editBillboards")) {
                                throw new DBException("Insufficient permissions");
                            }
                        }

                        DbBillboard.updateBillboard(billboardName, billboardData, billboardId);

                        break;
                    }

                    case "billboardNameExists": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        String billboardName = (String) ois.readObject();

                        returnData.data = DbBillboard.billboardNameExists(billboardName);

                        break;
                    }

                    case "getBillboardData": {
                        int billboardId = (int) ois.readObject();

                        String billboardData = DbBillboard.getBillboardData(billboardId);

                        returnData.data = billboardData;

                        break;
                    }

                    case "getBillboardCreatorName": {
                        int billboardId = (int) ois.readObject();

                        String billboardData = DbBillboard.getBillboardCreatorName(billboardId);

                        returnData.data = billboardData;

                        break;
                    }

                    case "getBillboardName": {
                        // Permissions required: none.

                        int billboardId = (int) ois.readObject();

                        String billboardName = DbBillboard.getBillboardName(billboardId);

                        returnData.data = billboardName;

                        break;
                    }

                    case "getBillboardId": {
                        // Permissions required: none.

                        String billboardName = (String) ois.readObject();

                        int billboardId = DbBillboard.getBillboardId(billboardName);

                        returnData.data = billboardId;

                        break;
                    }

                    case "deleteBillboard": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        int billboardId = (int) ois.readObject();

                        int creatorId = DbBillboard.getBillboardCreatorId(billboardId);

                        // (Permissions required: if deleting
                        // own billboard and that billboard is not currently scheduled, must have "Create
                        // Billboards" permission. To delete any other billboards, including those currently
                        // scheduled, must have "Edit All Billboards" permission.)

                        // Permissions required:
                        if (userId == creatorId) {  // if deleting own billboard
                            ArrayList<HashMap> billboardSchedule = DbSchedule.getBillboardSchedule(billboardId);
                            if (billboardSchedule.isEmpty()) { // and that billboard is not currently scheduled, must have "Create Billboards" permission.
                                if (!permissions.get("createBillboards")) {
                                    throw new DBException("Insufficient permissions");
                                }
                            } else { // To delete any other billboards, including those currently scheduled, must have "Edit All Billboards" permission.)
                                if (!permissions.get("editBillboards")) {
                                    throw new DBException("Insufficient permissions");
                                }
                            }
                        } else { // To delete any other billboards, including those currently scheduled, must have "Edit All Billboards" permission.)
                            if (!permissions.get("editBillboards")) {
                                throw new DBException("Insufficient permissions");
                            }
                        }

                        DbBillboard.deleteBillboard(billboardId);

                        break;
                    }

                    case "addSchedule": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        // Permissions required: "Schedule Billboards".
                        if (!permissions.get("scheduleBillboards")) {
                            throw new DBException("Insufficient permissions");
                        }

                        var scheduleData = (HashMap) ois.readObject();
                        int billboardId = (int) scheduleData.get("billboardId");
                        int day = (int) scheduleData.get("day");
                        int minutesStart = (int) scheduleData.get("minutesStart");
                        int minutesDuration = (int) scheduleData.get("minutesDuration");
                        boolean repeating = (boolean) scheduleData.get("repeating");
                        int minutesRepeatGap = (int) scheduleData.get("minutesRepeatGap");
                        DbSchedule.addSchedule(billboardId, day, minutesStart, minutesDuration, repeating, minutesRepeatGap, userId);

                        break;
                    }

                    case "logoutUser": {
                        DbLogin.logoutUser(token);

                        break;
                    }

                    case "getAllSchedules": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);
                        
                        var schedule = DbSchedule.getAllSchedules();

                        returnData.data = schedule;

                        break;
                    }

                    case "getBillboardSchedule": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        // Permissions required: "Schedule Billboards".
                        if (!permissions.get("scheduleBillboards")) {
                            throw new DBException("Insufficient permissions");
                        }

                        int scheduleId = (int) ois.readObject();

                        var schedule = DbSchedule.getBillboardSchedule(scheduleId);

                        returnData.data = schedule;

                        break;
                    }

                    case "deleteSchedule": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        // Permissions required: "Schedule Billboards".
                        if (!permissions.get("scheduleBillboards")) {
                            throw new DBException("Insufficient permissions");
                        }

                        int scheduleId = (int) ois.readObject();

                        DbSchedule.deleteSchedule(scheduleId);

                        break;
                    }

                    case "getUserId": {
                        int userId = DbLogin.checkToken(token);

                        String username = (String) ois.readObject();

                        returnData.data = DbUser.getUserId(username);

                        break;
                    }

                    case "getOwnPermissions": {
                        int userId = DbLogin.checkToken(token);
                        var userPermissions = DbUser.getUserPermissions(userId);

                        // Permissions required: if a user is requesting their own details, none

                        returnData.data = userPermissions;

                        break;
                    }

                    case "getPermissions": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        int requestingUserId = (int) ois.readObject();

                        // Permissions required: if a user is requesting their own details, none. To get details for other users, "Edit Users" permission is required
                        if (requestingUserId != userId) {
                            if (!permissions.get("editUsers")) {
                                throw new DBException("Insufficient permissions");
                            }
                        }

                        var userPermissions = DbUser.getUserPermissions(requestingUserId);

                        returnData.data = userPermissions;

                        break;
                    }

                    case "updatePassword": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        var userData = (HashMap) ois.readObject();

                        Integer requestUserId = (Integer) userData.get("userId");
                        String newPasswordHash = (String) userData.get("newPasswordHash");

                        // Note that all users will be able to change their own passwords, though only
                        // users with "Edit Users" permission can change the passwords of other users.
                        if (requestUserId != userId) {
                            if (!permissions.get("editUsers")) {
                                throw new DBException("Insufficient permissions");
                            }
                        }

                        DbUser.updatePassword(requestUserId, newPasswordHash);

                        break;
                    }

                    case "updateUserPermissions": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        // Permissions required: "Edit Users".
                        if (!permissions.get("editUsers")) {
                            throw new DBException("Insufficient permissions");
                        }

                        var userPermissions = (HashMap) ois.readObject();

                        Integer requestUserId = (Integer) userPermissions.get("userId");
                        Boolean editBillboards = (Boolean) userPermissions.get("editBillboards");
                        Boolean createBillboards = (Boolean) userPermissions.get("createBillboards");
                        Boolean scheduleBillboards = (Boolean) userPermissions.get("scheduleBillboards");
                        Boolean editUsers = (Boolean) userPermissions.get("editUsers");

                        // Note that no user has the ability to remove their own "Edit Users" permission.
                        if (requestUserId == userId) {
                            editUsers = true; // can't remove own
                        }

                        DbUser.updateUserPermissions(requestUserId, editBillboards, createBillboards,scheduleBillboards, editUsers);

                        break;
                    }

                    case "addUser": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        // Permissions required: "Edit Users".)
                        if (!permissions.get("editUsers")) {
                            throw new DBException("Insufficient permissions");
                        }

                        var userData = (HashMap) ois.readObject();
                        String username = (String) userData.get("username");
                        String passwordHash = (String) userData.get("passwordHash");
                        Boolean editBillboards = (Boolean) userData.get("editBillboards");
                        Boolean createBillboards = (Boolean) userData.get("createBillboards");
                        Boolean scheduleBillboards = (Boolean) userData.get("scheduleBillboards");
                        Boolean editUsers = (Boolean) userData.get("editUsers");

                        DbUser.addUser(username, passwordHash, editBillboards, createBillboards, scheduleBillboards, editUsers);

                        break;
                    }

                    case "deleteUser": {
                        int userId = DbLogin.checkToken(token);
                        HashMap<String, Boolean> permissions = DbUser.getUserPermissions(userId);

                        // (Permissions required: "Edit Users".
                        if (!permissions.get("editUsers")) {
                            throw new DBException("Insufficient permissions");
                        }

                        int deletingId = (int) ois.readObject();

                        // Note that no user has the ability to remove themselves.)
                        if (deletingId == userId) {
                            throw new DBException("Can't delete your own account");
                        }

                        DbUser.deleteUser(deletingId);

                        break;
                    }

                    case "loginUser": {
                        var userData = (HashMap) ois.readObject();

                        String username = (String) userData.get("username");
                        String passwordHash = (String) userData.get("passwordHash");

                        var data = DbLogin.loginUser(username, passwordHash);

                        returnData.data = data;

                        break;
                    }

                    case "checkSession": {
                        DbLogin.checkToken(token); // If it's not valid then it'll throw an error

                        break;
                    }

                    case "getCurrentBillboard": {
                        HashMap currentBillboard = DbSchedule.getCurrentBillboard();

                        returnData.data = currentBillboard;

                        break;
                    }

                    default: {
                        throw new DBException("Request type unknown");
                    }
                }
            } catch (DBException e) {
                System.out.println("- Error: " + e.getMessage());

                returnData.error = e.getMessage();
            } catch (ClassNotFoundException | NoSuchAlgorithmException | PasswordHashException e) {
                e.printStackTrace();

                returnData.error = "Internal server error";
            }

            // Finalise request
            oos.writeObject(returnData);

            oos.flush();

            oos.close();
            ois.close();
            socket.close();
        }
    }
}