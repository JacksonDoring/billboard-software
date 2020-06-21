package billboard.controlpanel;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Billboard schedule timetable GUI element
 */
public class ScheduleTimetable extends JPanel {
    static final int DAYS_IN_A_WEEK = 7;
    static final int MINUTES_IN_A_DAY = 1440;

    static final int DAY_PADDING = 10;
    static final int TIMETABLE_TOP_PADDING = 90;
    static final int TIMETABLE_LEFT_PADDING = 80;
    static final int TIMETABLE_RIGHT_PADDING = 30;
    static final int TIMETABLE_BOTTOM_PADDING = 30;
    static final int TIMETABLE_HEADERS_PADDING = 10;

    ArrayList<ScheduleTimetableCard> cards = new ArrayList<ScheduleTimetableCard>();

    /**
     * Creates a new billboard schedule timetable from a list of schedules
     * @param schedules - ArrayList of HashMaps containing schedule data
     */
    public ScheduleTimetable(ArrayList<HashMap> schedules) {
        for (var scheduleData : schedules) {
            int billboardId = (int)scheduleData.get("billboardId");
            String billboardName = (String)scheduleData.get("billboardName");

            ArrayList<HashMap> scheduleTimes = (ArrayList<HashMap>) scheduleData.get("scheduleTimes");

            String creatorName = null;
            try {
                creatorName = ServerBillboards.getBillboardCreatorName(billboardId);
            } catch (BillboardServerException e) {
                creatorName = "Unknown user";
            }

            for (var scheduleTimeData : scheduleTimes) {
                int scheduleTimeId = (int)scheduleTimeData.get("scheduleTimeId");

                int day = (int)scheduleTimeData.get("day");

                int startMinutes = (int)scheduleTimeData.get("startMinutes");
                int endMinutes = (int)scheduleTimeData.get("endMinutes");

                cards.add(new ScheduleTimetableCard(day, startMinutes, endMinutes, new Color(35, 166, 222), Color.black, billboardName, creatorName, scheduleTimeId));
            }
        }
    }

    /**
     * Draws the days across the top of the timetable
     * @param g - Swing graphics object
     * @param timetableArea - Rectangle to draw timetable in
     */
    public void paintDays(Graphics g, Rectangle timetableArea) {
        // Paint days across the top (monday-sunday)
        for (int day = 0; day < DAYS_IN_A_WEEK; day++) {
            String text = DayOfWeek.values()[day].getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            int textWidth = (int)(g.getFontMetrics().getStringBounds(text, g).getWidth());
            int textHeight = (int)(g.getFontMetrics().getStringBounds(text, g).getHeight());

            int x = timetableArea.x + ScheduleTimetable.DAY_PADDING / 2 + (int)(day / (float)ScheduleTimetable.DAYS_IN_A_WEEK * timetableArea.width) + textWidth / 2;
            int y = timetableArea.y - TIMETABLE_HEADERS_PADDING;

            g.drawString(text, x, y);
        }
    }

    /**
     * Draws the times across the side of the timetable
     * @param g - Swing graphics object
     * @param timetableArea - Rectangle to draw timetable in
     */
    public void paintTimes(Graphics g, Rectangle timetableArea) {
        // Print times (every hour should be good) along the side
        for (int hour = 0; hour <= 24; hour++) {
            String time = LocalTime.parse(Integer.toString(hour), DateTimeFormatter.ofPattern("H", Locale.US)).format(DateTimeFormatter.ofPattern("hh:mm a"));

            int textWidth = (int)g.getFontMetrics().getStringBounds(time, g).getWidth();
            int textHeight = (int)g.getFontMetrics().getStringBounds(time, g).getHeight();

            float hourHeight = (float)timetableArea.height / 24;

            int x = timetableArea.x - TIMETABLE_HEADERS_PADDING;
            int y = timetableArea.y + (int)(hour * hourHeight);

            g.drawString(time, x - textWidth, y + textHeight / 2 - 2);

            g.drawLine(timetableArea.x, y, x + timetableArea.width, y);
        }
    }

    /**
     * Renders the timetable
     * @param g - Swing graphics object
     */
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        // Header
        String header = "Schedules Timetable";

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));

        int headerWidth = (int)g.getFontMetrics().getStringBounds(header, g).getWidth();
        int headerHeight = (int)g.getFontMetrics().getStringBounds(header, g).getHeight();

        g.drawString(header, getWidth() / 2 - headerWidth / 2, 15 + headerHeight);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        // Timetable border
        int x = TIMETABLE_LEFT_PADDING;
        int y = TIMETABLE_TOP_PADDING;
        int w = getWidth() - TIMETABLE_LEFT_PADDING - TIMETABLE_RIGHT_PADDING;
        int h = getHeight() - TIMETABLE_TOP_PADDING -  TIMETABLE_BOTTOM_PADDING;

        Rectangle timetableArea = new Rectangle(x, y, w, h);

        // Days across top
        paintDays(g, timetableArea);

        // Times across side
        paintTimes(g, timetableArea);

        g.setColor(new Color(0.5f, 0.5f, 0.5f));
        g.drawRect(x, y, w, h);

        for (var card : cards) {
            card.draw(g, timetableArea);
        }
    }
}