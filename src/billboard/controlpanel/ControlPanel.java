package billboard.controlpanel;

import javax.swing.*;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * GUI for the control panel home
 */
public class ControlPanel {
    private JPanel controlPanel;

    private JButton createBillboard;
    private JButton listBillboards;
    private JButton scheduleBillboards;
    private JButton editUsers;
    private JButton createUser;
    private JButton logout;
    private JButton changeOwnPassword;

    /**
     * Main GUI
     * @param userPermissions - HashMap of user permissions
     */
    public ControlPanel(HashMap<String, Boolean> userPermissions){
        JFrame mainFrame = new JFrame("Billboard Control Panel");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.getContentPane().add(controlPanel);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);

        // initializing all the buttons that require specific permissions to not visible
        createBillboard.setVisible(false);
        scheduleBillboards.setVisible(false);
        editUsers.setVisible(false);
        createUser.setVisible(false);

        // show additional features that the user is allowed to use
        if(userPermissions.get("createBillboards")){
            createBillboard.setVisible(true);
        }

        if(userPermissions.get("editUsers")){
            editUsers.setVisible(true);
            createUser.setVisible(true);
        }

        if(userPermissions.get("scheduleBillboards")){
            scheduleBillboards.setVisible(true);
        }

        // add button functionality
        logout.addActionListener(e -> {
            int logout = JOptionPane.showConfirmDialog(controlPanel,"Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (logout == JOptionPane.YES_OPTION) {
                try {
                    ServerUsers.logoutUser();
                } catch (BillboardServerException ex) {
                    ex.printStackTrace();
                }
                mainFrame.dispose();
                new Login();
            }
        });

        createBillboard.addActionListener(e -> {
            //check for an active session token
            if (ServerUsers.checkActiveSession()) {
                new CreateBillboard();
            }
            //otherwise we will open a dialog box
            else {
                JOptionPane.showMessageDialog(controlPanel, "No active session found, please log in again.");
            }
        });

        listBillboards.addActionListener(e -> {
            //check for an active session token
            if (ServerUsers.checkActiveSession()) {
                try {
                    new ListBillboards(userPermissions);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            //otherwise we will open a dialog box
            else {
                JOptionPane.showMessageDialog(controlPanel, "No active session found, please log in again.");
            }
        });

        editUsers.addActionListener(e -> {
            //check for an active session token
            if (ServerUsers.checkActiveSession()) {
                new Users();
            }
            //otherwise we will open a dialog box
            else {
                JOptionPane.showMessageDialog(controlPanel, "No active session found, please log in again.");
            }
        });

        createUser.addActionListener(e -> {
            //check for an active session token
            if (ServerUsers.checkActiveSession()) {
                //if there is an active token then let this open the createUser
                new CreateUser();
            }
            //otherwise we will open a dialog box
            else {
                JOptionPane.showMessageDialog(controlPanel, "No active session found, please log in again.");
            }
        });

        changeOwnPassword.addActionListener(e -> {
            //check for an active session token
            if (ServerUsers.checkActiveSession()) {
                int ourUserId = Token.getUserId();

                try {
                    new EditUserPassword(ourUserId);
                } catch (BillboardServerException ex) {
                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(controlPanel, ex.getMessage());
                }
            }
            //otherwise we will open a dialog box
            else {
                JOptionPane.showMessageDialog(controlPanel, "No active session found, please log in again.");
            }
        });

        scheduleBillboards.addActionListener(e -> {
            //check for an active session token
            if (ServerUsers.checkActiveSession()) {
                new ScheduleOptions();
            }
            //otherwise we will open a dialog box
            else {
                JOptionPane.showMessageDialog(controlPanel, "No active session found, please log in again.");
            }
        });
    }
}


