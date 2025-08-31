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
        // Use a JPanel with GridBagLayout for centering
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 20; // increase height


        JLabel welcomeLabel = new JLabel("Welcome, " + username, SwingConstants.CENTER);
        welcomeLabel.setForeground(Color.BLUE);
        welcomeLabel.setFont(new Font("Tahoma", Font.BOLD, 28));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(welcomeLabel, gbc);

        // Buttons
        gbc.gridwidth = 2;
        gbc.gridx = 0;

        JButton viewEvents = new JButton("View Events");
        gbc.gridy = 1;
        viewEvents.setFocusPainted(false);
        panel.add(viewEvents, gbc);

        JButton buyTicket = new JButton("Buy Ticket");
        gbc.gridy = 2;
        panel.add(buyTicket, gbc);

        JButton transferTicket = new JButton("Transfer Ticket");
        gbc.gridy = 3;
        panel.add(transferTicket, gbc);

        JButton validateTicket = new JButton("Validate Ticket");
        gbc.gridy = 4;
        panel.add(validateTicket, gbc);

        JButton logout = new JButton("Logout");
        gbc.gridy = 5;
        panel.add(logout, gbc);

        // Action listeners
        viewEvents.addActionListener(e -> new EventsPage(userId, username).setVisible(true));
        buyTicket.addActionListener(e -> new BuyTicket(userId, username).setVisible(true));
        transferTicket.addActionListener(e -> new TransferTicketPage(userId, username).setVisible(true));
        validateTicket.addActionListener(e -> new ValidateTicket(userId, username).setVisible(true));
        logout.addActionListener(e -> {
            this.dispose(); // Close Home page completely
            new Login().setVisible(true); // Open Login page
        });

        add(panel);
        setSize(600, 600);
        setLocationRelativeTo(null); // center the frame on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
