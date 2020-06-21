package billboard.controlpanel;

import billboard.server.ServerResponse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class for sending requests to the server relating to billboards
 */
public class ServerBillboards {
    /**
     * Gets a list of billboards
     * @return List of billboards
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static ArrayList<HashMap> listBillboards() throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("listBillboards");
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        var billboards = (ArrayList<HashMap>) response.data;

        return billboards;
    }

    /**
     * Creates a billboard
     * @param billboardName - Billboard name
     * @param billboardData - Billboard data
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static void createBillboard(String billboardName, String billboardData) throws BillboardServerException {
        HashMap data = new HashMap();
        data.put("billboardName", billboardName);
        data.put("billboardData", billboardData);

        ServerResponse response = ServerConnection.sendRequest("createBillboard", data);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }
    }

    /**
     * Updates a billboard's information
     * @param billboardName - New billboard name
     * @param billboardData - New billboard data
     * @param billboardId - Updating billboard ID
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static void updateBillboard(String billboardName, String billboardData, Integer billboardId) throws BillboardServerException {
        HashMap data = new HashMap();
        data.put("billboardName", billboardName);
        data.put("billboardData", billboardData);
        data.put("billboardId", billboardId);

        ServerResponse response = ServerConnection.sendRequest("updateBillboard", data);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }
    }

    /**
     * Gets all the billboard data from the billboards ID that is supplied
     * @param billboardId - Billboard ID to get the data for
     * @return A String of the the billboard data
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static String getBillboardData(int billboardId) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getBillboardData", billboardId);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        String data = (String) response.data;

        return data;
    }

    /**
     * Gets a billboard's name
     * @param billboardId - Billboard ID to get the billboard name for
     * @return Billboard name
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static String getBillboardName(int billboardId) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getBillboardName", billboardId);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        String data = (String) response.data;

        return data;
    }

    /**
     * Gets a billboard's ID
     * @param billboardName - Billboard name to get the billboard ID for
     * @return Billboard ID
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static Integer getBillboardId(String billboardName) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getBillboardId", billboardName);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        Integer data = (Integer) response.data;

        return data;
    }

    /**
     * Gets the username of a billboard's creator
     * @param billboardId - Billboard ID to get the creator for
     * @return Billboard creator's username
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static String getBillboardCreatorName(int billboardId) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getBillboardCreatorName", billboardId);
        if (response.error != null) {
            return "N/A";
        }

        String data = (String) response.data;

        return data;
    }

    /**
     * Checks if a billboard name already exists
     * @param billboardName - Billboard name to check
     * @return Exist status
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static boolean billboardNameExists(String billboardName) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("billboardNameExists", billboardName);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        boolean data = (boolean) response.data;

        return data;
    }

    /**
     * Deletes the specified billboard from the database
     * @param billboardId - Billboard ID to delete
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static void deleteBillboard(int billboardId) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("deleteBillboard", billboardId);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }
    }
}