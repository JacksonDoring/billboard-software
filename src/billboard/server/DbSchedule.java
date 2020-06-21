package billboard.server;

import billboard.controlpanel.ServerSchedules;

import java.sql.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * DB functions for interacting with schedules
 */
public class DbSchedule {
    /**
     * Adds a billboard schedule
     * @param billboardId - Billboard ID to add schedule for
     * @param day (1-7) - Billboard schedule day
     * @param minutesStart - Billboard schedule starting minutes
     * @param minutesDuration - Billboard schedule duration minutes
     * @param repeating - Billboard schedule repeating boolean
     * @param minutesRepeatGap - Billboard schedule repeat minutes gap
     * @param userId - Billboard schedule creator user ID
     * @throws SQLException if an SQL exception occurs
     */
    public static void addSchedule(int billboardId, int day, int minutesStart, int minutesDuration, boolean repeating, int minutesRepeatGap, int userId) throws SQLException {
        Connection connection = DbConnection.getInstance();

        Timestamp creationTime = new Timestamp(System.currentTimeMillis());

        PreparedStatement statement = connection.prepareStatement("INSERT INTO Schedule(billboardId, creationTime, userId, day, minutesStart, minutesDuration, repeating,  minutesRepeatGap) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
        statement.setInt(1, billboardId);
        statement.setTimestamp(2, creationTime);
        statement.setInt(3, userId);
        statement.setInt(4, day);
        statement.setInt(5, minutesStart);
        statement.setInt(6, minutesDuration);
        statement.setBoolean(7, repeating);
        statement.setInt(8, minutesRepeatGap);

        statement.execute();

        // Get the scheduleId
        PreparedStatement statement2 = connection.prepareStatement("SELECT scheduleId FROM Schedule ORDER BY scheduleId DESC LIMIT 1");

        ResultSet rs = statement2.executeQuery();
        rs.next();

        int scheduleId = rs.getInt("scheduleId");

        // Add to the timetable
        addScheduleToTimetable(scheduleId, billboardId, day, minutesStart, minutesDuration, repeating, minutesRepeatGap);
    }

    static final int MINUTES_IN_A_DAY = 60 * 24;
    static final int DAYS_IN_A_WEEK = 7;

    /**
     * Helper class for adding schedule times
     */
    static class ScheduleTime {
        public int day;
        public int startMinutes;
        public int endMinutes;

        public ScheduleTime(int day, int startMinutes, int endMinutes) {
            this.day = day;
            this.startMinutes = startMinutes;
            this.endMinutes = endMinutes;
        }
    }

    /**
     * Custom schedule time exception
     */
    static class ScheduleTimeException extends Exception {
        public ScheduleTimeException() {
            super();
        }
    }

    /**
     * Helper class for holding schedule times
     */
    static class ScheduleTimes {
        private ArrayList<ScheduleTime> times = new ArrayList<ScheduleTime>();

        public boolean addTime(int day, int startMinutes, int endMinutes) throws ScheduleTimeException {
            if (day > DAYS_IN_A_WEEK) { // We want to end now
                throw new ScheduleTimeException();
            }

            if (startMinutes == endMinutes) { // Pointless to add it
                return false;
            }

            times.add(new ScheduleTime(day, startMinutes, endMinutes));

            return true;
        }

        public ArrayList<ScheduleTime> getTimes() {
            return times;
        }
    }

    /**
     * Adds times in which a schedule should run
     * @param scheduleId
     * @param billboardId
     * @param day
     * @param minutesStart
     * @param minutesDuration
     * @param repeating
     * @param minutesRepeatGap
     * @throws SQLException if an SQL exception occurs
     */
    private static void addScheduleToTimetable(int scheduleId, int billboardId, int day, int minutesStart, int minutesDuration, boolean repeating, int minutesRepeatGap) throws SQLException {
        Connection connection = DbConnection.getInstance();

        ScheduleTimes times = new ScheduleTimes();

        int currentDay = day;

        int currentMinutesStart = minutesStart;

        boolean repeated = false;
        try {
            while (currentDay <= 7) {
                if (repeating && repeated) { // Repeated iteration, add on the gap
                    currentMinutesStart += minutesRepeatGap;

                    // Check to see if it's going on to the next day
                    while (currentMinutesStart > MINUTES_IN_A_DAY) { // Could wrap multiple days, so it's in a while loop
                        // Move values to the next day
                        currentDay++;
                        currentMinutesStart -= MINUTES_IN_A_DAY; // Reset to the start of the day
                    }
                }

                int currentMinutesEnd = currentMinutesStart + minutesDuration;

                // Check to see if the duration of the schedule showing will wrap over to the next day
                while (currentMinutesEnd > MINUTES_IN_A_DAY) { // Could wrap multiple days, so it's in a while loop
                    times.addTime(currentDay, currentMinutesStart, MINUTES_IN_A_DAY); // Add this current day's showing, just cap it to end at the end of the day

                    // Move values to the next day
                    currentDay++;
                    currentMinutesStart = 0; // Reset to the start of the day
                    currentMinutesEnd -= MINUTES_IN_A_DAY; // Reset to the start of the day, but keep the remainder, as this is the extra amount it is shown on the new day.
                }

                // Check if the new day is valid
                if (currentDay > 7) {
                    break;
                }

                // Add the final time for the day/repeat
                times.addTime(currentDay, currentMinutesStart, currentMinutesEnd);

                // If we aren't repeating, then we only want to run this once
                if (!repeating) {
                    break; // We are done adding times
                } else {
                    repeated = true; // Set that this is a repeated iteration
                }
            }
        } catch (ScheduleTimeException e) {}

        for (var time : times.getTimes()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO ScheduleTimes(scheduleId, day, startMinutes, endMinutes) VALUES (?, ?, ?, ?);");

            statement.setInt(1, scheduleId);
            statement.setInt(2, time.day);
            statement.setInt(3, time.startMinutes);
            statement.setInt(4, time.endMinutes);

            statement.execute();
        }
    }

    /**
     * Get the schedule data for a billboard
     * @return ArrayList containing each schedule's data
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if the request fails
     */
    public static ArrayList<HashMap> getAllSchedules() throws SQLException, DBException {
        Connection connection = DbConnection.getInstance();

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM Schedule");

        // list to hold the data from the result set
        ArrayList<HashMap> schedules = new ArrayList<HashMap>();
        while (rs.next()){
            int scheduleId = rs.getInt("scheduleId");
            int billboardId = rs.getInt("billboardId");

            String billboardName = DbBillboard.getBillboardName(billboardId);

            HashMap scheduleData = new HashMap();
            scheduleData.put("scheduleId", scheduleId);
            scheduleData.put("billboardId", rs.getInt("billboardId"));
            scheduleData.put("billboardName", billboardName);
            scheduleData.put("creationTime", rs.getTimestamp("creationTime"));
            scheduleData.put("userId", rs.getInt("userId"));
            scheduleData.put("day", rs.getInt("day"));
            scheduleData.put("minutesStart", rs.getInt("minutesStart"));
            scheduleData.put("minutesDuration", rs.getInt("minutesDuration"));
            scheduleData.put("repeating", rs.getBoolean("repeating"));
            scheduleData.put("minutesRepeatGap", rs.getInt("minutesRepeatGap"));

            PreparedStatement statement2 = connection.prepareStatement("SELECT * FROM ScheduleTimes WHERE scheduleId=?");
            statement2.setInt(1, scheduleId);
            ResultSet rs2 = statement2.executeQuery();

            ArrayList<HashMap> scheduleTimes = new ArrayList<HashMap>();
            while (rs2.next()){
                HashMap scheduleTimeData = new HashMap();
                scheduleTimeData.put("scheduleTimeId", rs2.getInt("scheduleTimeId"));
                scheduleTimeData.put("day", rs2.getInt("day"));
                scheduleTimeData.put("startMinutes", rs2.getInt("startMinutes"));
                scheduleTimeData.put("endMinutes", rs2.getInt("endMinutes"));

                scheduleTimes.add(scheduleTimeData);
            }
            scheduleData.put("scheduleTimes", scheduleTimes);

            schedules.add(scheduleData);
        }

        return schedules;
    }

    /**
     * Deletes the schedule for a specified schedule ID
     * @param selectedScheduleId - Schedule ID to delete
     * @throws SQLException if an SQL exception occurs
     */
    public static void deleteSchedule(int selectedScheduleId) throws SQLException {
        Connection connection = DbConnection.getInstance();

        // Delete all of the times when the schedule runs
        PreparedStatement statement = connection.prepareStatement("DELETE FROM ScheduleTimes WHERE scheduleId=?");
        statement.setInt(1, selectedScheduleId);

        statement.executeQuery();

        // Delete the schedule itself
        PreparedStatement statement2 = connection.prepareStatement("DELETE FROM Schedule WHERE scheduleId=?");
        statement2.setInt(1, selectedScheduleId);

        statement2.executeQuery();
    }

    /**
     * Deletes the schedules for a billboard
     * @param billboardId - Billboard ID to delete schedules for
     * @throws SQLException if an SQL exception occurs
     */
    public static void deleteSchedulesForBillboard(int billboardId) throws SQLException {
        Connection connection = DbConnection.getInstance();

        // Get the schedules
        PreparedStatement statement = connection.prepareStatement("SELECT scheduleId FROM schedule WHERE billboardId=?");
        statement.setInt(1, billboardId);
        ResultSet rs = statement.executeQuery();

        // Delete each schedule
        while (rs.next()) {
            int scheduleId = rs.getInt("scheduleId");

            // Delete all of the times when the schedule runs
            PreparedStatement statement2 = connection.prepareStatement("DELETE FROM ScheduleTimes WHERE scheduleId=?");
            statement2.setInt(1, scheduleId);
            statement2.executeQuery();

            // Delete the schedule itself
            PreparedStatement statement3 = connection.prepareStatement("DELETE FROM Schedule WHERE scheduleId=?");
            statement3.setInt(1, scheduleId);
            statement3.executeQuery();
        }
    }

    /**
     * Gets all the schedule data for a specific billboard ID
     * @param searchingBillboardId - Billboard ID to get schedule for
     * @return An array list with HashMaps inside for easy access of data
     * @throws SQLException if an SQL exception occurs
     * @throws DBException if the request fails
     */
    public static ArrayList<HashMap> getBillboardSchedule(Integer searchingBillboardId) throws SQLException, DBException {
        Connection connection = DbConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM Schedule WHERE billboardId=?");
        statement.setInt(1, searchingBillboardId);
        ResultSet rs = statement.executeQuery();

        // list to hold the data from the result set
        ArrayList<HashMap> schedules = new ArrayList<HashMap>();
        while (rs.next()){
            int scheduleId = rs.getInt("scheduleId");
            int billboardId = rs.getInt("billboardId");

            String billboardName = DbBillboard.getBillboardName(billboardId);

            HashMap scheduleData = new HashMap();
            scheduleData.put("scheduleId", scheduleId);
            scheduleData.put("billboardId", rs.getInt("billboardId"));
            scheduleData.put("billboardName", billboardName);
            scheduleData.put("creationTime", rs.getTimestamp("creationTime"));
            scheduleData.put("userId", rs.getInt("userId"));
            scheduleData.put("day", rs.getInt("day"));
            scheduleData.put("minutesStart", rs.getInt("minutesStart"));
            scheduleData.put("minutesDuration", rs.getInt("minutesDuration"));
            scheduleData.put("repeating", rs.getBoolean("repeating"));
            scheduleData.put("minutesRepeatGap", rs.getInt("minutesRepeatGap"));

            PreparedStatement statement2 = connection.prepareStatement("SELECT * FROM ScheduleTimes WHERE scheduleId=?");
            statement2.setInt(1, scheduleId);
            ResultSet rs2 = statement2.executeQuery();

            ArrayList<HashMap> scheduleTimes = new ArrayList<HashMap>();
            while (rs2.next()){
                HashMap scheduleTimeData = new HashMap();
                scheduleTimeData.put("scheduleTimeId", rs2.getInt("scheduleTimeId"));
                scheduleTimeData.put("day", rs2.getInt("day"));
                scheduleTimeData.put("startMinutes", rs2.getInt("startMinutes"));
                scheduleTimeData.put("endMinutes", rs2.getInt("endMinutes"));

                scheduleTimes.add(scheduleTimeData);
            }
            scheduleData.put("scheduleTimes", scheduleTimes);

            schedules.add(scheduleData);
        }

        return schedules;
    }

    /**
     * Get the current showing billboard
     * @return HashMap containing billboard name and data
     * @throws SQLException if an SQL exception occurs
     */
    public static HashMap getCurrentBillboard() throws SQLException {
        Connection connection = DbConnection.getInstance();

        LocalDateTime now = LocalDateTime.now();
        int currentDay = now.getDayOfWeek().getValue();
        int currentMinutes = now.getHour() * 60 + now.getMinute();

        System.out.println("Now is day " + currentDay + " minute " + currentMinutes);

        PreparedStatement statement = connection.prepareStatement("SELECT b.name, b.data FROM Schedule as s " +
                                                                        "INNER JOIN ScheduleTimes as st ON st.scheduleId = s.scheduleId " +
                                                                        "INNER JOIN Billboards as b ON b.billboardId = s.billboardId " +
                                                                        "WHERE st.day = ? AND st.startMinutes < ? AND st.endMinutes > ? " +
                                                                        "ORDER BY st.scheduleTimeId DESC");
        statement.setInt(1, currentDay);
        statement.setInt(2, currentMinutes);
        statement.setInt(3, currentMinutes);
        ResultSet rs = statement.executeQuery();

        if (!rs.next()) {
            // No billboard is currently showing

            /*
                "If there is no billboard scheduled at a particular time, the Server should send back
                something else for the Viewer to display in the meantime. Your team should decide
                on something appropriate."
             */

            HashMap currentBillboardData = new HashMap();
            currentBillboardData.put("name", "Temporary billboard");
            currentBillboardData.put("data", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<billboard>\n" +
                    "    <message>No billboard is currently showing.</message>\n" +
                    "</billboard>\n");

            return currentBillboardData;
        }

        HashMap currentBillboardData = new HashMap();
        currentBillboardData.put("name", rs.getString("b.name"));
        currentBillboardData.put("data", rs.getString("b.data"));

        return currentBillboardData;
    }
}