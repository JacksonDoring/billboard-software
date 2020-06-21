package billboard.controlpanel;

import javax.swing.*;
import java.util.HashMap;

/**
 * GUI for editing a user's permissions
 */
public class EditUser {
    private JPanel panel;
    private JCheckBox editBillboardsCheckBox;
    private JCheckBox createBillboards;
    private JCheckBox editUsers;
    private JCheckBox scheduleBillboards;
    private JButton editUserButton;

    /**
     * Main GUI
     * @param userId - User ID to edit permissions for
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public EditUser(int userId) throws BillboardServerException {
        JFrame frame = new JFrame("Billboard Control Panel Login");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        HashMap userData = ServerUsers.getUserData(userId);
        String username = (String) userData.get("username");

        //values for the permission boxes
        Boolean[] values = new Boolean[4];
        //the list to loop through in order to set the labels
        String[] labels = {"editBillboards", "createBillboards", "editUsers", "scheduleBillboards"};

        //looping through the labels and getting the permissions
        for (int i = 0; i < labels.length; i++) {
            //checking if the data is a 1 or 0 and then assinging the true or false value to it
            if (userData.get(labels[i]).equals("1")) {
                values[i] = true;
            } else {
                values[i] = false;
            }
        }

        // Note that no user has the ability to remove their own "Edit Users" permission.
        if (Token.getUserId() == userId) {
            editUsers.setVisible(false);
        }

        //setting the fields of the gui to the users data
        this.editBillboardsCheckBox.setSelected(values[0]);
        this.createBillboards.setSelected(values[1]);
        this.editUsers.setSelected(values[2]);
        this.scheduleBillboards.setSelected(values[3]);
        editUserButton.addActionListener(e -> {
            try {
                if (ServerUsers.updateUserPermissions(userId, editBillboardsCheckBox.isSelected(), createBillboards.isSelected(), scheduleBillboards.isSelected(), editUsers.isSelected())){
                    JOptionPane.showMessageDialog(frame, "User Permissions Updated");
                    frame.dispose();
                }
                else{
                    JOptionPane.showMessageDialog(frame, "Failed to Update User Permissions!");
                }
            } catch (BillboardServerException ex) {
                ex.printStackTrace();
            }

        });
    }
}
