package billboard.controlpanel;

import javax.swing.*;

/**
 * GUI for viewing the billboard schedule timetable
 */
public class ScheduleViewer {
    /**
     * Main GUI
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public ScheduleViewer() throws BillboardServerException {
        JFrame frame = new JFrame("View Schedule");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(900, 800 );
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        var schedules = ServerSchedules.getAllSchedules();

        ScheduleTimetable cd = new ScheduleTimetable(schedules);
        frame.add(cd);
    }
}