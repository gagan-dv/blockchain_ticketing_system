package blockchain_ticketing_system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;

public class TransferTicketPage extends JFrame {

    private JTable table;
    private JTextField recipientEmailField;
    private JButton transferButton, backButton;
    private int userId;
    private String userName;

    public TransferTicketPage(int userId, String userName) {
        super("");
        this.userId = userId;
        this.userName = userName;
        initialize();
        loadUserTickets();
    }
    private void initialize() {
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(null);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JLabel titleLabel = new JLabel("Your Tickets");
        titleLabel.setFont(new Font("Poppins Bold", Font.BOLD, 26));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBounds(380, 20, 300, 30);
        getContentPane().add(titleLabel);
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 70, 820, 350);
        getContentPane().add(scrollPane);
        JLabel recipientLabel = new JLabel("Recipient Email:");
        recipientLabel.setBounds(30, 440, 150, 25);
        getContentPane().add(recipientLabel);
        recipientEmailField = new JTextField();
        recipientEmailField.setBounds(180, 440, 200, 25);
        getContentPane().add(recipientEmailField);
        transferButton = new JButton("Transfer Ticket");
        transferButton.setBounds(400, 440, 160, 30);
        transferButton.setBackground(new Color(0, 102, 204));
        transferButton.setForeground(Color.WHITE);
        transferButton.addActionListener(e -> transferTicket());
        getContentPane().add(transferButton);
        backButton = new JButton("Back");
        backButton.setBounds(600, 440, 120, 30);
        backButton.setBackground(Color.GRAY);
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> {
            this.setVisible(false);
            new Home(userId, userName).setVisible(true);
        });
        getContentPane().add(backButton);
        setVisible(true);
    }
    private void loadUserTickets() {
        try (Connection conn = Connect_Db.getConnection()) {
            String query = "SELECT t.ticket_id AS 'Ticket ID', e.name AS 'Event Name', t.status AS 'Status' " +
                    "FROM tickets t JOIN events e ON t.event_id = e.event_id " +
                    "WHERE t.owner_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load tickets: " + ex.getMessage());
        }
    }
    private void transferTicket() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a ticket to transfer.");
            return;
        }
        int ticketId = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
        String eventName = table.getValueAt(selectedRow, 1).toString();
        String recipientEmail = recipientEmailField.getText().trim();

        if (recipientEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter recipient's email.");
            return;
        }
        try (Connection conn = Connect_Db.getConnection()) {
            String recipientQuery = "SELECT user_id, name FROM users WHERE email=?";
            PreparedStatement pstRecipient = conn.prepareStatement(recipientQuery);
            pstRecipient.setString(1, recipientEmail);
            ResultSet rsRecipient = pstRecipient.executeQuery();
            if (!rsRecipient.next()) {
                JOptionPane.showMessageDialog(this, "Recipient email not found.");
                return;
            }
            int recipientId = rsRecipient.getInt("user_id");
            String recipientName = rsRecipient.getString("name");
            String updateTicket = "UPDATE tickets SET owner_id=?, status='transferred' WHERE ticket_id=?";
            PreparedStatement pstUpdate = conn.prepareStatement(updateTicket);
            pstUpdate.setInt(1, recipientId);
            pstUpdate.setInt(2, ticketId);
            pstUpdate.executeUpdate();
            String prevHash = "0"; // default for first block
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT block_hash FROM blockchain ORDER BY block_id DESC LIMIT 1");
            if (rs.next()) prevHash = rs.getString("block_hash");
            String transactionData = "User " + userName + " transferred ticket " + ticketId +
                    " for event " + eventName + " to " + recipientName;
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            String blockHash = StringUtil.applySha256(prevHash + transactionData + currentTimestamp.getTime());
            String insertBlock = "INSERT INTO blockchain (prev_hash, transaction_data, block_hash, timestamp) VALUES (?, ?, ?, ?)";
            PreparedStatement pstBlock = conn.prepareStatement(insertBlock);
            pstBlock.setString(1, prevHash);
            pstBlock.setString(2, transactionData);
            pstBlock.setString(3, blockHash);
            pstBlock.setTimestamp(4, currentTimestamp);
            pstBlock.executeUpdate();
            JOptionPane.showMessageDialog(this, "Ticket transferred successfully to " + recipientName);
            loadUserTickets();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error transferring ticket: " + ex.getMessage());
        }
    }
}
