package blockchain_ticketing_system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;

public class EventsPage extends JFrame {

    private JTable table;
    private JButton buyButton, backButton;
    private int userId; // Logged-in user ID
    private String userName;

    public EventsPage(int userId, String userName) {
        super("Available Events");
        this.userId = userId;
        this.userName = userName;
        initialize();
        loadEvents();
    }

    private void initialize() {
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(null);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel titleLabel = new JLabel("Available Events");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBounds(300, 20, 300, 30);
        getContentPane().add(titleLabel);

        // JTable for events
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 70, 820, 350);
        getContentPane().add(scrollPane);

        // Buy Ticket Button
        buyButton = new JButton("Buy Selected Ticket");
        buyButton.setBounds(200, 450, 200, 40);
        buyButton.setBackground(new Color(0, 102, 204));
        buyButton.setForeground(Color.WHITE);
        buyButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        buyButton.addActionListener(e -> buyTicket());
        getContentPane().add(buyButton);

        // Back Button
        backButton = new JButton("Back");
        backButton.setBounds(500, 450, 120, 40);
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

    private void loadEvents() {
        try (Connection conn = Connect_Db.getConnection()) {
            String query = "SELECT event_id AS 'ID', name AS 'Event Name', location AS 'Location', event_date AS 'Date' FROM events";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load events: " + ex.getMessage());
        }
    }

    private void buyTicket() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to buy.");
            return;
        }

        int eventId = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
        String eventName = table.getValueAt(selectedRow, 1).toString();

        try (Connection conn = Connect_Db.getConnection()) {

            // Insert ticket into tickets table
            String insertTicket = "INSERT INTO tickets (event_id, owner_id, status) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(insertTicket);
            pst.setInt(1, eventId);
            pst.setInt(2, userId);
            pst.setString(3, "sold");
            pst.executeUpdate();

            // Insert placeholder blockchain entry
            String insertBlock = "INSERT INTO blockchain (prev_hash, transaction_data, block_hash) VALUES (?, ?, ?)";
            PreparedStatement pstBlock = conn.prepareStatement(insertBlock);
            pstBlock.setString(1, "previous_hash_placeholder");
            pstBlock.setString(2, "User " + userName + " bought ticket for event " + eventName);
            pstBlock.setString(3, "hash_placeholder");
            pstBlock.executeUpdate();

            JOptionPane.showMessageDialog(this, "Ticket bought successfully for: " + eventName);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error buying ticket: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new EventsPage(1, "TestUser");
    }
}
