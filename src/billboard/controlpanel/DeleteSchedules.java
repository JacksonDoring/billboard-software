package billboard.controlpanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * GUI for deleting schedules
 */
public class DeleteSchedules extends JFrame {
    private BillboardPreview preview = new BillboardPreview();

    /**
     * Main GUI
     * @param billboardId - billboard id
     */
    public DeleteSchedules(int billboardId) {
        JFrame frame = new JFrame("Delete Schedules");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20,30,20,30));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        frame.getContentPane().add(panel);

        JLabel mainLabel = null;
        try {
            mainLabel = new JLabel("Delete Schedules for " + ServerBillboards.getBillboardName(billboardId));
        } catch (BillboardServerException e) {
            mainLabel = new JLabel("Delete Schedules for billboard");
        }

        mainLabel.setFont(new Font(mainLabel.getFont().getName(), Font.BOLD, 16));
        panel.add(mainLabel, gbc);

        JPanel billboardList = new JPanel();
        billboardList.setLayout(new GridLayout(0, 1));

        JScrollPane scrollPane = new JScrollPane(billboardList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        panel.add(scrollPane, gbc);

        JPanel billboardButtons = new JPanel();
        panel.add(billboardButtons, gbc);


        JButton deleteSchedule = new JButton("Delete Schedule");
        billboardButtons.add(deleteSchedule);

        ButtonGroup radioButtons = new ButtonGroup();

        // Get the user's own billboards from server
        try {
            ArrayList<HashMap> billboardSchedules = ServerSchedules.getBillboardSchedule(billboardId);

            System.out.println(billboardSchedules);

            billboardSchedules.forEach((schedule) -> {
                //getting the required data from the hashmap
                int minutesStart = (int) schedule.get("minutesStart");
                int minutesDuration = (int) schedule.get("minutesDuration");
                int day = (int) schedule.get("day");
                boolean repeating = (boolean) schedule.get("repeating");
                int minutesRepeatGap = (int) schedule.get("minutesRepeatGap");

                //converting int date to string day
                String[] strDaysArr = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                String dayString = strDaysArr[day - 1];

                // converting the minutes of the start to 24 hour time
                String startTime = ScheduleOptions.getTimeFromMins(minutesStart);

                // Add radio button
                JRadioButton button = new JRadioButton(dayString + " - Start time: " + startTime + ", duration: " + minutesDuration + " minutes" + (repeating ? ", repeating every " + minutesRepeatGap + " minutes" : ""));
                button.setActionCommand(Integer.toString((Integer) schedule.get("scheduleId")));
                radioButtons.add(button);
                billboardList.add(button);
            });
        } catch (BillboardServerException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage());
        }

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        deleteSchedule.addActionListener(e -> {
            int scheduleId = Integer.parseInt(radioButtons.getSelection().getActionCommand());
            try{
                ServerSchedules.deleteSchedule(scheduleId);
                JOptionPane.showMessageDialog(frame, "Billboard schedule deleted successfully");

                frame.dispose();

                new DeleteSchedules(billboardId);
            } catch (BillboardServerException ex) {
                JOptionPane.showMessageDialog(frame, "Error when deleting billboard");
            }
        });
    }
}