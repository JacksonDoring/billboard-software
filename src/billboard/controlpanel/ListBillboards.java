package billboard.controlpanel;

import billboard.server.DBException;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * GUI for listing billboards
 */
public class ListBillboards extends JFrame {
    private BillboardPreview preview = new BillboardPreview();

    /**
     * Main GUI
     * @param userPermissions - HashMap of user permissions
     * @throws SQLException if an SQL exception occurs
     */
    public ListBillboards(HashMap<String, Boolean> userPermissions) throws SQLException {
        JFrame frame = new JFrame("Billboard List");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20,30,20,30));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        frame.getContentPane().add(panel);

        JLabel mainLabel = new JLabel("Billboard List");
        mainLabel.setFont(new Font(mainLabel.getFont().getName(), Font.BOLD, 16));
        panel.add(mainLabel, gbc);

        JPanel billboardList = new JPanel();
        billboardList.setLayout(new GridLayout(0, 1));

        JScrollPane scrollPane = new JScrollPane(billboardList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        panel.add(scrollPane, gbc);

        JPanel billboardButtons = new JPanel();
        panel.add(billboardButtons, gbc);

        JButton previewBillboardButton = new JButton("Preview Billboard");
        billboardButtons.add(previewBillboardButton);

        JButton editBillboardButton = new JButton("Edit Billboard");
        //Setting the edit billboards button default visible to false
        editBillboardButton.setVisible(false);

        JButton deleteBillboardButton = new JButton("Delete Billboard");
        //Setting the delete billboards button default visible to false
        deleteBillboardButton.setVisible(false);

        editBillboardButton.setVisible(true);
        deleteBillboardButton.setVisible(true);
        billboardButtons.add(deleteBillboardButton);
        billboardButtons.add(editBillboardButton);

        ButtonGroup radioButtons = new ButtonGroup();
        HashMap<Integer, Integer> yourBillboards = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> billbordIds = new HashMap<Integer, Integer>();

        // Get billboards from server
        try {
            int index = 0;
            ArrayList<HashMap> billboards = ServerBillboards.listBillboards();
            for (HashMap billboardData : billboards) {
                index++;

                boolean ourBillboard = (Integer) billboardData.get("creatorId") == Token.getUserId();
                String creatorName = (String) billboardData.get("creatorUsername");

                // Add radio button
                JRadioButton button = new JRadioButton(billboardData.get("name") + (ourBillboard ? " (yours)" : " (creator: " + creatorName + ")"));
                button.setActionCommand(Integer.toString((Integer) billboardData.get("billboardId")));
                radioButtons.add(button);
                billboardList.add(button);

                // Show preview/edit/delete buttons if they aren't already visible
                billboardButtons.setVisible(true);

                int billId = (Integer) billboardData.get("billboardId");
                billbordIds.put(index, billId);

                if (ourBillboard) {
                    yourBillboards.put(index, billId);
                }
            }
        } catch (BillboardServerException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(frame, e.getMessage());
        }

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Previewing selected billboard
        previewBillboardButton.addActionListener(e -> {
            if (radioButtons.getSelection() == null) {
                return;
            }

            int selectedBillboardId = Integer.parseInt(radioButtons.getSelection().getActionCommand());

            try {
                String billboardData = ServerBillboards.getBillboardData(selectedBillboardId);

                preview.LoadXML(billboardData);
            } catch (ParserConfigurationException | IOException | SAXException | BillboardServerException ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(frame, "Failed to load billboard");
            }
        });

        // Editing selected billboard
        editBillboardButton.addActionListener(e -> {
            if (radioButtons.getSelection() == null) {
                return;
            }

            int selectedBillboardId = Integer.parseInt(radioButtons.getSelection().getActionCommand());
            boolean checkSchedule = false;
            try {
                checkSchedule = CheckSchedule(selectedBillboardId);
            } catch (BillboardServerException ex) {
                ex.printStackTrace();
            }

            if ((yourBillboards.containsValue(selectedBillboardId) && !checkSchedule) || userPermissions.get("editBillboards")) {
                try {
                    EditBillboard.edit(selectedBillboardId);
                } catch (BillboardServerException | SAXException | ParserConfigurationException | IOException ex) {
                    ex.printStackTrace();
                }
            }

            // Show edit billboard page (create billboard page modified)
        });

        // Deleting selected billboard
        deleteBillboardButton.addActionListener(e -> {
            if (radioButtons.getSelection() == null) {
                return;
            }

            Integer selectedBillboardId = Integer.parseInt(radioButtons.getSelection().getActionCommand());
            boolean checkSchedule = false;
            try {
                checkSchedule = CheckSchedule(selectedBillboardId);
            } catch (BillboardServerException ex) {
                ex.printStackTrace();
            }

            if((yourBillboards.containsValue(selectedBillboardId) && !checkSchedule) || userPermissions.get("editBillboards")){
                // Warning message to see if the user wants to delete the billboard
                int delete = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete the billboard?", "Billboard deletion", JOptionPane.YES_NO_OPTION);
                if (delete == JOptionPane.YES_OPTION) {
                    // Deleting the billboard

                    try {
                        ServerBillboards.deleteBillboard(selectedBillboardId);
                    } catch (BillboardServerException ex) {
                        ex.printStackTrace();

                        JOptionPane.showMessageDialog(frame, ex.getMessage());
                    }

                    // Showing message to confirm deletion
                    JOptionPane.showMessageDialog(frame, "The billboard has been deleted successfully");
                    frame.dispose();
                    try {
                        new ListBillboards(userPermissions);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else{
                JOptionPane.showMessageDialog(frame, "You cannot delete this billboard");
            }
        });
    }

    /**
     * Checks if a billboard is scheduled
     * @param billId - Billboard ID to check
     * @return Whether or not the billboard is scheduled
     * @throws BillboardServerException if the request fails
     */
    public static boolean CheckSchedule(int billId) throws BillboardServerException {
        // Getting the billboards that are scheduled
        ArrayList<HashMap> schedules = ServerSchedules.getAllSchedules();

        // If the billboard selected is already scheduled
        for (HashMap scheduleData : schedules) {
            int billId1 = (Integer) scheduleData.get("billboardId");
            if (billId1 == billId){
                return true;
            }
        }

        return false;
    }
}