package ui;

import dao.AttendanceDAO;
import dao.UserLibrary;
import model.Employee;
import service.EmployeeManagementService;

import java.awt.*;
import javax.swing.*;

public class DashboardPanel extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    
    // Services and DAOs (Fields must be declared here to be accessible in the whole class)
    private final EmployeeManagementService employeeService; 
    private final AttendanceDAO attendanceDao;
    private final UserLibrary authService;
    private final Employee currentUser; // This is the 'currentUser' the error is looking for

    // User Session Data
    private final String userRole;
    private final String userEmpNo;
    private final String userLoggedIn;
    private final String userFirstname;
    private final String userLastname;
    private String selectedEmpNo;

    // Panels (Ensure these panel classes accept EmployeeManagementService in their constructor)
    public EmployeeDashboard homePanel;
    public AddEmployeePanel addEmpPanel;
    public FullDetailsPanel fullEmpPanel;
    public LeavePanel leaveApp;
    public TimePanel timeEmpPanel;

    public DashboardPanel(EmployeeManagementService empService, AttendanceDAO attDao, UserLibrary auth, Employee user) {
        // 1. Assign Fields
        this.employeeService = empService;
        this.attendanceDao = attDao;
        this.authService = auth;

        // 2. Extract and Convert Model Data
        // Fix: Convert Enum to String using .name()
        this.userRole = user.getRole().name();
        // Fix: Use getEmpNo() from your Employee model
        this.userEmpNo = String.valueOf(user.getEmpNo());
      
        this.userLoggedIn = userEmpNo; 
        this.userFirstname = user.getFirstName();
        this.userLastname = user.getLastName();

        // 3. Initialize Panels with the SERVICE baton (Architecture: UI -> Service)
        // Make sure these panel constructors are updated to take EmployeeManagementService
        this.homePanel = new EmployeeDashboard(employeeService); 
        this.addEmpPanel = new AddEmployeePanel(employeeService); 
        this.fullEmpPanel = new FullDetailsPanel(employeeService, currentUser);
        this.leaveApp = new LeavePanel();
        this.timeEmpPanel = new TimePanel();

        // 4. Frame Setup        
        setTitle("MotorPH Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);                                            
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 5. Navigation Panel Setup
        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(128, 0, 0));
        navPanel.setPreferredSize(new Dimension(220, getHeight()));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        JLabel adminTitle = new JLabel("Admin Dashboard");
        adminTitle.setForeground(Color.WHITE);
        adminTitle.setFont(new Font("Arial", Font.BOLD, 22));
        adminTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 6. UI Components (Buttons)
        JButton btnEmpHome = UIUtils.createNavButton("Home", Color.white, Color.white);
        JButton btnTime = UIUtils.createNavButton("Attendance", Color.white, Color.white);
        JButton btnLeaveReqButton = UIUtils.createNavButton("Leave Request", Color.white, Color.white);
        JButton btnLeaveApp = UIUtils.createNavButton("Leave Approval", Color.white, Color.white);
        JButton btnITApproval = UIUtils.createNavButton("IT Approval", Color.white, Color.white);
        JButton btnITsupp = UIUtils.createNavButton("IT Support", Color.white, Color.white);
        JButton btnDatabase = UIUtils.createNavButton("Employee Database", Color.white, Color.white);
        JButton btnPayroll = UIUtils.createNavButton("Payroll", Color.white, Color.white);
        JButton btnLogout = UIUtils.createNavButton("Log out", Color.white, Color.white);

        // 7. CardLayout Setup
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(homePanel, "Home");
        cardPanel.add(addEmpPanel, "AddEmployee");
        cardPanel.add(fullEmpPanel, "FullDetails");
        cardPanel.add(leaveApp, "Leave");
        cardPanel.add(timeEmpPanel, "Time");

        // 8. Assemble Navigation
        navPanel.add(Box.createVerticalStrut(30)); 
        navPanel.add(adminTitle);
        navPanel.add(Box.createVerticalStrut(40)); 

        addNavComponent(navPanel, btnEmpHome);
        addNavComponent(navPanel, btnTime);
        addNavComponent(navPanel, btnLeaveReqButton);
        addNavComponent(navPanel, btnLeaveApp);
        addNavComponent(navPanel, btnITApproval);
        addNavComponent(navPanel, btnITsupp);
        addNavComponent(navPanel, btnDatabase);
        addNavComponent(navPanel, btnPayroll);
        
        navPanel.add(Box.createVerticalGlue());
        addNavComponent(navPanel, btnLogout);

        // 9. Action Listeners (Using the Class Fields)
        btnEmpHome.addActionListener(e -> {
            homePanel.reloadCSV();
            switchPanel("Home");
        });

        btnDatabase.addActionListener(e -> {
            homePanel.reloadCSV();
            switchPanel("Home");
        });

        btnTime.addActionListener(e -> {
            timeEmpPanel.setLoggedIn(userLoggedIn, userLastname, userFirstname);
            switchPanel("Time");
        });

        btnLogout.addActionListener(e -> {
            dispose();
            // Pass dependencies back to Login
            new LoginPanel(employeeService, attendanceDao, authService).setVisible(true);
        });

        add(navPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
        
        switchPanel("Home"); 
        setVisible(true);
    }

    private void addNavComponent(JPanel panel, JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        panel.add(button);
        panel.add(Box.createVerticalStrut(15));
    }

    private void switchPanel(String cardName) {
        cardLayout.show(cardPanel, cardName);
    }
}