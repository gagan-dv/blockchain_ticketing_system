package blockchain_ticketing_system;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame implements ActionListener {

    JLabel l1, l2, titleLabel;
    JTextField t1;
    JPasswordField t2;
    JButton b1, b2, forgotButton;

    public Login() {
        super("Login");
        setSize(500, 400); // slightly taller to fit forgot button
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center frame
        getContentPane().setBackground(Color.WHITE);
        setLayout(null);

        // Title
        titleLabel = new JLabel("Blockchain Ticketing Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBounds(50, 20, 400, 40);
        add(titleLabel);

        // Email Label & Field
        l1 = new JLabel("Email:");
        l1.setBounds(80, 90, 100, 30);
        add(l1);

        t1 = new JTextField();
        t1.setBounds(200, 90, 200, 30);
        add(t1);

        // Password Label & Field
        l2 = new JLabel("Password:");
        l2.setBounds(80, 140, 100, 30);
        add(l2);

        t2 = new JPasswordField();
        t2.setBounds(200, 140, 200, 30);
        add(t2);

        // Buttons
        b1 = new JButton("Login");
        b1.setBounds(80, 200, 120, 35);
        b1.setBackground(new Color(0, 102, 204));
        b1.setForeground(Color.WHITE);
        b1.setFont(new Font("SansSerif", Font.BOLD, 14));
        b1.addActionListener(this);
        add(b1);

        b2 = new JButton("Cancel");
        b2.setBounds(260, 200, 120, 35);
        b2.setBackground(Color.GRAY);
        b2.setForeground(Color.WHITE);
        b2.setFont(new Font("SansSerif", Font.BOLD, 14));
        b2.addActionListener(this);
        add(b2);

        // Forgot Password Button
        forgotButton = new JButton("Forgot Password?");
        forgotButton.setBounds(150, 260, 200, 30);
        forgotButton.setBackground(Color.WHITE);
        forgotButton.setForeground(new Color(0, 102, 204));
        forgotButton.setBorderPainted(false);
        forgotButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        forgotButton.addActionListener(this);
        add(forgotButton);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == b1) { // Login
            String email = t1.getText().trim();
            String password = new String(t2.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields");
                return;
            }

            String q = "SELECT * FROM users WHERE email=? AND password_hash=?";
            try (Connection con = Connect_Db.getConnection();
                 PreparedStatement pst = con.prepareStatement(q)) {

                pst.setString(1, email);
                pst.setString(2, password);

                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String userName = rs.getString("name");
                    new Home(userId,userName ).setVisible(true);
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid login");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            }

        } else if (ae.getSource() == b2) { // Cancel
            System.exit(0);

        } else if (ae.getSource() == forgotButton) { // Forgot Password
            String email = JOptionPane.showInputDialog(
                    this,                     // parent component
                    "Enter your email:",       // message
                    "Forgot Password",         // dialog title
                    JOptionPane.INFORMATION_MESSAGE  // message type (can also be WARNING_MESSAGE, etc.)
            );
            if (email != null && !email.isEmpty()) {
                try (Connection con = Connect_Db.getConnection();
                     PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE email=?")) {
                    pst.setString(1, email);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        String newPass = JOptionPane.showInputDialog(this, "Enter new password:");
                        if (newPass != null && !newPass.isEmpty()) {
                            PreparedStatement updatePst = con.prepareStatement(
                                    "UPDATE users SET password_hash=? WHERE email=?");
                            updatePst.setString(1, newPass);
                            updatePst.setString(2, email);
                            updatePst.executeUpdate();
                            JOptionPane.showMessageDialog(this, "Password updated successfully!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Email not found.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        new Login();
    }
}
