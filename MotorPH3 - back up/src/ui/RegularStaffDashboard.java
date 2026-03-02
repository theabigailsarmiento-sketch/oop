package ui;

import dao.EmployeeDAO;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Employee;
import service.LeaveStuff;

public class RegularStaffDashboard extends JFrame {
    private final EmployeeDAO employeeDao;
    private final Employee currentUser;
    private final LeaveStuff leaveService; 
    
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // UI Fields
    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextField txtSalary, txtRice, txtPhoneAllowance, txtClothing, txtGross, txtHourly;

    public RegularStaffDashboard(EmployeeDAO dao, Employee user) {
        this.employeeDao = dao;
        this.currentUser = user;
        this.leaveService = new LeaveStuff(dao);

        setTitle("MotorPH Portal - " + user.getFirstName());
        setSize(1300, 850); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        cardPanel.add(createHomePanel(), "HOME");
        cardPanel.add(createTimeTrackingPanel(), "TIME");
        cardPanel.add(createLeaveApplicationPanel(), "LEAVE");

        add(cardPanel, BorderLayout.CENTER);
        loadEmployeeDetails();
    }

    private JPanel createSidebar() {
        JPanel nav = new JPanel();
        nav.setBackground(new Color(128, 0, 0)); // Maroon
        nav.setPreferredSize(new Dimension(220, getHeight()));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        nav.add(Box.createVerticalStrut(30));
        addSidebarLabel(nav, "Welcome to MOTORPH,", 12, Font.PLAIN);
        addSidebarLabel(nav, currentUser.getFirstName() + "!", 14, Font.BOLD);
        
        nav.add(Box.createVerticalStrut(40));
        addNavButton(nav, "Home", e -> cardLayout.show(cardPanel, "HOME"));
        addNavButton(nav, "Time", e -> cardLayout.show(cardPanel, "TIME"));
        addNavButton(nav, "Leave Application", e -> cardLayout.show(cardPanel, "LEAVE"));

        nav.add(Box.createVerticalGlue());
        
        JButton btnLogout = new JButton("Log out");
        btnLogout.addActionListener(e -> { 
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { 
                new LoginPanel(employeeDao, new dao.UserLibrary(employeeDao)).setVisible(true); 
                this.dispose(); 
            }
        });
        nav.add(btnLogout);
        nav.add(Box.createVerticalStrut(20));
        return nav;
    }

    private JPanel createHomePanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Info Sections
        JPanel infoPanel = createGridPanel("Employee Information", 3, 4);
        txtEmpNo = addField(infoPanel, "EmployeeNo:", false);
        txtLastName = addField(infoPanel, "LastName:", false);
        txtFirstName = addField(infoPanel, "FirstName:", false);
        txtStatus = addField(infoPanel, "Status:", false);
        txtPosition = addField(infoPanel, "Position:", false);
        txtSupervisor = addField(infoPanel, "Supervisor:", false);

        JPanel personalPanel = createGridPanel("Personal Information", 7, 2);
        txtBirthday = addField(personalPanel, "Birthday:", false);
        txtAddress = addField(personalPanel, "Address:", true);
        txtPhone = addField(personalPanel, "Phone:", true);
        txtSss = addField(personalPanel, "SSS:", false);
        txtPhilHealth = addField(personalPanel, "PhilHealth:", false);
        txtTin = addField(personalPanel, "TIN:", false);
        txtPagibig = addField(personalPanel, "Pagibig:", false);

        JPanel financePanel = createGridPanel("Financial Information", 3, 4);
        txtSalary = addField(financePanel, "Basic Salary:", false);
        txtRice = addField(financePanel, "Rice Subsidy:", false);
        txtPhoneAllowance = addField(financePanel, "Phone Allowance:", false);
        txtClothing = addField(financePanel, "Clothing Allowance:", false);
        txtGross = addField(financePanel, "Gross Rate:", false);
        txtHourly = addField(financePanel, "Hourly Rate:", false);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        JButton btnUpdate = new JButton("Update Personal Info");
        styleButton(btnUpdate, new Color(34, 139, 34));
        btnUpdate.addActionListener(e -> updatePersonalInfo());

