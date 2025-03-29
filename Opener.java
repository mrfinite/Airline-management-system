import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Opener extends JFrame {
    public Opener() {
        // Set system look and feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Basic frame setup
        setTitle("Airline Ticket Booking");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title label
        JLabel titleLabel = new JLabel("Airline Ticket Booking", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Button panel with GridBagLayout for centered buttons
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.setBackground(new Color(0, 120, 215));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 0;
        buttonPanel.add(loginButton, gbc);

        // Sign Up button
        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setPreferredSize(new Dimension(200, 40));
        signUpButton.setBackground(new Color(50, 205, 50)); 
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 1;
        buttonPanel.add(signUpButton, gbc);

        // Action listeners for buttons
        loginButton.addActionListener(e -> {
            new LoginScreen();  
            dispose();          
        });

        signUpButton.addActionListener(e -> {
            new SignUpScreen(); 
            dispose();
        });

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        // Ensure UI is created on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new Opener());
    }
}