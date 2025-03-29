import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginScreen extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/AirlineBooking";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "dhruvhadap123!";

    public LoginScreen() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Airline Ticket Booking - Login");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Airline Ticket Booking", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Email:"), gbc);
        gbc.gridy = 1;
        JTextField emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(emailField, gbc);

        gbc.gridy = 2;
        inputPanel.add(new JLabel("Password:"), gbc);
        gbc.gridy = 3;
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        JLabel messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);
        inputPanel.add(messageLabel, gbc);

        gbc.gridy = 5;
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(200, 35));
        loginButton.setBackground(new Color(0, 120, 215));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(loginButton, gbc);

        gbc.gridy = 6;
        JLabel signUpLabel = new JLabel("<html><u>Don't have an account? Sign Up</u></html>", SwingConstants.CENTER);
        signUpLabel.setForeground(Color.BLUE);
        signUpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new SignUpScreen();
                dispose(); 
            }
        });
        inputPanel.add(signUpLabel, gbc);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (email.isEmpty() || password.isEmpty()) {
                    messageLabel.setText("Email and Password are required!");
                    return;
                }

                if (authenticateUser(email, password)) {
                    messageLabel.setForeground(Color.GREEN);
                    messageLabel.setText("Login Successful!");
                    dispose(); 
                    new Dashboard(email); 
                } else {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Invalid Credentials!");
                }
            }
        });

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setVisible(true);
    }

    private static boolean authenticateUser(String email, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT user_id FROM Users WHERE email = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next(); 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen());
    }
}