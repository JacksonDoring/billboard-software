package billboard.server;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * DB functions for interacting with billboards
 */
public class DbBillboard {
    /**
     * Creates a new billboard
     * @param billboardName - New billboard name
     * @param billboardData - New billboard data
     * @param userId - Billboard creator user ID
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if a custom DB error occurs
     */
    public static void createBillboard(String billboardName, String billboardData, Integer userId) throws SQLException, DBException {
        if (billboardName.isEmpty()) {
            throw new DBException("Billboard name cannot be empty");
        }

        if (billboardNameExists(billboardName)) {
            throw new DBException("Billboard name already exists");
        }

        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("INSERT INTO billboards (name, data, userId) VALUES (?, ?, ?);");
        statement.setString(1, billboardName);
        statement.setString(2, billboardData);
        statement.setInt(3, userId);

        statement.execute();
    }

    /**
     * Check if a billboard name already exists
     * @param billboardName - Billboard name to check
     * @return If the billboard name exists
     * @throws SQLException if an SQL exception occurs
     */
    public static boolean billboardNameExists(String billboardName) throws SQLException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM billboards WHERE name=?;");
        statement.setString(1, billboardName);

        ResultSet rs = statement.executeQuery();

        return rs.next(); // If there's a result returned, then this will be true
    }

    /**
     * Gets all of the billboards
     * @return ArrayList containing a HashMap with billboard ids and names
     * @throws SQLException if an SQL exception occurs
     */
    public static ArrayList<HashMap> listBillboards() throws SQLException {
        Connection connection = DbConnection.getInstance();

        Statement statement = connection.createStatement();

        ResultSet rs = statement.executeQuery("SELECT b.*, u.username FROM billboards as b " +
                "INNER JOIN users as u ON u.userId = b.userId;");

        // list to hold the data from the result set
        ArrayList<HashMap> billboards = new ArrayList<HashMap>();
        while (rs.next()){
            HashMap billboardData = new HashMap();
            billboardData.put("billboardId", rs.getInt("billboardId"));
            billboardData.put("name", rs.getString("name"));
            billboardData.put("creatorId", rs.getInt("userId"));
            billboardData.put("creatorUsername", rs.getString("username"));

            billboards.add(billboardData);
        }

        return billboards;
    }

    /**
     * Gets all of the billboards made by a specific user
     * @param userId - User ID to get billboards for
     * @return HashMap of billboard ids and billboard names
     * @throws SQLException if an SQL exception occurs
     */
    public static HashMap<Integer, String> listUserBillboards(int userId) throws SQLException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT billboardId, name FROM billboards WHERE userId=?");
        statement.setInt(1, userId);

        ResultSet rs = statement.executeQuery();

        // list to hold the data from the result set
        HashMap<Integer, String> billboards = new HashMap<Integer, String>();
        while (rs.next()){
            billboards.put(rs.getInt("billboardId"), rs.getString("name"));
        }

        return billboards;
    }

    /**
     * Gets data for a given billboard
     * @param billboardId - Billboard ID to get data for
     * @return HashMap of billboard data
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if the request fails
     */
    public static String getBillboardData(int billboardId) throws SQLException, DBException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT data FROM billboards WHERE billboardId=?;");
        statement.setInt(1, billboardId);

        ResultSet rs = statement.executeQuery();
        if (!rs.next()) {
            throw new DBException("Billboard does not exist");
        }

        String data = rs.getString("data");

        return data;
    }

    /**
     * Gets a billboard's name
     * @param billboardId - Billboard ID to get the billboard name for
     * @return Billboard name
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if the request fails
     */
    public static String getBillboardName(int billboardId) throws SQLException, DBException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT name FROM billboards WHERE billboardId=?;");
        statement.setInt(1, billboardId);

        ResultSet rs = statement.executeQuery();
        if (!rs.next()) {
            throw new DBException("Billboard does not exist");
        }

        String data = rs.getString("name");

        return data;
    }

    /**
     * Gets a billboard's ID
     * @param billboardName - Billboard name to get the billboard ID for
     * @return Billboard ID
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if the request fails
     */
    public static int getBillboardId(String billboardName) throws SQLException, DBException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT billboardId FROM billboards WHERE name=?;");
        statement.setString(1, billboardName);

        ResultSet rs = statement.executeQuery();
        if (!rs.next()) {
            throw new DBException("Billboard does not exist");
        }

        int data = rs.getInt("billboardId");

        return data;
    }

    /**
     * Gets the creator user id of a billboard
     * @param billboardId - Billboard ID to get the creator's user ID for
     * @return Creator's user ID
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if the request fails
     */
    public static int getBillboardCreatorId(int billboardId) throws SQLException, DBException {
        Connection connection = DbConnection.getInstance();

        // Delete all of the times when the schedule runs
        PreparedStatement statement = connection.prepareStatement("SELECT userId FROM Billboards WHERE billboardId=?");
        statement.setInt(1, billboardId);
        ResultSet rs = statement.executeQuery();

        // list to hold the data from the result set
        ArrayList<HashMap> schedules = new ArrayList<HashMap>();
        if (!rs.next()) {
            throw new DBException("Billboard does not exist");
        }

        int userId = rs.getInt("userId");

        return userId;
    }

    /**
     * Get the creator username of a billboard
     * @param billboardId - Billboard ID to get the creator's username for
     * @return Creator's username
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if the request fails
     */
    public static String getBillboardCreatorName(int billboardId) throws SQLException, DBException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT u.username FROM billboards as b " +
                "INNER JOIN users as u ON u.userId = b.userId " +
                "WHERE b.billboardId=?;");

        statement.setInt(1, billboardId);

        ResultSet rs = statement.executeQuery();
        if (!rs.next()) {
            throw new DBException("Error getting billboard creator");
        }

        String username = rs.getString("username");

        return username;
    }

    /**
     * Updates a billboard's data
     * @param billboardName - New billboard name
     * @param billboardData - New billboard data
     * @param billboardId - Billboard ID to edit
     * @return success
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if the request fails
     */
    public static boolean updateBillboard(String billboardName, String billboardData, Integer billboardId) throws SQLException, DBException {
        if (billboardName.isEmpty()) {
            throw new DBException("Billboard name cannot be empty");
        }

        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("UPDATE billboards SET name=?, data=? WHERE billboardId=?");
        statement.setString(1, billboardName);
        statement.setString(2, billboardData);
        statement.setInt(3, billboardId);

        statement.execute();

        return true;
    }

    /**
     * Deletes a billboard
     * @param billboardId - billboard ID to delete
     * @return success
     * @throws SQLException if an SQL exception occurs
     */
    public static boolean deleteBillboard(int billboardId) throws SQLException {
        Connection connection = DbConnection.getInstance();

       //PreparedStatement statement = connection.prepareStatement("DELETE FROM billboards WHERE billboardId=? AND userId=?");

        PreparedStatement statement = connection.prepareStatement("SELECT scheduleId FROM schedule WHERE billboardId=?");
        statement.setInt(1, billboardId);
        ResultSet rs = statement.executeQuery();

        //If the billboard has no schedules
        if (!rs.next()) {
            PreparedStatement statement3 = connection.prepareStatement("DELETE FROM billboards WHERE billboardId=?");
            statement3.setInt(1, billboardId);
            statement3.execute();
        }
        //Deleting the schedules for that billboard
        else {

            int scheduleId = rs.getInt("scheduleId");
            System.out.println(scheduleId);

            PreparedStatement statement2 = connection.prepareStatement("DELETE FROM scheduletimes WHERE scheduleId=?");
            statement2.setInt(1, scheduleId);
            statement2.execute();

            PreparedStatement statement4 = connection.prepareStatement("DELETE FROM schedule WHERE billboardId=?");
            statement4.setInt(1, billboardId);
            statement4.execute();

            PreparedStatement statement3 = connection.prepareStatement("DELETE FROM billboards WHERE billboardId=?");
            statement3.setInt(1, billboardId);
            statement3.execute();
        }

        return true;
    }
}
