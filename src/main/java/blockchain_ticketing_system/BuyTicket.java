package blockchain_ticketing_system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;

public class BuyTicket extends JFrame {

    private JTable table;
    private JButton buyButton, backButton;
    private int userId; // Logged-in user ID
    private String userName;

    public BuyTicket(int userId, String userName) {
        super("");
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
        titleLabel.setFont(new Font("Poppins Bold", Font.BOLD, 26));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBounds(320, 20, 300, 30);
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
        buyButton.setFont(new Font("Poppins", Font.BOLD, 14));
        buyButton.addActionListener(e -> buyTicket());
        getContentPane().add(buyButton);

        // Back Button
        backButton = new JButton("Back");
        backButton.setBounds(500, 450, 120, 40);
        backButton.setBackground(Color.GRAY);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Poppins", Font.BOLD, 14));
        backButton.addActionListener(e -> {
            this.setVisible(false);
            new Home(userId, userName).setVisible(true);
        });

        getContentPane().add(backButton);

        setVisible(true);
    }

    private void loadEvents() {
        try (Connection conn = Connect_Db.getConnection()) {
            String query = "SELECT e.event_id AS 'ID', e.name AS 'Event Name', " +
                    "e.location AS 'Location', e.event_date AS 'Date', " +
                    "(e.capacity - COUNT(t.ticket_id)) AS 'Remaining Seats' " +
                    "FROM events e " +
                    "LEFT JOIN tickets t ON e.event_id = t.event_id " +
                    "GROUP BY e.event_id, e.name, e.location, e.event_date, e.capacity";

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
            // Step 1: Check event capacity
            String countQuery = "SELECT COUNT(*) FROM tickets WHERE event_id = ?";
            PreparedStatement pstCount = conn.prepareStatement(countQuery);
            pstCount.setInt(1, eventId);
            ResultSet rsCount = pstCount.executeQuery();
            rsCount.next();
            int soldTickets = rsCount.getInt(1);

            String capacityQuery = "SELECT capacity FROM events WHERE event_id = ?";
            PreparedStatement pstCap = conn.prepareStatement(capacityQuery);
            pstCap.setInt(1, eventId);
            ResultSet rsCap = pstCap.executeQuery();

            if (!rsCap.next()) {
                JOptionPane.showMessageDialog(this, "Event not found.");
                return;
            }

            int capacity = rsCap.getInt("capacity");

            if (soldTickets >= capacity) {
                JOptionPane.showMessageDialog(this, "Sorry, this event is sold out.");
                return;
            }

            // Step 2: Insert ticket into tickets table
            String insertTicket = "INSERT INTO tickets (event_id, owner_id, status) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(insertTicket, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, eventId);
            pst.setInt(2, userId);
            pst.setString(3, "sold");
            pst.executeUpdate();

            ResultSet generatedKeys = pst.getGeneratedKeys();
            int ticketId = -1;
            if (generatedKeys.next()) {
                ticketId = generatedKeys.getInt(1);
            }

            // Step 3: Add blockchain entry
            String prevHash = "0"; // default for first block
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT block_hash FROM blockchain ORDER BY block_id DESC LIMIT 1");
            if (rs.next()) prevHash = rs.getString("block_hash");

            String transactionData = "User " + userName + " bought ticket ID " + ticketId + " for event " + eventName;

            // use raw millis instead of NOW()
            long tsMillis = System.currentTimeMillis();
            String blockHash = StringUtil.applySha256(prevHash + transactionData + tsMillis);

            String insertBlock = "INSERT INTO blockchain (prev_hash, transaction_data, block_hash, timestamp) VALUES (?, ?, ?, ?)";
            PreparedStatement pstBlock = conn.prepareStatement(insertBlock);
            pstBlock.setString(1, prevHash);
            pstBlock.setString(2, transactionData);
            pstBlock.setString(3, blockHash);
            pstBlock.setLong(4, tsMillis);  // 👈 store millis
            pstBlock.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "🎉 Ticket bought successfully!\nEvent: " + eventName +
                            "\nYour Ticket ID: " + ticketId);

            // Refresh table after purchase
            loadEvents();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error buying ticket: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new BuyTicket(1, "TestUser");
    }
}
