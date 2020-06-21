package billboard.controlpanel;

import javax.swing.*;

/**
 * GUI for creating a user
 */
public class CreateUser {
    private JPanel panel;
    private JTextField username;
    private JTextField password;
    private JCheckBox editBillboardsCheckBox;
    private JCheckBox createBillboards;
    private JCheckBox editUsers;
    private JCheckBox scheduleBillboards;
    private JButton createUserButton;

    /**
     * Main GUI
     */
    public CreateUser(){
        JFrame frame = new JFrame("Billboard Control Panel Login");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        createUserButton.addActionListener(e -> {
            //declaring the variables from the inout data of the form
            String userName = this.username.getText();
            String password = this.password.getText();
            Boolean editBillboards = this.editBillboardsCheckBox.isSelected();
            Boolean createBillboards = this.createBillboards.isSelected();
            Boolean editUsers = this.editUsers.isSelected();
            Boolean scheduleBillboards = this.scheduleBillboards.isSelected();
            if (userName.equals("") || password.equals("")){
                JOptionPane.showMessageDialog(frame, "Error: please fill all fields");
            }
            else {
                try {
                    ServerUsers.addUser(userName, password, editBillboards, createBillboards, scheduleBillboards, editUsers);

                    // Success popup
                    JOptionPane.showMessageDialog(frame, "User created");
                    frame.dispose();
                } catch (BillboardServerException ex) {
                    // Error message popup
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        });
    }
}
