package billboard.controlpanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * GUI for listing schedules
 */
public class ScheduleOptions extends JFrame {
    private BillboardPreview preview = new BillboardPreview();

    /**
     * Converts a time from minutes to HH:mm
     * @param minutes - Minutes to convert
     * @return Formatted time
     */
    public static String getTimeFromMins(int minutes){
        int mins = minutes;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, mins/60);
        cal.set(Calendar.MINUTE, mins % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String hhmm = sdf.format(cal.getTime());
        return hhmm;
    }

    /**
     * Main GUI
     */
    public ScheduleOptions() {
        JFrame frame = new JFrame("Schedule Billboards");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20,30,20,30));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        frame.getContentPane().add(panel);

        JLabel mainLabel = new JLabel("Schedule Billboards");
        mainLabel.setFont(new Font(mainLabel.getFont().getName(), Font.BOLD, 16));
        panel.add(mainLabel, gbc);

        JPanel billboardList = new JPanel();
        billboardList.setLayout(new GridLayout(0, 1));

        JScrollPane scrollPane = new JScrollPane(billboardList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        panel.add(scrollPane, gbc);

        JPanel billboardButtons = new JPanel();
        panel.add(billboardButtons, gbc);

        JButton scheduleBillboard = new JButton("Schedule Billboard");
        billboardButtons.add(scheduleBillboard);

        JButton deleteSchedule = new JButton("Billboard Schedule");
        billboardButtons.add(deleteSchedule);

        JButton viewSchedule = new JButton("View Schedule");
        billboardButtons.add(viewSchedule);

        ButtonGroup radioButtons = new ButtonGroup();

        // Get the user's own billboards from server
        try {
            ArrayList<HashMap> billboards = ServerBillboards.listBillboards();
            billboards.forEach((billboardData) -> {
                boolean ourBillboard = (Integer) billboardData.get("creatorId") == Token.getUserId();
                String creatorName = (String) billboardData.get("creatorUsername");

                // Add radio button
                JRadioButton button = new JRadioButton(billboardData.get("name") + (ourBillboard ? " (yours)" : " (creator: " + creatorName + ")"));
                button.setActionCommand(Integer.toString((Integer) billboardData.get("billboardId")));
                radioButtons.add(button);
                billboardList.add(button);

                billboardButtons.setVisible(true);
            });
        } catch (BillboardServerException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(frame, e.getMessage());
        }

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        scheduleBillboard.addActionListener(e -> {
            if (radioButtons.getSelection() == null) {
                return;
            }

            int selectedBillboardId = Integer.parseInt(radioButtons.getSelection().getActionCommand());

            new ScheduleBillboards(selectedBillboardId);
        });

        viewSchedule.addActionListener(e -> {
            try {
                new ScheduleViewer();
            } catch (BillboardServerException ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        deleteSchedule.addActionListener(e -> {
            if (radioButtons.getSelection() == null) {
                return;
            }

            int selectedBillboardId = Integer.parseInt(radioButtons.getSelection().getActionCommand());

            new DeleteSchedules(selectedBillboardId);
        });
    }
}