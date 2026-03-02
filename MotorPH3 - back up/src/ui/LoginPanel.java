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

    // LOGIC: Constructor now accepts the DAO and Service
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

        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        
        // ADDED: Request focus so user can type immediately
        empField.requestFocusInWindow();
        
        setVisible(true);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(128, 0, 0));
        leftPanel.setPreferredSize(new Dimension(400, 450));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        try {
            java.net.URL imgURL = getClass().getResource("/logo.png");
            if (imgURL == null) {
                imgURL = getClass().getResource("/resources/logo.png");
            }

            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image scaled = icon.getImage().getScaledInstance(180, -1, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaled));
                gbc.gridy = 0;
                gbc.insets = new Insets(0, 0, 20, 0); 
                leftPanel.add(imageLabel, gbc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel welcomeLabel = new JLabel("Welcome to MotorPH");
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        gbc.gridy = 1;
        leftPanel.add(welcomeLabel, gbc);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(null);
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
        // ADDED: Trigger login on Enter key
        empField.addActionListener(e -> handleLogin());
        rightPanel.add(empField);

        JLabel passLabel = new JLabel("Password :");
        passLabel.setBounds(50, 120, 100, 25);
        rightPanel.add(passLabel);

        passField = new JPasswordField();
        passField.setBounds(160, 120, 200, 25);
        // ADDED: Trigger login on Enter key
        passField.addActionListener(e -> handleLogin());
        rightPanel.add(passField);

        JCheckBox showPassword = new JCheckBox("Show");
        showPassword.setBounds(370, 120, 70, 25);
        showPassword.setBackground(Color.LIGHT_GRAY);
        showPassword.addActionListener(e -> passField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•'));
        rightPanel.add(showPassword);

        JButton loginButton = createStyledButton("Log in");
        loginButton.setBounds(160, 170, 90, 30);
        loginButton.addActionListener(e -> handleLogin());
        rightPanel.add(loginButton);
        
        // ADDED: Set this as the default button for the entire window
        this.getRootPane().setDefaultButton(loginButton);

        JButton exitButton = createStyledButton("Exit");
        exitButton.setBounds(270, 170, 90, 30);
        exitButton.addActionListener(e -> System.exit(0));
        rightPanel.add(exitButton);

        JButton resetButton = createStyledButton("Forgot Password?");
        resetButton.setBounds(160, 220, 200, 30);
        resetButton.addActionListener(e -> handlePasswordReset());
        rightPanel.add(resetButton);

        return rightPanel;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        return btn;
    }

    // LOGIC: Updated to use UserLibrary and routing
    private void handleLogin() {
        String username = empField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        if (authService.authenticate(username, password)) {
            Employee loggedUser = UserLibrary.getLoggedInEmployee();
            Role role = UserLibrary.getUserRole();
            
            JOptionPane.showMessageDialog(this, "Welcome, " + loggedUser.getFirstName() + "!");
            openDashboard(role, loggedUser);
        } else {
            loginAttempts++; 
            if (loginAttempts >= 3) {
                JOptionPane.showMessageDialog(this, "Security Alert: Too many failed attempts. Closing app.");
                System.exit(0);
            }
            JOptionPane.showMessageDialog(this, "Invalid credentials. Attempts left: " + (3 - loginAttempts));
        }
    }

    private void handlePasswordReset() {
        JOptionPane.showMessageDialog(this, 
            "Please contact the IT Department or your HR Manager to reset your password.", 
            "Password Reset", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    // LOGIC: Routing to proper dashboards based on the User Model
    // LOGIC: Routing to proper dashboards based on the User Model
    private void openDashboard(Role role, Employee user) {
        this.dispose(); 
        String pos = (user.getPosition() != null) ? user.getPosition().toLowerCase() : "";
        
        // 1. ADDED: Check for IT first
        if (pos.contains("it") || pos.contains("information technology")) {
            new ITDashboard(myHandler, user).setVisible(true);
        } 
        // 2. Existing HR logic
        else if (pos.contains("hr") || role == Role.HR_STAFF) {
            new HRDashboard(myHandler, user).setVisible(true);
        } 
        else if (pos.contains("accounting") || role == Role.ACCOUNTING) {
            // new AccountingDashboard(myHandler, user).setVisible(true);
        } 
        else if (role == Role.ADMIN) {
            // new AdminDashboard(myHandler, user).setVisible(true);
        } 
        // 3. Beatriz and others will fall here
        else {
            new RegularStaffDashboard(myHandler, user).setVisible(true);
        }
    }
}