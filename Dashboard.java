import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class Dashboard extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/AirlineBooking";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "dhruvhadap123!";

    private String userEmail;
    private int userId;
    private JTable bookingsTable;
    private DefaultTableModel bookingsModel;

    public Dashboard(String email) {
        this.userEmail = email;
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Airline Booking Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Get user ID
        userId = getUserId(email);

        // Main panel with tab layout
        JTabbedPane tabbedPane = new JTabbedPane();

        // Bookings Tab
        JPanel bookingsPanel = createBookingsPanel();
        tabbedPane.addTab("My Bookings", bookingsPanel);

        // Book New Flight Tab
        JPanel bookFlightPanel = createBookFlightPanel();
        tabbedPane.addTab("Book New Flight", bookFlightPanel);

        // Cancel Booking Tab
        JPanel cancelBookingPanel = createCancelBookingPanel();
        tabbedPane.addTab("Cancel Booking", cancelBookingPanel);

        add(tabbedPane);
        setVisible(true);
    }

    private int getUserId(String email) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT user_id FROM Users WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("user_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private JPanel createBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table model setup
        String[] columnNames = {"Booking ID", "Flight Number", "Source", "Destination", 
                                "Departure", "Passengers", "Total Price", "Status"};
        bookingsModel = new DefaultTableModel(columnNames, 0);
        bookingsTable = new JTable(bookingsModel);
        
        // Populate bookings
        populateBookings();

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void populateBookings() {
        bookingsModel.setRowCount(0); // Clear existing rows
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT b.booking_id, f.flight_number, f.source, f.destination, " +
                         "f.departure_datetime, b.num_passengers, b.total_price, b.status " +
                         "FROM Bookings b " +
                         "JOIN Flights f ON b.flight_id = f.flight_id " +
                         "WHERE b.user_id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("booking_id"));
                        row.add(rs.getString("flight_number"));
                        row.add(rs.getString("source"));
                        row.add(rs.getString("destination"));
                        row.add(rs.getTimestamp("departure_datetime"));
                        row.add(rs.getInt("num_passengers"));
                        row.add(rs.getDouble("total_price"));
                        row.add(rs.getString("status"));
                        
                        bookingsModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching bookings: " + e.getMessage());
        }
    }

    private JPanel createBookFlightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table to show available flights
        String[] columnNames = {"Flight Number", "Airline", "Source", "Destination", 
                                "Departure", "Available Seats", "Price"};
        DefaultTableModel flightsModel = new DefaultTableModel(columnNames, 0);
        JTable flightsTable = new JTable(flightsModel);
        
        // Populate available flights
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT flight_number, airline, source, destination, " +
                         "departure_datetime, available_seats, base_price " +
                         "FROM Flights WHERE available_seats > 0";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("flight_number"));
                    row.add(rs.getString("airline"));
                    row.add(rs.getString("source"));
                    row.add(rs.getString("destination"));
                    row.add(rs.getTimestamp("departure_datetime"));
                    row.add(rs.getInt("available_seats"));
                    row.add(rs.getDouble("base_price"));
                    
                    flightsModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching flights: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(flightsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Book Flight Button
        JButton bookButton = new JButton("Book Selected Flight");
        bookButton.addActionListener(e -> {
            int selectedRow = flightsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a flight to book");
                return;
            }

            // Here you would implement the booking logic
            // For now, just showing a placeholder
            String flightNumber = (String) flightsModel.getValueAt(selectedRow, 0);
            bookFlight(flightNumber);
        });

        panel.add(bookButton, BorderLayout.SOUTH);

        return panel;
    }

    private void bookFlight(String flightNumber) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Start transaction
            conn.setAutoCommit(false);

            // Get flight details
            String flightSql = "SELECT flight_id, available_seats, base_price FROM Flights WHERE flight_number = ?";
            PreparedStatement flightStmt = conn.prepareStatement(flightSql);
            flightStmt.setString(1, flightNumber);
            ResultSet flightRs = flightStmt.executeQuery();

            if (!flightRs.next()) {
                JOptionPane.showMessageDialog(this, "Flight not found");
                conn.rollback();
                return;
            }

            int flightId = flightRs.getInt("flight_id");
            int availableSeats = flightRs.getInt("available_seats");
            double basePrice = flightRs.getDouble("base_price");

            // Check if seats are available
            if (availableSeats < 1) {
                JOptionPane.showMessageDialog(this, "No seats available");
                conn.rollback();
                return;
            }

            // Insert booking
            String bookingSql = "INSERT INTO Bookings (user_id, flight_id, num_passengers, total_price, status) " +
                                "VALUES (?, ?, 1, ?, 'CONFIRMED')";
            PreparedStatement bookingStmt = conn.prepareStatement(bookingSql);
            bookingStmt.setInt(1, userId);
            bookingStmt.setInt(2, flightId);
            bookingStmt.setDouble(3, basePrice);
            bookingStmt.executeUpdate();

            // Update available seats
            String updateSeatsSql = "UPDATE Flights SET available_seats = available_seats - 1 WHERE flight_id = ?";
            PreparedStatement updateSeatsStmt = conn.prepareStatement(updateSeatsSql);
            updateSeatsStmt.setInt(1, flightId);
            updateSeatsStmt.executeUpdate();

            // Commit transaction
            conn.commit();

            // Refresh bookings and flights
            populateBookings();
            JOptionPane.showMessageDialog(this, "Flight booked successfully!");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage());
        }
    }

    private JPanel createCancelBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table to show user's bookings for cancellation
        String[] columnNames = {"Booking ID", "Flight Number", "Source", "Destination", 
                                "Departure", "Passengers", "Total Price", "Status"};
        DefaultTableModel cancelModel = new DefaultTableModel(columnNames, 0);
        JTable cancelTable = new JTable(cancelModel);
        
        // Populate bookings for cancellation
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT b.booking_id, f.flight_number, f.source, f.destination, " +
                         "f.departure_datetime, b.num_passengers, b.total_price, b.status " +
                         "FROM Bookings b " +
                         "JOIN Flights f ON b.flight_id = f.flight_id " +
                         "WHERE b.user_id = ? AND b.status = 'CONFIRMED'";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("booking_id"));
                        row.add(rs.getString("flight_number"));
                        row.add(rs.getString("source"));
                        row.add(rs.getString("destination"));
                        row.add(rs.getTimestamp("departure_datetime"));
                        row.add(rs.getInt("num_passengers"));
                        row.add(rs.getDouble("total_price"));
                        row.add(rs.getString("status"));
                        
                        cancelModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching bookings: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(cancelTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Cancel Booking Button
        JButton cancelButton = new JButton("Cancel Selected Booking");
        cancelButton.addActionListener(e -> {
            int selectedRow = cancelTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a booking to cancel");
                return;
            }

            int bookingId = (int) cancelModel.getValueAt(selectedRow, 0);
            cancelBooking(bookingId);
        });

        panel.add(cancelButton, BorderLayout.SOUTH);

        return panel;
    }

    private void cancelBooking(int bookingId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Start transaction
            conn.setAutoCommit(false);

            // Get booking details
            String bookingSql = "SELECT flight_id, num_passengers FROM Bookings WHERE booking_id = ?";
            PreparedStatement bookingStmt = conn.prepareStatement(bookingSql);
            bookingStmt.setInt(1, bookingId);
            ResultSet bookingRs = bookingStmt.executeQuery();

            if (!bookingRs.next()) {
                JOptionPane.showMessageDialog(this, "Booking not found");
                conn.rollback();
                return;
            }

            int flightId = bookingRs.getInt("flight_id");
            int numPassengers = bookingRs.getInt("num_passengers");

            // Update booking status
            String updateBookingSql = "UPDATE Bookings SET status = 'CANCELLED' WHERE booking_id = ?";
            PreparedStatement updateBookingStmt = conn.prepareStatement(updateBookingSql);
            updateBookingStmt.setInt(1, bookingId);
            updateBookingStmt.executeUpdate();

            // Update available seats
            String updateSeatsSql = "UPDATE Flights SET available_seats = available_seats + ? WHERE flight_id = ?";
            PreparedStatement updateSeatsStmt = conn.prepareStatement(updateSeatsSql);
            updateSeatsStmt.setInt(1, numPassengers);
            updateSeatsStmt.setInt(2, flightId);
            updateSeatsStmt.executeUpdate();

            // Commit transaction
            conn.commit();

            // Refresh bookings
            populateBookings();
            JOptionPane.showMessageDialog(this, "Booking cancelled successfully!");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cancellation failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard("test@example.com"));
    }
}