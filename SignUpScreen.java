import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SignUpScreen extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/AirlineBooking";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "dhruvhadap123!";

    public SignUpScreen() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Airline Ticket Booking - Sign Up");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); 

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridy = 1;
        JTextField nameField = new JTextField(20);
        nameField.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(nameField, gbc);

        gbc.gridy = 2;
        inputPanel.add(new JLabel("Email:"), gbc);
        gbc.gridy = 3;
        JTextField emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(emailField, gbc);

        gbc.gridy = 4;
        inputPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridy = 5;
        JTextField phoneField = new JTextField(20);
        phoneField.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(phoneField, gbc);

        gbc.gridy = 6;
        inputPanel.add(new JLabel("Password:"), gbc);
        gbc.gridy = 7;
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(passwordField, gbc);

        gbc.gridy = 8;
        JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);
        inputPanel.add(messageLabel, gbc);

        gbc.gridy = 9;
        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setPreferredSize(new Dimension(200, 35));
        signUpButton.setBackground(new Color(0, 120, 215));
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(signUpButton, gbc);

        gbc.gridy = 10;
        JLabel loginLabel = new JLabel("<html><u>Already have an account? Log In</u></html>", SwingConstants.CENTER);
        loginLabel.setForeground(Color.BLUE);
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LoginScreen();
                dispose(); 
            }
        });
        inputPanel.add(loginLabel, gbc);

        signUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String password = new String(passwordField.getPassword());

                // Enhanced input validation
                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("All fields are required!");
                    return;
                }

                // Basic email validation
                if (!isValidEmail(email)) {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Invalid email format!");
                    return;
                }

                // Basic phone validation (simple numeric check)
                if (!isValidPhone(phone)) {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Invalid phone number!");
                    return;
                }

                if (registerUser(name, email, phone, password)) {
                    messageLabel.setForeground(Color.GREEN);
                    messageLabel.setText("Registration Successful!");
                    
                    // Clear fields after successful registration
                    nameField.setText("");
                    emailField.setText("");
                    phoneField.setText("");
                    passwordField.setText("");

                    // Automatically open login screen
                    new LoginScreen();
                    dispose();
                } else {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Registration Failed. Email may exist.");
                }
            }
        });

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setVisible(true);
    }

    // Email validation method
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    // Phone validation method (basic numeric check)
    private static boolean isValidPhone(String phone) {
        return phone.matches("\\d{10}"); // Assumes 10-digit phone number
    }

    private static boolean registerUser(String name, String email, String phone, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Check if email already exists
            String checkEmail = "SELECT COUNT(*) FROM Users WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkEmail)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Email already exists
                    }
                }
            }

            // Insert new user
            String sql = "INSERT INTO Users (full_name, email, phone, password) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setString(4, password);  

                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SignUpScreen());
    }
}