package billboard.controlpanel;

import billboard.server.PasswordHash;
import billboard.server.PasswordHashException;

import javax.swing.*;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * GUI for editing a user's password
 */
public class EditUserPassword {
    private JButton updatePasswordBtn;
    private JPanel panel;
    private JPasswordField passwordBox;

    /**
     * Main GUI
     * @param userId - User ID to edit password for
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public EditUserPassword(int userId) throws BillboardServerException {
        JFrame frame = new JFrame("Billboard Control Panel Login");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        updatePasswordBtn.addActionListener(e -> {
            // Update the password
            try {
                // Create password hash
                String passwordHash = PasswordHash.hashString(passwordBox.getText());

                if (ServerUsers.updatePassword(userId, passwordHash)){
                    JOptionPane.showMessageDialog(frame, "Password Updated");
                    frame.dispose();
                }
                else {
                    JOptionPane.showMessageDialog(frame, "Error Update Failed!");
                }
            } catch (BillboardServerException ex) {
                ex.printStackTrace();
            } catch (NoSuchAlgorithmException | PasswordHashException ex) {
                ex.printStackTrace();
            }
        });
    }
}
