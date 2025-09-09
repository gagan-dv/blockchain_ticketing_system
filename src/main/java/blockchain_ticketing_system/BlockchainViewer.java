package blockchain_ticketing_system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;
import java.text.SimpleDateFormat;
public class BlockchainViewer extends JFrame {
    private JTable table;
    private JButton verifyButton, backButton;
    public BlockchainViewer() {
        super("");
        initialize();
        loadBlockchain();
    }
    private void initialize() {
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(null);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JLabel titleLabel = new JLabel("Blockchain Ledger");
        titleLabel.setFont(new Font("Poppins Bold", Font.BOLD, 26));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBounds(350, 20, 400, 30);
        getContentPane().add(titleLabel);
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 70, 920, 350);
        getContentPane().add(scrollPane);
        verifyButton = new JButton("Verify Blockchain");
        verifyButton.setBounds(250, 450, 200, 40);
        verifyButton.setBackground(new Color(0, 102, 204));
        verifyButton.setForeground(Color.WHITE);
        verifyButton.setFont(new Font("Poppins", Font.BOLD, 14));
        verifyButton.addActionListener(e -> verifyBlockchain());
        getContentPane().add(verifyButton);
        backButton = new JButton("Back");
        backButton.setBounds(500, 450, 120, 40);
        backButton.setBackground(Color.GRAY);
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Poppins", Font.BOLD, 14));
        backButton.addActionListener(e -> this.setVisible(false));
        getContentPane().add(backButton);
        setVisible(true);
    }
    private void loadBlockchain() {
        try (Connection conn = Connect_Db.getConnection()) {
            String query = "SELECT block_id, prev_hash, transaction_data, block_hash, timestamp FROM blockchain";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load blockchain: " + ex.getMessage());
        }
    }
    private void verifyBlockchain() {
        try (Connection conn = Connect_Db.getConnection()) {
            String query = "SELECT block_id, prev_hash, transaction_data, block_hash, timestamp FROM blockchain ORDER BY block_id ASC";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            String prevHash = "0"; // default for genesis
            int blockIndex = 0;
            while (rs.next()) {
                blockIndex = rs.getInt("block_id");
                String storedPrevHash = rs.getString("prev_hash");
                String transactionData = rs.getString("transaction_data");
                String storedHash = rs.getString("block_hash");
                long tsMillis = rs.getLong("timestamp");
                String recalculatedHash = StringUtil.applySha256(storedPrevHash + transactionData + tsMillis);
                System.out.println("Block " + blockIndex + " check:");
                System.out.println("  stored prev_hash: " + storedPrevHash);
                System.out.println("  stored hash:      " + storedHash);
                System.out.println("  recalculated:     " + recalculatedHash);
                if (!storedHash.equals(recalculatedHash)) {
                    JOptionPane.showMessageDialog(this,
                            "Blockchain broken at block " + blockIndex + " (hash tampered)\n" +
                                    "Stored hash: " + storedHash + "\n" +
                                    "Recalculated: " + recalculatedHash,
                            "Message", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!storedPrevHash.equals(prevHash)) {
                    JOptionPane.showMessageDialog(this,
                            "Blockchain broken at block " + blockIndex + " (prev hash mismatch)",
                            "Message", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                prevHash = storedHash;
            }

            JOptionPane.showMessageDialog(this,
                    "Blockchain verified successfully!",
                    "Message", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error verifying blockchain: " + ex.getMessage(),
                    "Message", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new BlockchainViewer();
    }
}
