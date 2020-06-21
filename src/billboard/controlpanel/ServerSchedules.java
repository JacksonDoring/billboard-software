package billboard.controlpanel;

import billboard.server.ServerResponse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class for sending requests to the server relating to schedules
 */
public class ServerSchedules {
    /**
     * Adds a schedule for a billboard
     * @param billboardId - Billboard ID to add schedule for
     * @param day - Schedule day
     * @param minutesStart - Schedule minutes start
     * @param minutesDuration - Schedule minutes duration
     * @param repeating - If the schedule is repeating or not
     * @param minutesRepeatGap - Schedule repeat minutes gap
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static void addSchedule(int billboardId, int day, int minutesStart, int minutesDuration, boolean repeating, int minutesRepeatGap) throws BillboardServerException {
        HashMap data = new HashMap();
        data.put("billboardId", billboardId);
        data.put("day", day);
        data.put("minutesStart", minutesStart);
        data.put("minutesDuration", minutesDuration);
        data.put("repeating", repeating);
        data.put("minutesRepeatGap", minutesRepeatGap);

        ServerResponse response = ServerConnection.sendRequest("addSchedule", data);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }
    }

    /**
     * Gets all of the scheduled billboards and times
     * @return ArrayList of HashMaps containing billboard data
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static ArrayList<HashMap> getAllSchedules() throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getAllSchedules", null);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        return (ArrayList<HashMap>) response.data;
    }

    /**
     * Gets the currently scheduled billboard
     * @return HashMap containing the current billboard's data
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static HashMap getCurrentBillboard() throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getCurrentBillboard", null);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        HashMap currentBillboard = (HashMap) response.data;

        return currentBillboard;
    }

    /**
     * Gets the schedule for a billboard
     * @param searchingBillboardId - Billboard ID to get schedule for
     * @return Billboard schedule
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static ArrayList<HashMap> getBillboardSchedule(Integer searchingBillboardId) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("getBillboardSchedule", searchingBillboardId);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }

        ArrayList<HashMap> billboardData = (ArrayList) response.data;

        return billboardData;
    }

    /**
     * Deletes a schedule
     * @param scheduleId - Schedule ID to delete
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static void deleteSchedule(Integer scheduleId) throws BillboardServerException {
        ServerResponse response = ServerConnection.sendRequest("deleteSchedule", scheduleId);
        if (response.error != null) {
            throw new BillboardServerException(response.error);
        }
    }
}
