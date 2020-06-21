package billboard.server;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * DB functions for interacting with users
 */
public class DbUser {
    /**
     * Gets all the usernames from the database
     * @return ArrayList of usernames
     * @throws SQLException if an SQL exception occurs
     */
    public static HashMap<Integer, String> getUsernames() throws SQLException {
        Connection connection = DbConnection.getInstance();

        Statement statement = connection.createStatement();

        ResultSet rs = statement.executeQuery("SELECT userId, username FROM users");

        // list to hold the data from the result set
        HashMap<Integer, String> usernames = new HashMap<Integer, String>();
        while (rs.next()){
            usernames.put(rs.getInt("userId"), rs.getString("username"));
        }

        return usernames;
    }

    /**
     * Check if a given username exists
     * @param username - Username to check
     * @return Boolean whether or not the username exists
     * @throws SQLException if an SQL exception occurs
     */
    public static boolean usernameExists(String username) throws SQLException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username=?;");
        statement.setString(1, username);

        ResultSet rs = statement.executeQuery();

        return rs.next(); // If there's a result returned, then this will be true
    }

    /**
     * Get billboards that were created by a given user
     * @param userId - User ID to get billboards for
     * @return HashMap of billboard ids and names
     * @throws SQLException if an SQL exception occurs
     */
    public static HashMap<Integer, String> getUsersBillboards(Integer userId) throws SQLException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT billboardId, name FROM billboards WHERE userId=?;");
        statement.setInt(1, userId);

        ResultSet rs = statement.executeQuery();

        // Get data
        HashMap<Integer, String> billboards = new HashMap();
        while (rs.next()) {
            billboards.put(rs.getInt("billboardId"), rs.getString("name"));
        }

        return billboards;
    }

    /**
     * Get data for a specific user
     * @param userId - User ID to get data for
     * @return HashMap of user data
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if the request fails
     */
    public static HashMap<String, String> getUserData(int userId) throws SQLException, DBException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE userId=?;");
        statement.setInt(1, userId);

        ResultSet rs = statement.executeQuery();
        if (!rs.next()) {
            throw new DBException("User does not exist");
        }

        HashMap<String, String> userData = new HashMap();
        userData.put("username", rs.getString("username"));
        userData.put("password", rs.getString("password"));

        userData.put("editBillboards", rs.getString("editBillboards"));
        userData.put("createBillboards", rs.getString("createBillboards"));
        userData.put("scheduleBillboards", rs.getString("scheduleBillboards"));
        userData.put("editUsers", rs.getString("editUsers"));

        return userData;
    }

    
    /**
     * Get permissions for a specific user
     * @param userId - User ID to get permissions for
     * @return HashMap of permissions
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if the request fails
     */
    public static HashMap<String, Boolean> getUserPermissions(int userId) throws SQLException, DBException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT editBillboards, createBillboards, scheduleBillboards, editUsers FROM users WHERE userId=?;");
        statement.setInt(1, userId);

        ResultSet rs = statement.executeQuery();
        if (!rs.next()) {
            throw new DBException("User does not exist");
        }

        // Get data
        HashMap<String, Boolean> permissions = new HashMap();
        permissions.put("editBillboards", rs.getBoolean("editBillboards"));
        permissions.put("createBillboards", rs.getBoolean("createBillboards"));
        permissions.put("scheduleBillboards", rs.getBoolean("scheduleBillboards"));
        permissions.put("editUsers", rs.getBoolean("editUsers"));

        return permissions;
    }

    /**
     * Adds a new user to the database
     * @param username - Username
     * @param passwordHash - Password hash
     * @param editBillboards - Edit billboards permission
     * @param createBillboards - Create billboards permission
     * @param scheduleBillboards - Schedule billboards permission
     * @param editUsers - Edit users permission
     * @throws SQLException if an SQL exception occurs
     * @throws NoSuchAlgorithmException if adding the salt to the hashed password fails
     * @throws DBException if the request fails
     * @throws PasswordHashException if the hashing fails
     */
    public static void addUser(String username, String passwordHash, Boolean editBillboards, Boolean createBillboards, Boolean scheduleBillboards, Boolean editUsers) throws SQLException, NoSuchAlgorithmException, DBException, PasswordHashException {
        // Check if there's a username and password given
        if (username == "" || passwordHash == "") {
            throw new DBException("Not all fields provided");
        }

        // Check if a user with the same username already exists
        if (usernameExists(username)) {
            throw new DBException("Username already exists");
        }

        // Salt the password hash
        String salt = PasswordHash.generateSalt();
        String saltedHashedPassword = PasswordHash.addSaltToHash(passwordHash, salt);

        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username, password, salt, editBillboards, createBillboards, scheduleBillboards, editUsers) VALUES (?, ?, ?, ?, ?, ?, ?);");
        statement.setString(1, username);
        statement.setString(2, saltedHashedPassword);
        statement.setString(3, salt);
        statement.setBoolean(4, editBillboards);
        statement.setBoolean(5, createBillboards);
        statement.setBoolean(6, scheduleBillboards);
        statement.setBoolean(7, editUsers);

        statement.execute();
    }

    /**
     * Adds an initial user if there are none already existing
     * @throws SQLException if an SQL exception occurs
     * @throws NoSuchAlgorithmException if hashing the password fails
     * @throws DBException if the request fails
     * @throws PasswordHashException if the hashing fails
     */
    public static void addInitialUser() throws SQLException, NoSuchAlgorithmException, DBException, PasswordHashException {
        // Check if any users exist
        Connection connection = DbConnection.getInstance();

        //Selecting all the rows from the user table in the database
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM users");

        //If the sql returns a result then a user already exists
        if (rs.next())
            return;

        //Creating username and password for initial user
        final String username = "admin";
        final String password = "password";

        // Hashing the password
        String hashedPassword = PasswordHash.hashString(password);

        //Adding the initial user
        addUser(username, hashedPassword, true, true, true, true);
    }

    /**
     * Updates the permissions of a user
     * @param userId - User ID to update permissions for
     * @param editBillboards - New edit billboards permission
     * @param createBillboards - New create billboards permission
     * @param scheduleBillboards - New schedule billboards permission
     * @param editUsers - New edit users permission
     * @throws SQLException if an SQL exception occurs
     */
    public static void updateUserPermissions(Integer userId, Boolean editBillboards, Boolean createBillboards, Boolean scheduleBillboards, Boolean editUsers) throws SQLException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("UPDATE users SET editBillboards = ?, createBillboards = ?, scheduleBillboards = ?, editUsers = ? WHERE userId=?");
        statement.setBoolean(1, editBillboards);
        statement.setBoolean(2, createBillboards);
        statement.setBoolean(3, scheduleBillboards);
        statement.setBoolean(4, editUsers);
        statement.setInt(5, userId);

        statement.execute();
    }

    /**
     * Updates a user's password
     * @param userId - User ID to update password for
     * @param passwordHash - New password hash
     * @throws SQLException if an SQL exception occurs
     * @throws NoSuchAlgorithmException if adding the salt to the hashed password fails
     * @throws PasswordHashException if the hashing fails
     */
    public static void updatePassword(int userId, String passwordHash) throws SQLException, NoSuchAlgorithmException, PasswordHashException {
        Connection connection = DbConnection.getInstance();

        // Salt the password hash
        String salt = PasswordHash.generateSalt();
        String saltedHashedPassword = PasswordHash.addSaltToHash(passwordHash, salt);

        // Set the password
        PreparedStatement statement2 = connection.prepareStatement("UPDATE users SET password=?, salt=? WHERE userId=?");
        statement2.setString(1, saltedHashedPassword);
        statement2.setString(2, salt);
        statement2.setInt(3, userId);

        statement2.execute();
    }

    /**
     * Deletes a user
     * @param userId - User ID to delete
     * @throws SQLException if an SQL exception occurs
     */
    public static void deleteUser(int userId) throws SQLException {
        Connection connection = DbConnection.getInstance();

        // Delete all of the user's schedules
        // Get the user's schedules
        PreparedStatement statement = connection.prepareStatement("SELECT scheduleId FROM schedule WHERE userId=?");
        statement.setInt(1, userId);
        ResultSet rs = statement.executeQuery();

        // Delete each schedule
        while (rs.next()) {
            int scheduleId = rs.getInt("scheduleId");
            DbSchedule.deleteSchedule(scheduleId);
        }

        // Get the user's billboards
        PreparedStatement statement2 = connection.prepareStatement("SELECT billboardId FROM billboards WHERE userId=?");
        statement2.setInt(1, userId);
        ResultSet rs2 = statement2.executeQuery();

        // Delete each billboard (and the schedules for each)
        while (rs2.next()) {
            int billboardId = rs2.getInt("billboardId");
            DbSchedule.deleteSchedulesForBillboard(billboardId);
            DbBillboard.deleteBillboard(billboardId);
        }

        // Delete the user
        PreparedStatement statement3 = connection.prepareStatement("DELETE FROM users WHERE userId=?");
        statement3.setInt(1, userId);
        statement3.execute();
    }

    /**
     * Gets a user's user ID from their username
     * @param username - Username to get user ID for
     * @return User ID
     * @throws SQLException if an SQL exception occurs
     */
    public static Integer getUserId(String username) throws SQLException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT userId FROM users WHERE username = ?");
        statement.setString(1, username);

        ResultSet rs = statement.executeQuery();
        rs.next();

        return rs.getInt("userId");
    }
}
