package ui;

import dao.EmployeeDAO;
import dao.UserLibrary;
import java.awt.*;
import javax.swing.*;
import model.Employee;
import model.Role;

public class LoginPanel extends JFrame {

    private final EmployeeDAO myHandler;
    private final UserLibrary authService;
    
    private JTextField empField;
    private JPasswordField passField;
    private int loginAttempts = 0;

    public LoginPanel(EmployeeDAO dao, UserLibrary auth) {
        this.myHandler = dao;
        this.authService = auth;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("MotorPH Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Retaining your split-pane design
        add(createLeftPanel(), BorderLayout.WEST);
        add(createRightPanel(), BorderLayout.CENTER);
        
        empField.requestFocusInWindow();
        setVisible(true);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(128, 0, 0)); // Your original Maroon
        leftPanel.setPreferredSize(new Dimension(400, 450));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Re-adding your logo logic
        try {
            java.net.URL imgURL = getClass().getResource("/logo.png");
            if (imgURL == null) imgURL = getClass().getResource("/resources/logo.png");

            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image scaled = icon.getImage().getScaledInstance(180, -1, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaled));
                gbc.gridy = 0;
                gbc.insets = new Insets(0, 0, 20, 0); 
                leftPanel.add(imageLabel, gbc);
            }
        } catch (Exception e) {}

        JLabel welcomeLabel = new JLabel("Welcome to MotorPH");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        gbc.gridy = 1;
        leftPanel.add(welcomeLabel, gbc);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(null); // Absolute layout for your bounds
        rightPanel.setBackground(Color.LIGHT_GRAY);

        JLabel loginLabel = new JLabel("Please log in with your credentials");
        loginLabel.setFont(new Font("Arial", Font.BOLD, 14));
        loginLabel.setBounds(50, 30, 300, 25);
        rightPanel.add(loginLabel);

        JLabel empLabel = new JLabel("Username :");
        empLabel.setBounds(50, 80, 100, 25);
        rightPanel.add(empLabel);

        empField = new JTextField();
        empField.setBounds(160, 80, 200, 25);
        empField.addActionListener(e -> handleLogin());
        rightPanel.add(empField);

        JLabel passLabel = new JLabel("Password :");
        passLabel.setBounds(50, 120, 100, 25);
        rightPanel.add(passLabel);

        passField = new JPasswordField();
        passField.setBounds(160, 120, 200, 25);
        passField.addActionListener(e -> handleLogin());
        rightPanel.add(passField);

        JCheckBox showPassword = new JCheckBox("Show");
        showPassword.setBounds(370, 120, 70, 25);
        showPassword.setBackground(Color.LIGHT_GRAY);
        showPassword.addActionListener(e -> passField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•'));
        rightPanel.add(showPassword);

        // Styled buttons as per your screenshot
        JButton loginButton = createStyledButton("Log in", new Color(30, 144, 255), Color.BLACK);
        loginButton.setBounds(160, 170, 90, 30);
        loginButton.addActionListener(e -> handleLogin());
        this.getRootPane().setDefaultButton(loginButton);
        rightPanel.add(loginButton);

        JButton exitButton = createStyledButton("Exit", Color.WHITE, Color.BLACK);
        exitButton.setBounds(270, 170, 90, 30);
        exitButton.addActionListener(e -> System.exit(0));
        rightPanel.add(exitButton); 

        JButton forgotBtn = createStyledButton("Forgot Password?", Color.WHITE, Color.BLACK);
    forgotBtn.setBounds(160, 220, 200, 30);
        forgotBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Contact your IT Support for password assistance."));
        rightPanel.add(forgotBtn);

        return rightPanel;
    }

   private JButton createStyledButton(String text, Color bg, Color fg) {
    JButton btn = new JButton(text);
    btn.setFocusPainted(false);
    btn.setForeground(fg);
    
    // Use BasicButtonUI to stop macOS from forcing the gray/rounded default look
    btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
    
    btn.setBackground(bg);
    btn.setContentAreaFilled(true);
    btn.setOpaque(true);
    
    // Create the rounded look using a LineBorder with 'true' for rounded corners
    if (bg.equals(new Color(30, 144, 255))) { 
        // Log in button: Blue with white border
        btn.setBorder(new javax.swing.border.LineBorder(Color.WHITE, 1, true));
    } else {
        // Exit & Forgot Password: White with Light Gray border
        btn.setBorder(new javax.swing.border.LineBorder(Color.LIGHT_GRAY, 1, true));
    }

    return btn;
}


    private void handleLogin() {
        String username = empField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        try {
            // Business Logic: Service handles the "Checking"
            if (authService.authenticate(username, password)) {
                Employee loggedUser = UserLibrary.getLoggedInEmployee();
                
                // Business Logic: Service decides the Role (Eduard = IT)
                Role role = authService.determineRole(loggedUser.getPosition());
                
                loginAttempts = 0;
                JOptionPane.showMessageDialog(this, "Welcome, " + loggedUser.getFirstName() + "!");
                
                // UI Navigation logic
                navigateToDashboard(role, loggedUser);
            } else {
                handleFailedAttempt();
            }
        } catch (Exception ex) {
            handleFailedAttempt();
        }
    }

    private void navigateToDashboard(Role role, Employee user) {
        this.dispose();
        switch (role) {
            case ADMIN: new AdminDashboard(myHandler, user).setVisible(true); break;
            case IT_STAFF: new ITDashboard(myHandler, user).setVisible(true); break;
            case HR_STAFF: new HRDashboard(myHandler, user).setVisible(true); break;
            default: new RegularStaffDashboard(myHandler, user).setVisible(true); break;
        }
    }

    private void handleFailedAttempt() {
        loginAttempts++;
        if (loginAttempts >= 3) {
            JOptionPane.showMessageDialog(this, "Too many failed attempts. Closing.");
            System.exit(0);
        }
        JOptionPane.showMessageDialog(this, "Invalid credentials. Attempts left: " + (3 - loginAttempts));
    }
}