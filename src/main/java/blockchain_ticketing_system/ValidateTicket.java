package blockchain_ticketing_system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ValidateTicket extends JFrame {

    private JTextField ticketIdField;
    private JButton validateButton, backButton;
    private int userId;
    private String userName;

    public ValidateTicket(int userId, String userName) {
        super("Validate Ticket");
        this.userId = userId;
        this.userName = userName;
        initialize();
    }

    private void initialize() {
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(null);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel titleLabel = new JLabel("Ticket Validation");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBounds(130, 20, 300, 30);
        getContentPane().add(titleLabel);

        JLabel ticketLabel = new JLabel("Enter Ticket ID:");
        ticketLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        ticketLabel.setBounds(50, 80, 150, 25);
        getContentPane().add(ticketLabel);

        ticketIdField = new JTextField();
        ticketIdField.setBounds(200, 80, 200, 25);
        getContentPane().add(ticketIdField);

        validateButton = new JButton("Validate");
        validateButton.setBounds(100, 150, 120, 35);
        validateButton.setBackground(new Color(0, 102, 204));
        validateButton.setForeground(Color.WHITE);
        validateButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        validateButton.addActionListener(e -> validateTicket());
        getContentPane().add(validateButton);

        backButton = new JButton("Back");
        backButton.setBounds(250, 150, 120, 35);
        backButton.setBackground(Color.GRAY);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        backButton.addActionListener(e -> {
            this.setVisible(false);
            new Home(userId, userName).setVisible(true);
        });
        getContentPane().add(backButton);

        setVisible(true);
    }

    private void validateTicket() {
        String ticketIdStr = ticketIdField.getText().trim();
        if (ticketIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a ticket ID.");
            return;
        }

        try {
            int ticketId = Integer.parseInt(ticketIdStr);

            try (Connection conn = Connect_Db.getConnection()) {
                // Check ticket validity
                String query = "SELECT t.ticket_id, t.status, u.name AS owner_name, e.name AS event_name " +
                        "FROM tickets t " +
                        "JOIN users u ON t.owner_id = u.user_id " +
                        "JOIN events e ON t.event_id = e.event_id " +
                        "WHERE t.ticket_id = ?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setInt(1, ticketId);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    String status = rs.getString("status");
                    String ownerName = rs.getString("owner_name");
                    String eventName = rs.getString("event_name");

                    if (status.equalsIgnoreCase("sold") || status.equalsIgnoreCase("transferred")) {
                        JOptionPane.showMessageDialog(this, "Ticket is VALID.\nEvent: " + eventName + "\nOwner: " + ownerName);
                    } else {
                        JOptionPane.showMessageDialog(this, "Ticket is INVALID or not sold yet.");
                    }

                } else {
                    JOptionPane.showMessageDialog(this, "Ticket not found.");
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Ticket ID format.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error validating ticket: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new ValidateTicket(1, "TestUser"); // Test
    }
}