        JButton btnViewPayslip = new JButton("View Current Payslip");
        styleButton(btnViewPayslip, new Color(0, 32, 77));
        btnViewPayslip.addActionListener(e -> showCurrentPayslip());

        JButton btnExport = new JButton("Export to PDF/Print");
        styleButton(btnExport, new Color(64, 64, 64));
        btnExport.addActionListener(e -> exportPayslipToPDF());

        actionPanel.add(btnUpdate); 
        actionPanel.add(btnViewPayslip); 
        actionPanel.add(btnExport);

        mainPanel.add(infoPanel); 
        mainPanel.add(personalPanel); 
        mainPanel.add(financePanel);
        mainPanel.add(actionPanel);

        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(mainPanel), BorderLayout.CENTER); }};
    }

    private JPanel createTimeTrackingPanel() {
        JPanel timePanel = new JPanel(new BorderLayout(15, 15));
        timePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        String[] months = {"All", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthPicker = new JComboBox<>(months);
        header.add(new JLabel("View Month:"));
        header.add(monthPicker);

        JButton btnIn = new JButton("Check In");
        JButton btnOut = new JButton("Check Out");
        styleButton(btnIn, new Color(34, 139, 34)); 
        styleButton(btnOut, new Color(178, 34, 34)); 
        btnOut.setEnabled(false); 

        header.add(btnIn); header.add(btnOut);

        String[] columns = {"Date", "Log In", "Log Out"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

   Runnable refresh = () -> {
    model.setRowCount(0);
    String selectedMonth = (String) monthPicker.getSelectedItem();
    
    // We pass the month name (e.g., "June") directly to the DAO 
    // because your CSVHandler's isMonthMatch handles the conversion.
    Object[][] data = employeeDao.getAttendanceByMonth(currentUser.getEmpNo(), selectedMonth);
    
    if (data != null) {
        for (Object[] row : data) {
            // DATA IS ALREADY [Date, In, Out] FROM CSVHandler
            // Just add it directly!
            model.addRow(row); 
        }
    }
};

        btnIn.addActionListener(e -> {
            employeeDao.recordAttendance(currentUser.getEmpNo(), "Check-in");
            btnIn.setEnabled(false); btnOut.setEnabled(true);
            refresh.run();
        });

        btnOut.addActionListener(e -> {
            employeeDao.recordAttendance(currentUser.getEmpNo(), "Check-out");
            btnOut.setEnabled(false); btnIn.setEnabled(true);
            refresh.run();
        });

        monthPicker.addActionListener(e -> refresh.run());
        refresh.run();

        timePanel.add(header, BorderLayout.NORTH);
        timePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return timePanel;
    }
private JPanel createLeaveApplicationPanel() {
    JPanel leavePanel = new JPanel(new BorderLayout(20, 20));
    leavePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

    // Form Panel Setup
    JPanel form = new JPanel(new GridBagLayout());
    form.setBorder(BorderFactory.createTitledBorder("Request New Leave"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    String[] leaveTypes = {"Vacation Leave", "Sick Leave", "Emergency Leave", "Maternity Leave", "Paternity Leave"};
    JComboBox<String> comboType = new JComboBox<>(leaveTypes);
    
    JTextField txtStart = createField(false);
    JTextField txtEnd = createField(false);
    JTextArea txtReason = new JTextArea(4, 25);
    txtReason.setLineWrap(true);
    txtReason.setWrapStyleWord(true);
    txtReason.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    JButton btnPickStart = new JButton("📅 Start Date");
    JButton btnPickEnd = new JButton("📅 End Date");
    JButton btnSubmit = new JButton("Submit Request");
    styleButton(btnSubmit, new Color(0, 51, 102));

    gbc.gridy = 0; form.add(new JLabel("Type:"), gbc);
    gbc.gridy = 1; form.add(comboType, gbc);
    gbc.gridy = 2; form.add(new JLabel("Start Date:"), gbc);
    gbc.gridy = 3; form.add(txtStart, gbc);
    gbc.gridy = 4; form.add(btnPickStart, gbc);
    gbc.gridy = 5; form.add(new JLabel("End Date:"), gbc);
    gbc.gridy = 6; form.add(txtEnd, gbc);
    gbc.gridy = 7; form.add(btnPickEnd, gbc);
    gbc.gridy = 8; form.add(new JLabel("Reason:"), gbc);
    gbc.gridy = 9; form.add(new JScrollPane(txtReason), gbc);
    gbc.gridy = 10; form.add(btnSubmit, gbc);

    // --- TABLE LOGIC ---
    String[] cols = {"Last Name", "First Name", "Start Date", "End Date", "Reason", "Status"};
    
    // Status Renderer
    javax.swing.table.DefaultTableCellRenderer statusRenderer = new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (value != null) ? value.toString().trim() : "";
            c.setFont(c.getFont().deriveFont(java.awt.Font.BOLD));
            setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            if (status.equalsIgnoreCase("APPROVED")) c.setForeground(new java.awt.Color(0, 128, 0));
            else if (status.equalsIgnoreCase("DECLINED")) c.setForeground(java.awt.Color.RED);
            else c.setForeground(java.awt.Color.BLUE);
            return c;
        }
    };

    DefaultTableModel model = new DefaultTableModel(cols, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    
    JTable table = new JTable(model);
    table.setRowHeight(25);

    // Helper to refresh data from CSV
    Runnable refreshTable = () -> {
        Object[][] data = leaveService.getLeaveHistory(currentUser.getEmpNo());
        model.setDataVector(data, cols);
        // Re-apply renderer because setDataVector resets columns
        table.getColumnModel().getColumn(5).setCellRenderer(statusRenderer);
    };

    // Initial Load
    refreshTable.run();

    btnPickStart.addActionListener(e -> txtStart.setText(new DatePicker(this).setPickedDate()));
    btnPickEnd.addActionListener(e -> txtEnd.setText(new DatePicker(this).setPickedDate()));

    btnSubmit.addActionListener(e -> {
        if (txtStart.getText().isEmpty() || txtEnd.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select both Start and End dates.");
            return;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            leaveService.submitLeave(
                currentUser.getEmpNo(), 
                (String) comboType.getSelectedItem(), 
                LocalDate.parse(txtStart.getText(), formatter), 
                LocalDate.parse(txtEnd.getText(), formatter), 
                txtReason.getText()
            );

            refreshTable.run(); // Update UI
            JOptionPane.showMessageDialog(this, "Leave application submitted successfully!");
            txtStart.setText(""); txtEnd.setText(""); txtReason.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    });

    leavePanel.add(form, BorderLayout.WEST);
    leavePanel.add(new JScrollPane(table), BorderLayout.CENTER);    
    return leavePanel;
}

    private void updatePersonalInfo() {
        currentUser.setAddress(txtAddress.getText().trim());
        currentUser.setPhone(txtPhone.getText().trim());
        if (leaveService.updateEmployeeProfile(currentUser)) {
            JOptionPane.showMessageDialog(this, "Profile Updated!");
        }
    }

   private void showCurrentPayslip() {
    // RE-FETCH to ensure we have the latest numeric values from the DAO
    Employee latestData = employeeDao.findById(currentUser.getEmpNo());
    
    if (latestData == null || latestData.getBasicSalary() == 0) {
        JOptionPane.showMessageDialog(this, "Error: Salary data could not be loaded from CSV.", "Data Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    double totalGross = latestData.getBasicSalary() + 
                        latestData.getRiceSubsidy() + 
                        latestData.getPhoneAllowance() + 
                        latestData.getClothingAllowance();

    String payslip = String.format(
        "MOTORPH PAYSLIP\n------------------------------\n" +
        "Employee: %s %s\n" +
        "Basic Salary: %,.2f\n" +
        "Rice Subsidy: %,.2f\n" +
        "Phone Allowance: %,.2f\n" +
        "Clothing Allowance: %,.2f\n" +
        "------------------------------\n" +
        "Gross Rate: %,.2f",
        latestData.getFirstName(), latestData.getLastName(),
        latestData.getBasicSalary(), latestData.getRiceSubsidy(),
        latestData.getPhoneAllowance(), latestData.getClothingAllowance(), totalGross
    );
    
    JOptionPane.showMessageDialog(this, payslip, "My Payslip", JOptionPane.INFORMATION_MESSAGE);
}
   private void exportPayslipToPDF() {
    String fileName = "Payslip_EMP" + currentUser.getEmpNo() + ".txt";
    Employee latestData = employeeDao.findById(currentUser.getEmpNo());
    
    double totalGross = latestData.getBasicSalary() + latestData.getRiceSubsidy() + 
                        latestData.getPhoneAllowance() + latestData.getClothingAllowance();

    try (PrintWriter writer = new PrintWriter(new File(fileName))) {
        writer.println("========================================");
        writer.println("       MOTORPH OFFICIAL PAYSLIP         ");
        writer.println("========================================");
        writer.printf("Employee ID:    %d%n", latestData.getEmpNo());
        writer.printf("Name:           %s %s%n", latestData.getFirstName(), latestData.getLastName());
        writer.println("----------------------------------------");
        writer.printf("Basic Salary:   %,.2f%n", latestData.getBasicSalary());
        writer.printf("Rice Subsidy:   %,.2f%n", latestData.getRiceSubsidy());
        writer.printf("Phone Alw:      %,.2f%n", latestData.getPhoneAllowance());
        writer.printf("Clothing Alw:   %,.2f%n", latestData.getClothingAllowance());
        writer.println("----------------------------------------");
        writer.printf("TOTAL GROSS:    %,.2f%n", totalGross);
        writer.println("========================================");
        writer.println("Generated on: " + java.time.LocalDateTime.now());
        
        writer.flush();
        JOptionPane.showMessageDialog(this, "Payslip exported successfully to: " + fileName);
    } catch (FileNotFoundException ex) {
        JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void loadEmployeeDetails() {
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
        JTextField field = new JTextField();
        field.setEditable(editable);
        if (!editable) field.setBackground(new Color(245, 245, 245));
        return field;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(190, 35));
        btn.setOpaque(true); btn.setBorderPainted(false);
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
    }

    private void addNavButton(JPanel nav, String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 35));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(al);
        nav.add(btn); nav.add(Box.createVerticalStrut(10));
    }

    private void addSidebarLabel(JPanel nav, String text, int size, int style) {
        JLabel l = new JLabel(text); l.setForeground(Color.WHITE);
        l.setFont(new Font("Tahoma", style, size)); l.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(l);
    }


    public void handleSupportButton(Employee user) {
    user.requestTechnicalSupport("Hardware issue"); 
    // This works whether 'user' is an Admin, HR, or IT!
}
    
    class DatePicker { 
        int month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        JLabel l = new JLabel("", JLabel.CENTER);
        String day = "";
        JDialog d;
        JButton[] button = new JButton[42];

        public DatePicker(JFrame parent) {
            d = new JDialog(); d.setModal(true);
            String[] header = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
            JPanel p1 = new JPanel(new GridLayout(7, 7));
            p1.setPreferredSize(new Dimension(430, 120));

            for (int x = 0; x < button.length; x++) {
                final int selection = x;
                button[x] = new JButton(); button[x].setFocusPainted(false);
                button[x].setBackground(Color.WHITE);
                if (x > 6) {
                    button[x].addActionListener(e -> {
                        day = button[selection].getActionCommand();
                        d.dispose();
                    });
                }
                if (x < 7) { button[x].setText(header[x]); button[x].setForeground(Color.RED); }
                p1.add(button[x]);
            }
            JPanel p2 = new JPanel(new GridLayout(1, 3));
            JButton previous = new JButton("<< Previous");
            previous.addActionListener(e -> { month--; displayDate(); });
            p2.add(previous); p2.add(l);
            JButton next = new JButton("Next >>");
            next.addActionListener(e -> { month++; displayDate(); });
            p2.add(next);
            d.add(p1, BorderLayout.CENTER); d.add(p2, BorderLayout.SOUTH);
            d.pack(); d.setLocationRelativeTo(parent);
            displayDate(); d.setVisible(true);
        }

        public final void displayDate() {
            for (int x = 7; x < button.length; x++) button[x].setText("");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, 1);
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            for (int x = 6 + dayOfWeek, d = 1; d <= daysInMonth; x++, d++)
                button[x].setText("" + d);
            l.setText(new java.text.SimpleDateFormat("MMMM yyyy").format(cal.getTime()));
        }

        public String setPickedDate() {
            if (day.equals("")) return "";
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, Integer.parseInt(day));
            return new java.text.SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
        }
    }
}
