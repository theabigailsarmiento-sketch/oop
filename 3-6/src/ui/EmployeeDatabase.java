package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.Employee;
import model.IAdminOperations;
import model.RegularStaff;
import service.EmployeeManagementService;
import util.EmployeeDetailForm;

public class EmployeeDatabase extends JPanel {
    private CardLayout masterlistLayout;
    private JPanel masterlistContainer;
    private DefaultTableModel empTableModel;
    private JTable empTable;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    private final EmployeeManagementService employeeManagementService;
    private final Employee currentUser;

    public EmployeeDatabase(EmployeeManagementService service, Employee user) {
        this.employeeManagementService = service;
        this.currentUser = user;
        setLayout(new BorderLayout());
        add(createMasterlistPanel());
        refreshTable();
    }

    private JPanel createMasterlistPanel() {
        masterlistLayout = new CardLayout();
        masterlistContainer = new JPanel(masterlistLayout);
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));

        // --- NEW SEARCH BAR UI ---
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 30));
        setupPlaceholder(searchField, "Search by Name, ID, or Position...");
        
        JLabel searchIcon = new JLabel("🔍 Search: ");
        searchIcon.setFont(new Font("Tahoma", Font.BOLD, 12));
        
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // --- TABLE SETUP ---
        String[] columns = {"ID", "Last Name", "First Name", "Status", "Position", "Supervisor"};
        empTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        empTable = new JTable(empTableModel);
        
        // --- SEARCH LOGIC ---
        rowSorter = new TableRowSorter<>(empTableModel);
        empTable.setRowSorter(rowSorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String text = searchField.getText();
                if (text.trim().isEmpty() || text.equals("Search by Name, ID, or Position...")) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        JButton btnAddNew = new JButton("Add");
        JButton btnView = new JButton("View Details");
        JButton btnDelete = new JButton("Delete");
        
        styleButton(btnAddNew, new Color(0, 82, 204));
        styleButton(btnView, new Color(70, 130, 180));
        styleButton(btnDelete, new Color(255, 0, 0));

        btnAddNew.addActionListener(e -> masterlistLayout.show(masterlistContainer, "FORM"));

       // Inside EmployeeDatabase.java -> createMasterlistPanel()
btnView.addActionListener(e -> {
    int row = empTable.getSelectedRow();
    if (row != -1) {
        int modelRow = empTable.convertRowIndexToModel(row); 
        try {
            int empId = Integer.parseInt(empTableModel.getValueAt(modelRow, 0).toString());
            
            // Requesting the Model data through the Service Layer
            Object[] details = employeeManagementService.getEmployeeDetailsForForm(empId);
            
            if (details != null) {
                // Launching the UI (Detail Form) and passing the Service + User
                new EmployeeDetailForm(details, employeeManagementService, currentUser).setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
});
      
        btnDelete.addActionListener(e -> {
            int row = empTable.getSelectedRow();
            if (row != -1) {
                int modelRow = empTable.convertRowIndexToModel(row);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this employee?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    int empId = Integer.parseInt(empTableModel.getValueAt(modelRow, 0).toString());
                    if (employeeManagementService.removeEmployee((IAdminOperations)currentUser, empId)) { 
                        refreshTable(); 
                        JOptionPane.showMessageDialog(this, "Deleted."); 
                    }
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.add(btnAddNew); 
        btnPanel.add(btnView);  
        btnPanel.add(btnDelete);
        
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(empTable), BorderLayout.CENTER);
        tablePanel.add(btnPanel, BorderLayout.SOUTH);

        masterlistContainer.add(tablePanel, "TABLE");
        masterlistContainer.add(createNewHireFormPanel(), "FORM");
        
        return masterlistContainer;
    }

    private JPanel createNewHireFormPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));

        JPanel personalPanel = createFormSection("Personal Information", 0, 2);
        JTextField fName = new JTextField(); 
        JTextField lName = new JTextField();
        JTextField bday = new JTextField("MM/dd/yyyy");
        JButton btnPickBday = new JButton("📅 Pick Date");
        
        btnPickBday.addActionListener(e -> {
            DatePicker picker = new DatePicker(mainPanel);
            String pickedValue = picker.setPickedDate();
            if (!pickedValue.isEmpty()) {
                bday.setText(pickedValue);
                bday.setForeground(Color.BLACK);
            }
        });

        JTextField address = new JTextField();
        JTextField phone = new JTextField();
        ((javax.swing.text.AbstractDocument) phone.getDocument()).setDocumentFilter(new util.MaskFormatterFilter("###-###-###"));
        setupPlaceholder(phone, "000-000-000");

        JComboBox<String> gender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        addFormField(personalPanel, "First Name *", fName);
        addFormField(personalPanel, "Last Name *", lName);
        personalPanel.add(new JLabel("Birthday *")); 
        JPanel bw = new JPanel(new BorderLayout()); bw.add(bday); bw.add(btnPickBday, BorderLayout.EAST);
        personalPanel.add(bw);
        addFormField(personalPanel, "Gender:", gender);
        addFormField(personalPanel, "Address:", address);
        addFormField(personalPanel, "Phone Number:", phone);

        // ... Identification and Status panels (Simplified for space, but logic is same)
        JPanel govPanel = createFormSection("Identification & Status", 0, 2);
        JTextField sss = new JTextField(); setupPlaceholder(sss, "00-0000000-0");
        JTextField phil = new JTextField(); setupPlaceholder(phil, "000000000000");
        JTextField tin = new JTextField(); setupPlaceholder(tin, "000-000-000-000");
        JTextField pagibig = new JTextField(); setupPlaceholder(pagibig, "000000000000");
        JComboBox<String> status = new JComboBox<>(new String[]{"Regular", "Probationary"});
        
        addFormField(govPanel, "SSS #:", sss);
        addFormField(govPanel, "Philhealth #:", phil);
        addFormField(govPanel, "TIN #:", tin);
        addFormField(govPanel, "Pag-ibig #:", pagibig);
        addFormField(govPanel, "Status:", status);

        JPanel jobPanel = createFormSection("Employment", 0, 2);
        JTextField pos = new JTextField();
        JComboBox<String> superv = new JComboBox<>(new String[]{"N/A", "Garcia, Manuel III", "Lim, Antonio", "Villanueva, Andrea"}); 
        addFormField(jobPanel, "Position:", pos);
        addFormField(jobPanel, "Supervisor:", superv);

        JPanel financePanel = createFormSection("Financial Information", 0, 4);
        JTextField salary = new JTextField("0.00");
        JTextField rice = new JTextField("0.00");
        JTextField pallow = new JTextField("0.00");
        JTextField cloth = new JTextField("0.00");
        addFormField(financePanel, "Salary:", salary);
        addFormField(financePanel, "Rice:", rice);
        addFormField(financePanel, "Phone:", pallow);
        addFormField(financePanel, "Clothing:", cloth);

        formContainer.add(personalPanel); formContainer.add(govPanel); formContainer.add(jobPanel); formContainer.add(financePanel);

        JButton btnSave = new JButton("Confirm Hire");
        styleButton(btnSave, new Color(34, 139, 34));
        btnSave.addActionListener(e -> {
            try {
                String birthDateStr = bday.getText().trim();
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy");
                java.time.LocalDate birthDate = java.time.LocalDate.parse(birthDateStr, formatter);
                
                Employee newEmp = new RegularStaff(); 
                newEmp.setFirstName(fName.getText().trim());
                newEmp.setLastName(lName.getText().trim());
                newEmp.setBirthday(birthDate);
                newEmp.setBasicSalary(Double.parseDouble(salary.getText().trim()));
                // ... set other fields

                if (employeeManagementService.registerEmployee((IAdminOperations)currentUser, newEmp)) {
                    JOptionPane.showMessageDialog(this, "Success!");
                    refreshTable(); 
                    masterlistLayout.show(masterlistContainer, "TABLE"); 
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnBack = new JButton("Cancel"); 
        btnBack.addActionListener(e -> masterlistLayout.show(masterlistContainer, "TABLE"));
        bp.add(btnBack); bp.add(btnSave);
        
        mainPanel.add(new JScrollPane(formContainer), BorderLayout.CENTER);
        mainPanel.add(bp, BorderLayout.SOUTH);
        return mainPanel;
    }

    // --- DATE PICKER CLASS ---
    class DatePicker {
        int month, year;
        String day = "";
        JDialog d;
        JButton[] button = new JButton[42];

        public DatePicker(Component parent) {
            Window parentWindow = SwingUtilities.getWindowAncestor(parent);
            d = new JDialog(parentWindow instanceof Frame ? (Frame)parentWindow : null); 
            d.setModal(true); 
            d.setTitle("Select Date");
            d.setLayout(new BorderLayout());

            java.util.Calendar now = java.util.Calendar.getInstance();
            month = now.get(java.util.Calendar.MONTH);
            year = now.get(java.util.Calendar.YEAR) - 18;

            JPanel p1 = new JPanel(new GridLayout(7, 7));
            String[] header = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

            for (int x = 0; x < button.length; x++) {
                final int selection = x;
                button[x] = new JButton();
                if (x < 7) {
                    button[x].setText(header[x]);
                    button[x].setForeground(Color.RED);
                    button[x].setEnabled(false);
                } else {
                    button[x].addActionListener(e -> {
                        if (!button[selection].getText().isEmpty()) {
                            day = button[selection].getText();
                            d.dispose(); 
                        }
                    });
                }
                p1.add(button[x]);
            }

            JPanel p2 = new JPanel(new FlowLayout());
            JComboBox<String> monthCombo = new JComboBox<>(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
            monthCombo.setSelectedIndex(month);
            Integer[] yearsArr = new Integer[80]; 
            int startYear = now.get(java.util.Calendar.YEAR) - 18;
            for (int i = 0; i < 80; i++) yearsArr[i] = startYear - i;
            JComboBox<Integer> yearCombo = new JComboBox<>(yearsArr);
            yearCombo.setSelectedItem(year);

            monthCombo.addActionListener(e -> { month = monthCombo.getSelectedIndex(); displayDate(); });
            yearCombo.addActionListener(e -> { year = (Integer) yearCombo.getSelectedItem(); displayDate(); });

            p2.add(monthCombo); p2.add(yearCombo);
            d.add(p2, BorderLayout.NORTH); d.add(p1, BorderLayout.CENTER);
            d.pack(); d.setLocationRelativeTo(parent);
            displayDate();
            d.setVisible(true);
        }

        public void displayDate() {
            for (int x = 7; x < button.length; x++) button[x].setText("");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, 1);
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            for (int x = 6 + dayOfWeek, d = 1; d <= daysInMonth; x++, d++) button[x].setText("" + d);
        }

        public String setPickedDate() {
            if (day.isEmpty()) return "";
            return String.format("%02d/%02d/%04d", month + 1, Integer.parseInt(day), year);
        }
    }

    public final void refreshTable() {
        if (empTableModel == null) return;
        empTableModel.setRowCount(0);
        List<Employee> list = employeeManagementService.getAll();
        for (Employee emp : list) {
            empTableModel.addRow(new Object[]{ emp.getEmpNo(), emp.getLastName(), emp.getFirstName(), emp.getStatus(), emp.getPosition(), emp.getSupervisor() });
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setOpaque(true); btn.setBorderPainted(false);
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
    }

    private void setupPlaceholder(JTextField field, String hint) {
        field.setText(hint); field.setForeground(Color.GRAY);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { if (field.getText().equals(hint)) { field.setText(""); field.setForeground(Color.BLACK); } }
            @Override public void focusLost(java.awt.event.FocusEvent e) { if (field.getText().isEmpty()) { field.setForeground(Color.GRAY); field.setText(hint); } }
        });
    }

    private String getActualValue(JTextField field, String hint) {
        String val = field.getText().trim();
        return val.equals(hint) ? "" : val;
    }

    private void addFormField(JPanel p, String label, JComponent f) {
        p.add(new JLabel(label)); p.add(f);
    }

    private JPanel createFormSection(String title, int rows, int cols) {
        JPanel p = new JPanel(new GridLayout(rows, cols, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
        return p;
    }
}