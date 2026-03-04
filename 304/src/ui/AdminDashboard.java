package ui;

import dao.EmployeeDAO;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Admin;
import model.Employee;
import model.IAdminOperations;
import service.EmployeeManagementService;
import service.HRSerbisyo;
import service.LeaveStuff;

public class AdminDashboard extends JFrame {

    private final EmployeeDAO employeeDao;
    private final Employee currentUser;
    private final LeaveStuff leaveService;
    private final HRSerbisyo hrService;
    private final EmployeeManagementService employeeManagementService;

    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    private JTable empTable; 
    private DefaultTableModel empTableModel;
    private JTable leaveTable;
    private DefaultTableModel leaveApprovalModel;

    private CardLayout masterlistLayout;
    private JPanel masterlistContainer;

    private JTextField txtEmpNo, txtLastName, txtFirstName, txtStatus, txtPosition, txtSupervisor;
    private JTextField txtBirthday, txtAddress, txtPhone, txtSss, txtPhilHealth, txtTin, txtPagibig;
    private JTextField txtSalary, txtRice, txtPhoneAllowance, txtClothing, txtGross, txtHourly;

    public AdminDashboard(EmployeeDAO dao, Employee user) {
        this.employeeDao = dao;
        this.currentUser = user;
        this.leaveService = new LeaveStuff(dao);                                                                                    
        this.hrService = new HRSerbisyo(dao);   
        this.employeeManagementService = new EmployeeManagementService(dao);

        setTitle("MotorPH Admin Portal - " + user.getFirstName() + " " + user.getLastName());
        setSize(1300, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createHomePanel(), "HOME");
        cardPanel.add(createTimeTrackingPanel(), "TIME");
        cardPanel.add(createLeaveApplicationPanel(), "LEAVE_APP");
        cardPanel.add(createMasterlistPanel(), "MASTERLIST");
        cardPanel.add(createLeaveApprovalPanel(), "LEAVE_APPROVALS");

        add(createSidebar(), BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        loadPersonalDetails(this.currentUser);     
        refreshTable();       
        refreshLeaveTable();
    }

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
            JOptionPane.showMessageDialog(this, "Please select a leave request first.");
        }
    }

    

    private void refreshTable() {
        List<Employee> updatedList = employeeManagementService.getAllEmployees();
        if (empTableModel != null) {
            empTableModel.setRowCount(0);
            for (Employee emp : updatedList) {
                empTableModel.addRow(new Object[]{
                    emp.getEmpNo(), emp.getLastName(), emp.getFirstName(),
                    emp.getStatus(), emp.getPosition(), emp.getSupervisor()
                });
            }
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

    private JPanel createSidebar() {
        JPanel nav = new JPanel();
        nav.setBackground(new Color(128, 0, 0));
        nav.setPreferredSize(new Dimension(220, getHeight()));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        nav.add(Box.createVerticalStrut(30));
        addSidebarLabel(nav, "Welcome Admin,", 12, Font.PLAIN);
        addSidebarLabel(nav, currentUser.getFirstName() + "!", 14, Font.BOLD);

        nav.add(Box.createVerticalStrut(40));
        addNavButton(nav, "Home", e -> cardLayout.show(cardPanel, "HOME"));
        addNavButton(nav, "Time", e -> cardLayout.show(cardPanel, "TIME"));
        addNavButton(nav, "Apply Leave", e -> cardLayout.show(cardPanel, "LEAVE_APP"));
        
        nav.add(Box.createVerticalStrut(20));
        nav.add(new JSeparator(JSeparator.HORIZONTAL));
        nav.add(Box.createVerticalStrut(20));

        addNavButton(nav, "Employee Database", e -> { refreshTable(); cardLayout.show(cardPanel, "MASTERLIST"); });
        addNavButton(nav, "Leave Approval", e -> { refreshLeaveTable(); cardLayout.show(cardPanel, "LEAVE_APPROVALS"); });

        nav.add(Box.createVerticalGlue());
        JButton btnLogout = new JButton("Log out");
    btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
    btnLogout.setPreferredSize(new Dimension(100, 30));
    btnLogout.setBackground(Color.WHITE);
    btnLogout.setForeground(Color.BLACK);
    btnLogout.setOpaque(true);
btnLogout.setBorderPainted(false);
btnLogout.setFocusPainted(false);
btnLogout.setFont(new Font("Tahoma", Font.BOLD, 11));

btnLogout.addActionListener(e -> { 
    int confirm = JOptionPane.showConfirmDialog(this, "Log out?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) { 
        new LoginPanel(employeeDao, new dao.UserLibrary(employeeDao)).setVisible(true); 
        this.dispose(); 
    }
});
        nav.add(btnLogout);
        nav.add(Box.createVerticalStrut(20));
        return nav;
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
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Request New Leave"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> comboType = new JComboBox<>(new String[]{"Vacation Leave", "Sick Leave", "Emergency Leave"});
        JTextField txtStart = createField(false);
        JTextField txtEnd = createField(false);
        JTextArea txtReason = new JTextArea(4, 25);
        txtReason.setLineWrap(true);

        JButton btnPickStart = new JButton("📅 Start Date");
        JButton btnPickEnd = new JButton("📅 End Date");
        JButton btnSubmit = new JButton("Submit Request");
        styleButton(btnSubmit, new Color(0, 51, 102));

        styleButtonToWhite(btnPickStart);
styleButtonToWhite(btnPickEnd);
styleButtonToWhite(btnSubmit);

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
        
        btnPickStart.addActionListener(e -> txtStart.setText(new DatePicker(this).setPickedDate()));
        btnPickEnd.addActionListener(e -> txtEnd.setText(new DatePicker(this).setPickedDate()));
        btnSubmit.addActionListener(e -> {
            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                leaveService.submitLeave(currentUser.getEmpNo(), (String) comboType.getSelectedItem(), 
                    LocalDate.parse(txtStart.getText(), f), LocalDate.parse(txtEnd.getText(), f), txtReason.getText());
                model.setDataVector(leaveService.getLeaveHistory(currentUser.getEmpNo()), cols);
                JOptionPane.showMessageDialog(this, "Leave submitted!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error in dates."); }
        });

        leavePanel.add(form, BorderLayout.WEST);
        leavePanel.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        return leavePanel;
    }

    private JPanel createMasterlistPanel() {
        masterlistLayout = new CardLayout();
        masterlistContainer = new JPanel(masterlistLayout);
        JPanel tableView = new JPanel(new BorderLayout(15, 15));
        tableView.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        empTableModel = new DefaultTableModel(new String[]{"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"}, 0);
        empTable = new JTable(empTableModel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Add New Hire");
        JButton btnDelete = new JButton("Delete Selected");
        btnAdd.setForeground(Color.BLACK);
        btnDelete.setForeground(Color.BLACK);

        btnDelete.addActionListener(e -> {
             int selectedRow = empTable.getSelectedRow();
             if (selectedRow != -1) {
                 int empId = (int) empTable.getValueAt(selectedRow, 0);
                 if(employeeManagementService.deleteEmployee((IAdminOperations) currentUser, empId)) {
                     refreshTable();
                 }
             }
        });

        actionPanel.add(btnAdd); actionPanel.add(btnDelete);
        tableView.add(new JScrollPane(empTable), BorderLayout.CENTER);
        tableView.add(actionPanel, BorderLayout.SOUTH);

        masterlistContainer.add(tableView, "TABLE");
        masterlistContainer.add(createNewHireFormPanel(), "FORM");
        btnAdd.addActionListener(e -> masterlistLayout.show(masterlistContainer, "FORM"));
        return masterlistContainer;
    }

    private JPanel createNewHireFormPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));

        JPanel personalPanel = createFormSection("Personal Information", 5, 2);
        JTextField fName = new JTextField(); JTextField lName = new JTextField();
        JTextField bday = new JTextField("MM/dd/yyyy");
        JButton btnPickBday = new JButton("📅 Pick Date");
        btnPickBday.addActionListener(e -> bday.setText(new DatePicker(this).setPickedDate()));
        JTextField addr = new JTextField(); JTextField contact = new JTextField();
        addFormField(personalPanel, "First Name:", fName);
        addFormField(personalPanel, "Last Name:", lName);
        personalPanel.add(new JLabel("Birthday:")); 
        JPanel bw = new JPanel(new BorderLayout()); bw.add(bday); bw.add(btnPickBday, BorderLayout.EAST);
        personalPanel.add(bw);
        addFormField(personalPanel, "Address:", addr);
        addFormField(personalPanel, "Phone #:", contact);

        JPanel idPanel = createFormSection("Government Identifications", 4, 2);
        JTextField sss = new JTextField(); JTextField ph = new JTextField();
        JTextField tin = new JTextField(); JTextField pag = new JTextField();
        addFormField(idPanel, "SSS:", sss); addFormField(idPanel, "PhilHealth:", ph);
        addFormField(idPanel, "TIN:", tin); addFormField(idPanel, "Pagibig:", pag);

        JPanel jobPanel = createFormSection("Employment & Salary", 5, 2);
        JTextField pos = new JTextField();
        JComboBox<String> deptDropdown = new JComboBox<>(new String[]{"Executive", "IT Operations and Systems", "HR", "Accounting", "Sales & Marketing", "Supply Chain and Logistics"});
        JComboBox<String> supervDropdown = new JComboBox<>();
        supervDropdown.addItem("N/A");
        java.util.Set<String> uniqueSupervisors = new java.util.LinkedHashSet<>();
        for (Employee emp : employeeManagementService.getAllEmployees()) {
            if(emp.getSupervisor() != null && !emp.getSupervisor().isEmpty()) 
                uniqueSupervisors.add(emp.getSupervisor().replace("\"", "").trim());
        }
        for (String s : uniqueSupervisors) supervDropdown.addItem(s);
        JComboBox<String> stat = new JComboBox<>(new String[]{"Regular", "Probationary"});
        JTextField salary = new JTextField("0.00");
        addFormField(jobPanel, "Position:", pos); addFormField(jobPanel, "Dept:", deptDropdown);
        addFormField(jobPanel, "Supervisor:", supervDropdown); addFormField(jobPanel, "Status:", stat);
        addFormField(jobPanel, "Salary:", salary);

        formContainer.add(personalPanel); formContainer.add(idPanel); formContainer.add(jobPanel);
        
        JButton btnSave = new JButton("Confirm Hire");
        styleButton(btnSave, new Color(34, 139, 34));
        btnSave.addActionListener(e -> {
            try {
                Employee newEmp = new Admin();
                newEmp.setEmpNo(employeeManagementService.getNextAvailableId());
                newEmp.setFirstName(fName.getText()); newEmp.setLastName(lName.getText());
                newEmp.setBirthday(LocalDate.parse(bday.getText(), DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                newEmp.setAddress(addr.getText()); newEmp.setPhone(contact.getText());
                newEmp.setSss(sss.getText()); newEmp.setPhilhealth(ph.getText());
                newEmp.setTin(tin.getText()); newEmp.setPagibig(pag.getText());
                newEmp.setPosition(pos.getText()); newEmp.setSupervisor((String)supervDropdown.getSelectedItem());
                newEmp.setStatus((String)stat.getSelectedItem());
                newEmp.setBasicSalary(Double.parseDouble(salary.getText()));
                if(employeeManagementService.registerNewEmployee((IAdminOperations)currentUser, newEmp)){
                    refreshTable(); masterlistLayout.show(masterlistContainer, "TABLE");
                }
            } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Input Error."); }
        });

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnBack = new JButton("Cancel"); btnBack.addActionListener(e -> masterlistLayout.show(masterlistContainer, "TABLE"));
        bp.add(btnBack); bp.add(btnSave);
        mainPanel.add(new JScrollPane(formContainer), BorderLayout.CENTER);
        mainPanel.add(bp, BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel createFormSection(String title, int r, int c) {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    private void addFormField(JPanel p, String l, JComponent f) { p.add(new JLabel(l)); p.add(f); }

    private JPanel createLeaveApprovalPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        leaveApprovalModel = new DefaultTableModel(new String[]{"Emp ID", "Last Name", "First Name", "Type", "Start", "End", "Reason", "Status"}, 0);
        leaveTable = new JTable(leaveApprovalModel);
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApprove = new JButton("Approve"); styleButton(btnApprove, new Color(34, 139, 34));
        btnApprove.addActionListener(e -> handleLeaveAction("Approved"));
        actionPanel.add(btnApprove);
        panel.add(new JScrollPane(leaveTable), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JTextField addField(JPanel p, String label, boolean edit) {
        p.add(new JLabel(label));
        JTextField f = createField(edit); p.add(f); return f;
    }

    private JPanel createGridPanel(String title, int r, int c) {
        JPanel p = new JPanel(new GridLayout(r, c, 10, 5));
        p.setBorder(BorderFactory.createTitledBorder(title)); return p;
    }

    private JTextField createField(boolean editable) { 
        JTextField f = new JTextField(); f.setEditable(editable); 
        if(!editable) f.setBackground(new Color(240, 240, 240)); 
        return f; 
    }

    private void addNavButton(JPanel nav, String text, java.awt.event.ActionListener al) {
    JButton btn = new JButton(text);
    btn.setMaximumSize(new Dimension(190, 35)); // Fixed width for alignment
    btn.setPreferredSize(new Dimension(190, 35));
    btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    // Style to match the Beatriz dashboard
    btn.setBackground(Color.WHITE);
    btn.setForeground(Color.BLACK);
    btn.setFocusPainted(false);
    btn.setOpaque(true);
    btn.setBorderPainted(false);
    btn.setFont(new Font("Tahoma", Font.BOLD, 12));

    btn.addActionListener(al);
    nav.add(btn);
    nav.add(Box.createVerticalStrut(10));
}

    private void addSidebarLabel(JPanel nav, String text, int size, int style) {
        JLabel l = new JLabel(text); l.setForeground(Color.WHITE);
        l.setFont(new Font("Tahoma", style, size)); l.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(l);
    }

    private void styleButton(JButton btn, Color bg) {
    btn.setBackground(bg);
    btn.setForeground(Color.WHITE);
    btn.setPreferredSize(new Dimension(150, 35));
    btn.setFocusPainted(false);
    btn.setOpaque(true);           // Vital for background color to show
    btn.setBorderPainted(false);   // Vital for the flat look
    btn.setFont(new Font("Tahoma", Font.BOLD, 12));
    
    // Optional: Add a slight margin
    btn.setMargin(new Insets(5, 15, 5, 15));
}

private JButton createStyledButton(String text, Color bg, Color fg) {
    JButton btn = new JButton(text);
    btn.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // Force flat style
    btn.setFocusPainted(false);
    btn.setForeground(fg);
    btn.setBackground(bg);
    btn.setOpaque(true);
    
    // COMPOUND BORDER: Rounding (LineBorder) + Internal Padding (EmptyBorder)
    btn.setBorder(BorderFactory.createCompoundBorder(
        new javax.swing.border.LineBorder(new Color(200, 200, 200), 1, true), // The 'true' makes it rounded
        BorderFactory.createEmptyBorder(5, 15, 5, 15)
    ));

    return btn;
}

    // --- INNER CLASS: DATEPICKER ---
    class DatePicker {
        int month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String day = "";
        JDialog d;
        JButton[] button = new JButton[42];

        public DatePicker(JFrame parent) {
            d = new JDialog(); d.setModal(true); d.setTitle("Select Date"); d.setLayout(new BorderLayout());
            JPanel header = new JPanel(new FlowLayout());
            JButton btnPrev = new JButton("<<"); JButton btnNext = new JButton(">>");
            Integer[] years = new Integer[87]; for (int i = 0; i < years.length; i++) years[i] = 2026 - i;
            JComboBox<Integer> yearBox = new JComboBox<>(years); yearBox.setSelectedItem(year);
            JLabel lblMonth = new JLabel(); updateMonthLabel(lblMonth);
            btnPrev.addActionListener(e -> { month--; if(month < 0){ month = 11; year--; } updateMonthLabel(lblMonth); yearBox.setSelectedItem(year); displayDate(); });
            btnNext.addActionListener(e -> { month++; if(month > 11){ month = 0; year++; } updateMonthLabel(lblMonth); yearBox.setSelectedItem(year); displayDate(); });
            yearBox.addActionListener(e -> { year = (Integer) yearBox.getSelectedItem(); displayDate(); });
            header.add(btnPrev); header.add(lblMonth); header.add(btnNext); header.add(new JLabel(" Year: ")); header.add(yearBox);
            JPanel p1 = new JPanel(new GridLayout(7, 7));
            for (int x = 0; x < button.length; x++) {
                final int selection = x; button[x] = new JButton();
                button[x].addActionListener(e -> { day = button[selection].getActionCommand(); if (!day.equals("")) d.dispose(); });
                p1.add(button[x]);
            }
            displayDate();
            d.add(header, BorderLayout.NORTH); d.add(p1, BorderLayout.CENTER);
            d.pack(); d.setLocationRelativeTo(parent); d.setVisible(true);
        }

        private void updateMonthLabel(JLabel l) {
            String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
            l.setText(months[month]);
        }

        public void displayDate() {
            for (int x = 0; x < button.length; x++) button[x].setText("");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, 1);
            int startDay = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
            int days = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            for (int i = 0; i < days; i++) {
                button[startDay + i].setText(String.valueOf(i + 1));
                button[startDay + i].setActionCommand(String.valueOf(i + 1));
            }
        }

        public String setPickedDate() {
            if (day.equals("")) return "";
            return String.format("%02d/%02d/%d", month + 1, Integer.parseInt(day), year);
        }
    }

    private void styleButtonToWhite(JButton btn) {
    btn.setBackground(Color.WHITE);
    btn.setForeground(Color.BLACK);
    btn.setOpaque(true);
    btn.setBorderPainted(true); // Keep border for visibility on light gray
    btn.setFont(new Font("Tahoma", Font.BOLD, 11));
}
}