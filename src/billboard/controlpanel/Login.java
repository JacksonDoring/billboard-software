package billboard.controlpanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * GUI for logging in
 */
public class Login {
    private JFrame frame;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JPanel panelMain;
    private JLabel heading1;
    private JLabel usernameLabel;
    private JLabel passwordLabel;

    //action event split into a seperate class due to using it twice
    private class LoginUser implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e){
            //this is the main function to login the user that will be called on the action listeners.
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Check the login details
            try {
                ServerUsers.loginUser(username, password);

                JOptionPane.showMessageDialog(frame, "Login successful!");

                // Close the login window
                frame.dispose();

                // Get permissions
                HashMap<String, Boolean> permissions = ServerUsers.getOwnPermissions();

                //Creating a control panel with the permissions
                new ControlPanel(permissions); // show the main control window
            } catch (BillboardServerException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }
    }

    /**
     * Main GUI
     */
    public Login() {
        frame = new JFrame("Billboard Control Panel Login");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(panelMain);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        loginButton.addActionListener(new LoginUser());
        usernameField.addActionListener(new LoginUser()); // For pressing enter in the username field
        passwordField.addActionListener(new LoginUser()); // For pressing enter in the password field
    }
}
