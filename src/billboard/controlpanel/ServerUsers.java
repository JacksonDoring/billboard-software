package billboard.controlpanel;

import billboard.server.PasswordHash;
import billboard.server.PasswordHashException;
import billboard.server.ServerResponse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Class for sending requests to the server relating to users
 */
public class ServerUsers {
    /**
     * Gets all usernames from the sever socket and returns them as an ArrayList
     * @return ArrayList of usernames
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static HashMap<Integer, String> getUsernames() throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getUsernames");
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        var usernames = (HashMap<Integer, String>) response.data;

        return usernames;
    }

    /**
     * Updates the password of the userId that is given
     * @param userId - User ID to update
     * @param newPasswordHash - New password hash
     * @return a boolean to say if the updated succeeded
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static boolean updatePassword(Integer userId, String newPasswordHash) throws BillboardServerException {
        // Getting the data from the parameters and putting it in the hashmap
        HashMap userData = new HashMap();
        userData.put("userId", userId);
        userData.put("newPasswordHash", newPasswordHash);

        ServerResponse response = ServerConnection.sendRequest("updatePassword", userData);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        return true;
    }

    /**
     * checks the current sessions
     * @return returns a boolean to say if it is a valid session or not
     */
    public static boolean checkActiveSession() {
        ServerResponse response = ServerConnection.sendRequest("checkSession");
        if (response.error != null) {
            return false;
        }

        return true;
    }

    /**
     * Gets user data
     * @param userId - User ID to get data for
     * @return HashMap of user data
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static HashMap getUserData(int userId) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getUserData", userId);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        var userData = (HashMap) response.data;

        return userData;
    }

    /**
     * Logs out a user
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static void logoutUser() throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("logoutUser", null);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

    }

    /**
     * Gets the user id from the username that it is supplied with
     * @param username - Username to get user ID for
     * @return User ID
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static Integer getUserId(String username) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getUserId", username);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        Integer userId = (Integer) response.data;

        return userId;
    }

    /**
     * Add the user to the database with the given paremeters
     * @param username - New user's username
     * @param password - New user's password
     * @param editBillboards - New user's edit billboards permission
     * @param createBillboards - New user's create billboards permission
     * @param scheduleBillboards - New user's schedule billboards permission
     * @param editUsers - New user's edit users permission
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static void addUser(String username, String password, Boolean editBillboards, Boolean createBillboards, Boolean scheduleBillboards, Boolean editUsers) throws BillboardServerException {
        String passwordHash;

        try {
            // Hashing the password
            passwordHash = PasswordHash.hashString(password);
        } catch (NoSuchAlgorithmException | PasswordHashException e) {
            e.printStackTrace();

            throw new BillboardServerException("Failed to hash password");
        }

        // Getting the data from the parameters and putting it in the hashmap
        HashMap userData = new HashMap();
        userData.put("username", username);
        userData.put("passwordHash", passwordHash);
        userData.put("editBillboards", editBillboards);
        userData.put("createBillboards", createBillboards);
        userData.put("scheduleBillboards", scheduleBillboards);
        userData.put("editUsers", editUsers);

        ServerResponse response = ServerConnection.sendRequest("addUser", userData);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }
    }

    /**
     * Creates a login session for the user if the user credentials are recorded in the database
     * @param username - Username to login with
     * @param password - Password to login with
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static void loginUser(String username, String password) throws BillboardServerException {
        String passwordHash;

        try {
            // Hashing the password
            passwordHash = PasswordHash.hashString(password);
        } catch (NoSuchAlgorithmException | PasswordHashException e) {
            e.printStackTrace();

            throw new BillboardServerException("Failed to hash password");
        }

        // Getting the data from the parameters and putting it in the hashmap
        HashMap userData = new HashMap();
        userData.put("username", username);
        userData.put("passwordHash", passwordHash);

        ServerResponse response = ServerConnection.sendRequest("loginUser", userData);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        HashMap data = (HashMap) response.data;

        String token = (String) data.get("sessionKey");
        Integer userId = (Integer) data.get("userId");

        Token.setToken(token); // Set the session token
        Token.setUserId(userId);

        //setting the userId to be user later
        Token.setUserId(ServerUsers.getUserId(username));
    }

    /**
     * Gets the permissions of a specified user
     * @param userId - User ID to get permissions for
     * @return A HashMap of all the permissions the user has
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static HashMap<String, Boolean> getPermissions(int userId) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getPermissions", userId);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        HashMap<String, Boolean> userData = (HashMap<String, Boolean>) response.data;
        return userData;
    }

    /**
     * Gets the permissions of the currently logged in user
     * @return A HashMap of all the permissions the user has
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static HashMap<String, Boolean> getOwnPermissions() throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getOwnPermissions");
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        HashMap<String, Boolean> userData = (HashMap<String, Boolean>) response.data;
        return userData;
    }

    /**
     * Deletes a user from the database
     * @param userId - User ID to delete
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static void deleteUser(Integer userId) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("deleteUser", userId);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }
    }

    /**
     * Updates the provided user permissions
     * @param userId - User ID to update permissions for
     * @param editBillboards - New edit billboards permission
     * @param createBillboards - New create billboards permission
     * @param scheduleBillboards - New schedule billboards permission
     * @param editUsers - New edit users permission
     * @return A boolean to show if the permissions were updated succesfully
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static boolean updateUserPermissions(Integer userId, Boolean editBillboards, Boolean createBillboards, Boolean scheduleBillboards, Boolean editUsers) throws BillboardServerException {
        // Getting the data from the parameters and putting it in the hashmap
        HashMap userPermissions = new HashMap();
        userPermissions.put("userId", userId);
        userPermissions.put("editBillboards", editBillboards);
        userPermissions.put("createBillboards", createBillboards);
        userPermissions.put("scheduleBillboards", scheduleBillboards);
        userPermissions.put("editUsers", editUsers);


        ServerResponse response = ServerConnection.sendRequest("updateUserPermissions", userPermissions);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        return true;
    }
}

