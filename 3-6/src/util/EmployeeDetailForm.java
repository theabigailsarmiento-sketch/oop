package util;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import model.Employee;
import service.EmployeeManagementService;

public class EmployeeDetailForm extends JFrame {
    private JTextField[] fields;
    private JButton btnEdit, btnSave;
    private final EmployeeManagementService service;
    private final Employee currentUser;

    private final String[] labels = {
        "Employee #", "Last Name", "First Name", "Gender", "Birthday", "Address", "Phone #",
        "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
        "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
        "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate", "Role"
    };

    public EmployeeDetailForm(Object[] data, EmployeeManagementService service, Employee currentUser) {
        System.out.println("DEBUG: Raw Data Length = " + data.length);
    for(int i=0; i<data.length; i++) {
        System.out.println("Index " + i + ": " + data[i]);
    }
        
        this.service = service;
        this.currentUser = currentUser;

        setTitle("Employee Full Details");
        setSize(550, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Prepare Data using Service (Zero Logic in UI)
        String[] displayData = service.getFormattedDataForForm(data);

        // 2. Setup Panel
        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        fields = new JTextField[labels.length];

        // 3. Single Loop to build UI
        for (int i = 0; i < labels.length; i++) {
            mainPanel.add(new JLabel(labels[i] + ":"));
            
            String value = (displayData[i] != null) ? displayData[i] : "";
            
            fields[i] = new JTextField(value);
            fields[i].setEditable(false);
            
            // Apply Masks and numeric limits
            applyFilters(i, fields[i]);
            
            // Style non-editable fields (Emp #, Gross, Hourly, Role)
            if (i == 0 || i >= 18) {
                fields[i].setBackground(new Color(230, 230, 230));
            } else {
                fields[i].setBackground(Color.WHITE);
            }
            
            mainPanel.add(fields[i]);
        }

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        // Footer Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addActionButtons(footer);
        add(footer, BorderLayout.SOUTH);
        
        setVisible(true);
    }

private void applyFilters(int index, JTextField field) {
    AbstractDocument doc = (AbstractDocument) field.getDocument();
    switch (index) {
        case 6: // Phone # (This is now correct for your new mapping)
            doc.setDocumentFilter(new util.MaskFormatterFilter("###-###-###"));
            break;
        case 7: // SSS #
            doc.setDocumentFilter(new util.MaskFormatterFilter("##-#######-#"));
            break;
        case 10: // Pag-ibig #
            doc.setDocumentFilter(new NumericLimitFilter(12));
            break;
        case 14: case 15: case 16: case 17: // Financials (Salary, Rice, Phone, Cloth)
            doc.setDocumentFilter(new NumericLimitFilter(10)); 
            break;
    }
}

    private void addActionButtons(JPanel buttonPanel) {
        btnEdit = new JButton("Edit Employee");
        btnSave = new JButton("Save Changes");
        btnSave.setVisible(false);

        btnEdit.addActionListener(e -> {
            setFieldsEditable(true);
            btnEdit.setVisible(false);
            btnSave.setVisible(true);
        });

        btnSave.addActionListener(e -> {
            if (service.updateEmployeeFromForm(currentUser, fields)) {
                JOptionPane.showMessageDialog(this, "Employee Information Changes Saved");
                setFieldsEditable(false);
                btnSave.setVisible(false);
                btnEdit.setVisible(true);
            }
        });

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnSave);
    }

    private void setFieldsEditable(boolean active) {
        for (int i = 0; i < fields.length; i++) {
            // Unlock fields 1 through 17 (includes Gender, excludes Emp# and Rates)
            if (i > 0 && i < 18) { 
                fields[i].setEditable(active);
                fields[i].setBackground(active ? Color.WHITE : new Color(245, 245, 245));
            }
        }
    }

    // Helper Filter Class
    class NumericLimitFilter extends javax.swing.text.DocumentFilter {
        private final int limit;
        public NumericLimitFilter(int limit) { this.limit = limit; }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) 
                throws javax.swing.text.BadLocationException {
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = current.substring(0, offset) + text + current.substring(offset + length);
            if (next.matches("\\d*") && next.length() <= limit) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}