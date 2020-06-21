package billboard.controlpanel;

import java.awt.*;

/**
 * Class for handling a billboard schedule timetable card
 */
public class ScheduleTimetableCard {
    private int day;
    private int startMinutes;
    private int endMinutes;
    private Color cardColor;
    private Color textColor;
    private String billboardName;
    private String billboardCreator;
    private int scheduleTimeId;

    /**
     * Creates a new timetable card
     * @param day - Schedule day
     * @param startMinutes - Schedule start minutes
     * @param endMinutes - Schedule end minutes
     * @param cardColor - Schedule card colour
     * @param textColor - Schedule card text colour
     * @param billboardName - Schedule billboard name
     * @param billboardCreator - Schedule billboard creator name
     * @param scheduleTimeId - Schedule time ID
     */
    public ScheduleTimetableCard(int day, int startMinutes, int endMinutes, Color cardColor, Color textColor, String billboardName, String billboardCreator, int scheduleTimeId) {
        this.day = day;
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
        this.cardColor = cardColor;
        this.textColor = textColor;
        this.billboardName = billboardName;
        this.billboardCreator = "Creator: " + billboardCreator;
        this.scheduleTimeId = scheduleTimeId;
    }

    /**
     * Renders the timetable card
     * @param graphics - Swing graphics object
     * @param timetableArea - Rectangle to draw timetable in
     */
    public void draw(Graphics graphics, Rectangle timetableArea) {
        int day_width = timetableArea.width / ScheduleTimetable.DAYS_IN_A_WEEK - ScheduleTimetable.DAY_PADDING;

        int x = timetableArea.x + ScheduleTimetable.DAY_PADDING / 2 + (int)((day - 1) / (float)ScheduleTimetable.DAYS_IN_A_WEEK  * timetableArea.width);

        int y = timetableArea.y + (int)((float)startMinutes / ScheduleTimetable.MINUTES_IN_A_DAY * timetableArea.height);
        int y2 = timetableArea.y + (int)((float)endMinutes / ScheduleTimetable.MINUTES_IN_A_DAY * timetableArea.height);

        int height = y2 - y;

        // Card fill
        graphics.setColor(cardColor);
        graphics.fillRect(x, y, day_width, height);

        // Card border
        graphics.setColor(Color.black);
        graphics.drawRect(x, y, day_width, height);

        int textWidth = (int)(graphics.getFontMetrics().getStringBounds(billboardName, graphics).getWidth());
        int textHeight = (int)(graphics.getFontMetrics().getStringBounds(billboardName, graphics).getHeight());

        int nameWidth = (int)(graphics.getFontMetrics().getStringBounds(billboardCreator, graphics).getWidth());
        int nameHeight = (int)(graphics.getFontMetrics().getStringBounds(billboardCreator, graphics).getHeight());

        // Card text
        graphics.setColor(textColor);
        graphics.drawString(billboardName, x + (day_width / 2) - (textWidth / 2), y + (height / 2) + textHeight / 2 - 2);
        //drawing the billboard creators name
        graphics.drawString(billboardCreator, x + (day_width / 2) - (nameWidth / 2), y + (height / 2) + nameHeight / 2 + nameHeight - 4);
    }
}