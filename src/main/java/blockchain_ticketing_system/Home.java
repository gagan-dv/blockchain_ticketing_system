package blockchain_ticketing_system;

import javax.swing.*;
import java.awt.*;

public class Home extends JFrame {

    private int userId;
    private String username;

    public Home(int userId, String username) {
        super("Blockchain Ticketing System");
        this.userId = userId;
        this.username = username;
        initialize();
    }
    private void initialize() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel welcomeLabel = new JLabel("Welcome, " + username, SwingConstants.CENTER);
        welcomeLabel.setForeground(new Color(0, 102, 204));
        welcomeLabel.setFont(new Font("Poppins", Font.BOLD, 28));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(welcomeLabel, gbc);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        JButton viewAndBuy = styledButton("View & Buy Tickets", new Color(0, 102, 204));
        gbc.gridy = 1;
        panel.add(viewAndBuy, gbc);
        JButton transferTicket = styledButton("Transfer Ticket", new Color(0, 102, 204));
        gbc.gridy = 2;
        panel.add(transferTicket, gbc);
        JButton validateTicket = styledButton("Validate Ticket", new Color(0, 102, 204));
        gbc.gridy = 3;
        panel.add(validateTicket, gbc);
        JButton blockchainButton = styledButton("View Blockchain", new Color(0, 102, 204));
        gbc.gridy = 4;
        panel.add(blockchainButton, gbc);
        JButton logout = styledButton("Logout", new Color(220, 53, 69));
        gbc.gridy = 5;
        panel.add(logout, gbc);
        viewAndBuy.addActionListener(e -> new BuyTicket(userId, username).setVisible(true));
        transferTicket.addActionListener(e -> new TransferTicketPage(userId, username).setVisible(true));
        validateTicket.addActionListener(e -> new ValidateTicket(userId, username).setVisible(true));
        blockchainButton.addActionListener(e -> new BlockchainViewer().setVisible(true));
        logout.addActionListener(e -> {
            this.dispose();
            new Login().setVisible(true); 
        });
        add(panel);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    private JButton styledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Poppins", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(250, 45));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }
}
