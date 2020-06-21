package billboard.server;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;

/**
 * DB functions for interacting with authentication
 */
public class DbLogin {
    /**
     * Creates a login session for the given userid
     * @param userId - user ID to create a login session for
     * @return Session key
     * @throws SQLException if an SQL exception occurs
     * @throws NoSuchAlgorithmException if creating the session key fails
     * @throws PasswordHashException if the hashing fails
     */
    public static String createLoginSession(int userId) throws SQLException, NoSuchAlgorithmException, PasswordHashException {
        final long sessionDurationMs = 1000 * 60 * 60 * 24; // sessions expire after 24 hours

        // set up session variables
        String hash = PasswordHash.hashString(String.valueOf(System.currentTimeMillis()));// Hash current time (will give a unique hash)

        // Salt the hash as well
        String salt = PasswordHash.generateSalt();

        String sessionKey = PasswordHash.addSaltToHash(hash, salt);

        Timestamp expiryDate = new Timestamp(System.currentTimeMillis() + sessionDurationMs);

        // add the session to the db
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("INSERT INTO Sessions(userId, sessionKey, expiry) VALUES (?, ?, ?);");
        statement.setInt(1, userId);
        statement.setString(2, sessionKey);
        statement.setTimestamp(3, expiryDate);
        statement.executeQuery();

        // return the session key to send to the user
        return sessionKey;
    }

    /**
     * Tries to log in a user using given details
     * @param username - Username to login with
     * @param passwordHash - Password hash to login with
     * @return HashMap containing the session key and userid
     * @throws SQLException if an SQL exception occurs
     * @throws NoSuchAlgorithmException if adding the salt to the hashed password fails
     * @throws DBException if a custom DB error occurs
     * @throws PasswordHashException if the hashing fails
     */
    public static HashMap loginUser(String username, String passwordHash) throws SQLException, NoSuchAlgorithmException, DBException, PasswordHashException {
        // Check if the username exists
        if (!DbUser.usernameExists(username)) {
            throw new DBException("Username not registered");
        }

        Connection connection = DbConnection.getInstance();

        // Get the real password and salt
        PreparedStatement statement = connection.prepareStatement("SELECT userid, password, salt FROM users WHERE username=?;");
        statement.setString(1, username);

        ResultSet rs = statement.executeQuery();
        rs.next();

        int userId = rs.getInt("userid");
        String realPasswordHash = rs.getString("password");
        String salt = rs.getString("salt");

        // Add the salt to the hashed password
        String saltedPasswordHash = PasswordHash.addSaltToHash(passwordHash, salt);

        // Check if the hash of the password given is the same as the real password hash
        if (!saltedPasswordHash.equals(realPasswordHash)) {
            throw new DBException("Password incorrect");
        }

        String sessionKey = createLoginSession(userId);

        HashMap data = new HashMap();
        data.put("sessionKey", sessionKey);
        data.put("userId", userId);

        return data;
    }

    /**
     * Logs out a user via a login token
     * @param token - Token to log out
     * @throws SQLException if an SQL exception occurs
     */
    public static void logoutUser(String token) throws SQLException {
        Connection connection = DbConnection.getInstance();

        // Get the real password
        PreparedStatement statement = connection.prepareStatement("DELETE FROM Sessions WHERE sessionKey=?");

        statement.setString(1, token);

        statement.execute();
    }

    /**
     * Deletes expired session tokens
     * @throws SQLException if an SQL exception occurs
     */
    public static void deleteOldTokens() throws SQLException {
        Connection connection = DbConnection.getInstance();

        // Delete tokens that have expired
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("DELETE FROM sessions WHERE expiry < NOW()");
    }

    /**
     * Checks if a session token is valid
     * @param token - Token to check
     * @return owner's userid of the token (if valid)
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if a custom DB error occurs
     */
    public static int checkToken(String token) throws SQLException, DBException {
        // Check if the token is empty first off
        if (token == null || token.isEmpty()) {
            throw new DBException("No token supplied");
        }

        Connection connection = DbConnection.getInstance();

        // Delete old tokens
        deleteOldTokens();

        // Get a token that is valid
        PreparedStatement statement = connection.prepareStatement("SELECT userId FROM Sessions WHERE sessionKey=?");
        statement.setString(1, token);

        ResultSet rs = statement.executeQuery();

        boolean validToken = rs.next();

        if (!validToken) {
            throw new DBException("Token invalid");
        }

        int userId = rs.getInt("userId");
        return userId;
    }
}