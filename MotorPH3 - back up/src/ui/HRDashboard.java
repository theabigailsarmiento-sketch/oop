package ui;

import dao.EmployeeDAO;
import dao.UserLibrary;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.HRSerbisyo;
import service.LeaveStuff;

public class HRDashboard extends JFrame {

    // --- Dependency Injection Fields ---
    private final EmployeeDAO employeeDao;
    private final Employee currentUser;
    private final LeaveStuff leaveService;
    private final HRSerbisyo hrService;

    // --- UI Layout Components ---
    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    // --- Masterlist Components ---
    private JTable empTable; 
    private DefaultTableModel empTableModel;
    
    // --- Leave Approval Components ---
    private JTable leaveTable;
    private DefaultTableModel leaveApprovalModel;

    // --- Personal Information Fields (Standardized Names) ---
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextField txtSalary, txtRice, txtPhoneAllowance, txtClothing, txtGross, txtHourly;

    /**
     * CONSTRUCTOR: Following OOP Architecture (DAO -> Service -> UI)
     */
    public HRDashboard(EmployeeDAO dao, Employee user) {
        this.employeeDao = dao;
        this.currentUser = user;
        this.leaveService = new LeaveStuff(dao);                                                                                    
        this.hrService = new HRSerbisyo(dao);   

        setTitle("MotorPH HR Portal - " + user.getFirstName() + " " + user.getLastName());
        setSize(1300, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize CardLayout for navigation
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Build Panels
        cardPanel.add(createHomePanel(), "HOME");
        cardPanel.add(createTimeTrackingPanel(), "TIME");
        cardPanel.add(createLeaveApplicationPanel(), "LEAVE_APP");
        cardPanel.add(createMasterlistPanel(), "MASTERLIST");
        cardPanel.add(createLeaveApprovalPanel(), "LEAVE_APPROVALS");

        add(createSidebar(), BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        // Initial Data Load
        loadPersonalDetails(this.currentUser);     
        refreshTable();       
        refreshLeaveTable();
    }

    /**
     * MAPPING: Maps Model data to UI text fields.
     */
    private void loadPersonalDetails(Employee emp) {
        if (emp == null) return;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        txtEmpNo.setText(String.valueOf(currentUser.getEmpNo()));
        txtLastName.setText(currentUser.getLastName());
        txtFirstName.setText(currentUser.getFirstName());
        txtStatus.setText(currentUser.getStatus());
        txtPosition.setText(currentUser.getPosition());
        txtSupervisor.setText(currentUser.getSupervisor());
        if (currentUser.getBirthday() != null) txtBirthday.setText(currentUser.getBirthday().format(dateFormatter));
        txtAddress.setText(currentUser.getAddress());
        txtPhone.setText(currentUser.getPhone());
        txtSss.setText(currentUser.getSss());
        txtPhilHealth.setText(currentUser.getPhilhealth());
        txtTin.setText(currentUser.getTin());
        txtPagibig.setText(currentUser.getPagibig());
        txtSalary.setText(String.format("%.2f", currentUser.getBasicSalary()));
        txtRice.setText(String.format("%.2f", currentUser.getRiceSubsidy()));
        txtPhoneAllowance.setText(String.format("%.2f", currentUser.getPhoneAllowance()));
        txtClothing.setText(String.format("%.2f", currentUser.getClothingAllowance()));
        txtGross.setText(String.format("%.2f", currentUser.getGrossRate()));
        txtHourly.setText(String.format("%.2f", currentUser.getHourlyRate()));
    }

    private JPanel createHomePanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Group 1: Employee Work Info
        JPanel infoPanel = createGridPanel("Employee Information", 3, 4);
        txtEmpNo = addField(infoPanel, "EmployeeNo:", false);
        txtLastName = addField(infoPanel, "LastName:", false);
        txtFirstName = addField(infoPanel, "FirstName:", false);
        txtStatus = addField(infoPanel, "Status:", false);
        txtPosition = addField(infoPanel, "Position:", false);
        txtSupervisor = addField(infoPanel, "Supervisor:", false);

        // Group 2: Personal ID Info
        JPanel personalPanel = createGridPanel("Personal Information", 7, 2);
        txtBirthday = addField(personalPanel, "Birthday:", false);
        txtAddress = addField(personalPanel, "Address:", true);
        txtPhone = addField(personalPanel, "Phone:", true);
        txtSss = addField(personalPanel, "SSS:", false);
        txtPhilHealth = addField(personalPanel, "PhilHealth:", false);
        txtTin = addField(personalPanel, "TIN:", false);
        txtPagibig = addField(personalPanel, "Pagibig:", false);

        // Group 3: Payroll Info
        JPanel financePanel = createGridPanel("Financial Information", 3, 4);
        txtSalary = addField(financePanel, "Basic Salary:", false);
        txtRice = addField(financePanel, "Rice Subsidy:", false);
        txtPhoneAllowance = addField(financePanel, "Phone Allowance:", false);
        txtClothing = addField(financePanel, "Clothing Allowance:", false);
        txtGross = addField(financePanel, "Gross Rate:", false);
        txtHourly = addField(financePanel, "Hourly Rate:", false);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnUpdate = new JButton("Update Personal Info");
        styleButton(btnUpdate, new Color(34, 139, 34));
        
        btnUpdate.addActionListener(e -> {
            currentUser.setAddress(txtAddress.getText());
            currentUser.setPhone(txtPhone.getText());
            employeeDao.update(currentUser); 
            JOptionPane.showMessageDialog(this, "Profile Updated!");
        });

        actionPanel.add(btnUpdate);
        mainPanel.add(infoPanel); 
        mainPanel.add(personalPanel); 
        mainPanel.add(financePanel);
        mainPanel.add(actionPanel);

        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(mainPanel), BorderLayout.CENTER); }};
    }

    private void handleLeaveAction(String newStatus) {
        int selectedRow = leaveTable.getSelectedRow();
        if (selectedRow != -1) {
            int empId = Integer.parseInt(leaveTable.getValueAt(selectedRow, 0).toString());
            String startDate = leaveTable.getValueAt(selectedRow, 4).toString(); 
            
            hrService.updateLeaveStatus(empId, startDate, newStatus);
            refreshLeaveTable();
            JOptionPane.showMessageDialog(this, "Leave Request " + newStatus);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a leave request from the table first.");
        }
    }

    private void refreshTable() {
        if (empTableModel == null) return;
        empTableModel.setRowCount(0);
        java.util.List<Employee> latestList = employeeDao.getAll();
        for (Employee emp : latestList) {
            empTableModel.addRow(new Object[]{
                emp.getEmpNo(), emp.getLastName(), emp.getFirstName(), 
                emp.getStatus(), emp.getPosition(), emp.getSupervisor()
            });
        }
    }

    private void refreshLeaveTable() {
        if (leaveApprovalModel == null) return;
        leaveApprovalModel.setRowCount(0);
        Object[][] data = hrService.getAllLeaveRequestsForTable();
        if (data != null) {
            for (Object[] row : data) leaveApprovalModel.addRow(row);
        }
    }

    // --- UI Utility Methods ---

    private JPanel createSidebar() {
        JPanel nav = new JPanel();
        nav.setBackground(new Color(128, 0, 0));
        nav.setPreferredSize(new Dimension(220, getHeight()));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        nav.add(Box.createVerticalStrut(30));
        addSidebarLabel(nav, "Welcome HR,", 12, Font.PLAIN);
        addSidebarLabel(nav, currentUser.getFirstName() + "!", 14, Font.BOLD);

        nav.add(Box.createVerticalStrut(40));
        addNavButton(nav, "Home", e -> cardLayout.show(cardPanel, "HOME"));
        addNavButton(nav, "Time", e -> cardLayout.show(cardPanel, "TIME"));
        addNavButton(nav, "Apply Leave", e -> cardLayout.show(cardPanel, "LEAVE_APP"));
        
        nav.add(Box.createVerticalStrut(20));
        nav.add(new JSeparator(JSeparator.HORIZONTAL));
        nav.add(Box.createVerticalStrut(20));

        addNavButton(nav, "Employee Masterlist", e -> {
            refreshTable(); 
            cardLayout.show(cardPanel, "MASTERLIST");
        });

        addNavButton(nav, "Leave Approval", e -> {
            refreshLeaveTable(); 
            cardLayout.show(cardPanel, "LEAVE_APPROVALS");
        });

        nav.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("Log out");
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.addActionListener(e -> { 
            new LoginPanel(employeeDao, new UserLibrary(employeeDao)).setVisible(true); 
            this.dispose(); 
        });
        nav.add(btnLogout);
        nav.add(Box.createVerticalStrut(20));

        return nav;
    }

    private JTextField addField(JPanel p, String label, boolean edit) {
        p.add(new JLabel(label));
        JTextField f = createField(edit);
        p.add(f);
        return f;
    }

    private JPanel createGridPanel(String title, int r, int c) {
        JPanel p = new JPanel(new GridLayout(r, c, 10, 5));
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    private JTextField createField(boolean editable) { 
        JTextField f = new JTextField(); 
        f.setEditable(editable); 
        if(!editable) f.setBackground(new Color(240, 240, 240)); 
        return f; 
    }

    private void addNavButton(JPanel nav, String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(al);
        nav.add(btn); nav.add(Box.createVerticalStrut(10));
    }

    private void addSidebarLabel(JPanel nav, String text, int size, int style) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Tahoma", style, size));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(l);
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor); btn.setForeground(Color.WHITE);
        btn.setOpaque(true); btn.setBorderPainted(false);
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
    }

    // --- Sub-Panels for CardLayout ---

    private JPanel createTimeTrackingPanel() {
        JPanel timePanel = new JPanel(new BorderLayout(15, 15));
        timePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        JComboBox<String> monthPicker = new JComboBox<>(new String[]{"All", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
        header.add(new JLabel("View Month:")); header.add(monthPicker);
        
        DefaultTableModel model = new DefaultTableModel(new String[]{"Date", "Log In", "Log Out"}, 0);
        JTable table = new JTable(model);
        monthPicker.addActionListener(e -> {
            model.setRowCount(0);
            Object[][] data = employeeDao.getAttendanceByMonth(currentUser.getEmpNo(), (String) monthPicker.getSelectedItem());
            if (data != null) for (Object[] row : data) model.addRow(row);
        });

        timePanel.add(header, BorderLayout.NORTH);
        timePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return timePanel;
    }

    private JPanel createLeaveApplicationPanel() {
        JPanel leavePanel = new JPanel(new BorderLayout(20, 20));
        leavePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Request New Leave"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] leaveTypes = {"Vacation Leave", "Sick Leave", "Emergency Leave", "Maternity Leave", "Paternity Leave"};
        JComboBox<String> comboType = new JComboBox<>(leaveTypes);
        JTextField txtStart = createField(false);
        JTextField txtEnd = createField(false);
        JTextArea txtReason = new JTextArea(4, 25);
        txtReason.setLineWrap(true);
        txtReason.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JButton btnPickStart = new JButton("📅 Start Date");
        JButton btnPickEnd = new JButton("📅 End Date");
        JButton btnSubmit = new JButton("Submit Request");
        styleButton(btnSubmit, new Color(0, 51, 102));

        gbc.gridy = 0; form.add(new JLabel("Type:"), gbc);
        gbc.gridy = 1; form.add(comboType, gbc);
        gbc.gridy = 2; form.add(new JLabel("Start:"), gbc);
        gbc.gridy = 3; form.add(txtStart, gbc);
        gbc.gridy = 4; form.add(btnPickStart, gbc);
        gbc.gridy = 5; form.add(new JLabel("End:"), gbc);
        gbc.gridy = 6; form.add(txtEnd, gbc);
        gbc.gridy = 7; form.add(btnPickEnd, gbc);
        gbc.gridy = 8; form.add(new JLabel("Reason:"), gbc);
        gbc.gridy = 9; form.add(new JScrollPane(txtReason), gbc);
        gbc.gridy = 10; form.add(btnSubmit, gbc);

        String[] cols = {"Last Name", "First Name", "Start Date", "End Date", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(leaveService.getLeaveHistory(currentUser.getEmpNo()), cols);
        JTable table = new JTable(model);
        
        btnPickStart.addActionListener(e -> txtStart.setText(new DatePicker(this).setPickedDate()));
        btnPickEnd.addActionListener(e -> txtEnd.setText(new DatePicker(this).setPickedDate()));

        btnSubmit.addActionListener(e -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                LocalDate start = LocalDate.parse(txtStart.getText(), formatter);
                LocalDate end = LocalDate.parse(txtEnd.getText(), formatter);
                leaveService.submitLeave(currentUser.getEmpNo(), (String) comboType.getSelectedItem(), start, end, txtReason.getText());
                model.setDataVector(leaveService.getLeaveHistory(currentUser.getEmpNo()), cols);
                JOptionPane.showMessageDialog(this, "Leave submitted!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: Check date format."); }
        });

        leavePanel.add(form, BorderLayout.WEST);
        leavePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return leavePanel;
    }

    private JPanel createMasterlistPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        empTableModel = new DefaultTableModel(new String[]{"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"}, 0);
        empTable = new JTable(empTableModel);
        panel.add(new JScrollPane(empTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLeaveApprovalPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        leaveApprovalModel = new DefaultTableModel(new String[]{"Emp ID", "Last Name", "First Name", "Type", "Start", "End", "Reason", "Status"}, 0);
        leaveTable = new JTable(leaveApprovalModel);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApprove = new JButton("Approve");
        JButton btnDecline = new JButton("Decline");
        styleButton(btnApprove, new Color(34, 139, 34));
        styleButton(btnDecline, Color.RED);
        
        btnApprove.addActionListener(e -> handleLeaveAction("Approved"));
        btnDecline.addActionListener(e -> handleLeaveAction("Declined"));
        actionPanel.add(btnDecline); actionPanel.add(btnApprove);
        
        panel.add(new JScrollPane(leaveTable), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    // --- DatePicker Helper (Simplifed) ---
    class DatePicker { 
        int month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        JLabel l = new JLabel("", JLabel.CENTER);
        String day = "";
        JDialog d;
        JButton[] button = new JButton[42];

        public DatePicker(JFrame parent) {
            d = new JDialog(); d.setModal(true);
            JPanel p1 = new JPanel(new GridLayout(7, 7));
            for (int x = 0; x < button.length; x++) {
                final int selection = x;
                button[x] = new JButton();
                if (x > 6) button[x].addActionListener(e -> { day = button[selection].getActionCommand(); d.dispose(); });
                p1.add(button[x]);
            }
            displayDate();
            d.add(p1, BorderLayout.CENTER);
            d.pack(); d.setLocationRelativeTo(parent); d.setVisible(true);
        }

        public void displayDate() {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, 1);
            int days = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            for (int x = 7, d = 1; d <= days; x++, d++) button[x].setText("" + d);
        }

        public String setPickedDate() {
            if (day.equals("")) return "";
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, Integer.parseInt(day));
            return new java.text.SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
        }
    }
}