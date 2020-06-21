package billboard.controlpanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * GUI for scheduling a billboard
 */
public class ScheduleBillboards {
    private JPanel panel;
    private JComboBox day;
    private JComboBox time;
    private JButton scheduleButton;
    private JTextField duration;
    private JLabel title;
    private JTextField repeatMinutes;
    private JLabel repeatMinutesLabel;
    private JComboBox repeatOptions;
    private JCheckBox repeatCheckBox;

    /**
     * Main GUI
     * @param billboardId - Billboard ID to schedule
     */
    public ScheduleBillboards(int billboardId){
        JFrame frame = new JFrame("Schedule Billboards");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        repeatMinutes.setVisible(false);
        repeatMinutesLabel.setVisible(false);

        try {
            title.setText("Scheduling Billboard: " + ServerBillboards.getBillboardName(billboardId));
        } catch (BillboardServerException e) {
            e.printStackTrace();
        }

        //We should test to see if the start time, duration, and custom repeat minutes they entered is the format we want to pass to the severchanges
        repeatOptions.addActionListener(e -> {
            if (repeatOptions.getSelectedItem() == "Custom"){
                repeatMinutes.setVisible(true);
                repeatMinutesLabel.setVisible(true);
            } else {
                repeatMinutes.setVisible(false);
                repeatMinutesLabel.setVisible(false);
            }
        });

        scheduleButton.addActionListener(e -> {
            Integer day = this.day.getSelectedIndex() + 1;

            Date selectedTime = null;
            int repeatMin = 0;
            int timeSinceMidnight = 0;
            int duration = 0;
            boolean repeat = false;

            //converting the time supplied by user into a time object
            try {
                selectedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(LocalDate.now() + " " + time.getSelectedItem().toString());

                ZonedDateTime nowZoned = ZonedDateTime.now();
                Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
                //getting the time in seconds from midnight
                long midnightTime = midnight.getEpochSecond();
                long timeSelected = selectedTime.getTime() / 1000;

                //gets the time selected from midnight to the selected time
                timeSinceMidnight = (int) ((timeSelected - midnightTime) / 60);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(frame,  "Please make sure the time is in format HH:MM and is it 24 hour time.");

                return;
            }

            //parsing the duration as an integer
            try{
                duration = Integer.parseInt(this.duration.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,  "Please make sure the minutes is a number only");
                return;
            }

            //parsing the repeat minutes if the custom option is selected
            if (repeatOptions.getSelectedItem() == "Custom"){
                try{
                    repeatMin = Integer.parseInt(repeatMinutes.getText());
                    repeat = true;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,  "Please make sure the repeat minutes is a number only");
                    return;
                }
            }
            else if(repeatOptions.getSelectedItem() == "Every Day"){
                repeatMin = 60 * 24;
                repeat = true;
            }
            else if(repeatOptions.getSelectedItem() == "Every Hour"){
                repeatMin = 60;
                repeat = true;
            }

            if(duration > repeatMin && repeatOptions.getSelectedItem() != "None"){
                JOptionPane.showMessageDialog(frame,  "The duration cannot be longer than the repeat cycle");
                return;
            }

            try {
                ServerSchedules.addSchedule(billboardId, day, timeSinceMidnight, duration, repeat, repeatMin);
            } catch (BillboardServerException ex) {
                ex.printStackTrace();
            }

            try {
                JOptionPane.showMessageDialog(frame, "Schedule Added for " + ServerBillboards.getBillboardName(billboardId));
            } catch (BillboardServerException ex) {
                ex.printStackTrace();
            }

            frame.dispose();
        });
    }
}
