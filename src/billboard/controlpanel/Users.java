package billboard.controlpanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * GUI for the user list
 */
public class Users extends JFrame {
    /**
     * Main GUI
     */
    public Users() {
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(20,30,20,30));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        frame.getContentPane().add(panel);

        JLabel mainLabel = new JLabel("User List");
        mainLabel.setFont(new Font(mainLabel.getFont().getName(), Font.BOLD, 16));
        panel.add(mainLabel, gbc);

        JPanel userList = new JPanel();
        userList.setLayout(new GridLayout(0, 1));

        JScrollPane scrollPane = new JScrollPane(userList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        panel.add(scrollPane, gbc);

        JPanel userButtons = new JPanel();
        panel.add(userButtons, gbc);

        JButton editPasswordButton = new JButton("Edit Password");
        userButtons.add(editPasswordButton);

        JButton editPermissionsButton = new JButton("Edit Permissions");
        userButtons.add(editPermissionsButton);

        JButton deleteUserButton = new JButton("Delete User");
        userButtons.add(deleteUserButton);

        ButtonGroup radioButtons = new ButtonGroup();

        // Get billboards from server
        try {
            ServerUsers.getUsernames().forEach((userId, username) -> {
                JRadioButton button = new JRadioButton(username);
                button.setActionCommand(Integer.toString(userId));
                radioButtons.add(button);
                userList.add(button);
                userButtons.setVisible(true);
            });
        } catch (BillboardServerException e) {
            e.printStackTrace();
            
            JOptionPane.showMessageDialog(frame, e.getMessage());
        }

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        editPasswordButton.addActionListener(e -> {
            if (radioButtons.getSelection() == null) {
                return;
            }

            int selectedUserId = Integer.parseInt(radioButtons.getSelection().getActionCommand());

            try {
                new EditUserPassword(selectedUserId);
            } catch (BillboardServerException ex) {
                ex.printStackTrace();
            
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        //editing the users permissions
        editPermissionsButton.addActionListener(e -> {
            if (radioButtons.getSelection() == null) {
                return;
            }

            int selectedUserId = Integer.parseInt(radioButtons.getSelection().getActionCommand());

            try {
                new EditUser(selectedUserId);
            } catch (BillboardServerException ex) {
                ex.printStackTrace();
            
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
            System.out.println(selectedUserId);
        });

        // Deleting the users
        deleteUserButton.addActionListener(e -> {
            if (radioButtons.getSelection() == null) {
                return;
            }

            int selectedUserId = Integer.parseInt(radioButtons.getSelection().getActionCommand());
            if (selectedUserId == Token.getUserId()){
                JOptionPane.showMessageDialog(frame, "Cannot delete yourself!");
                return;
            }

            // Warning message to see if the user wants to delete the user
            int delete = JOptionPane.showConfirmDialog(frame,"Are you sure you want to delete the user?", "User deletion", JOptionPane.YES_NO_OPTION);
            if (delete == JOptionPane.YES_OPTION) {
                try {
                    ServerUsers.deleteUser(selectedUserId);
                    // Showing message to confirm deletion
                    JOptionPane.showMessageDialog(frame, "The user has been deleted successfully");
                    frame.dispose();
                } catch (BillboardServerException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }



                new Users();
            }
        });
    }
}