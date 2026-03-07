package util;

import java.awt.*;
import javax.swing.*;
import model.Employee; //
import service.EmployeeManagementService; //

public class EmployeeDetailForm extends JFrame {
    private JTextField[] fields;
    private JButton btnEdit, btnSave;
    
    // References to support UI-Service-DAO-Model structure
    private final EmployeeManagementService service;
    private final Employee currentUser;

    private final String[] labels = {
        "Employee #", "Last Name", "First Name", "Birthday", "Address", "Phone #",
        "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
        "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
        "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate", "Role"
    };

    // Updated constructor to accept Service and CurrentUser
    public EmployeeDetailForm(Object[] data, EmployeeManagementService service, Employee currentUser) {
        this.service = service;
        this.currentUser = currentUser;

        setTitle("Employee Full Details");
        setSize(550, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 1. Main Content Panel
        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        fields = new JTextField[labels.length];

        // 2. Loop through labels and fill with data
        for (int i = 0; i < labels.length; i++) {
            mainPanel.add(new JLabel(labels[i] + ":"));
            String value = (data != null && i < data.length && data[i] != null) ? data[i].toString() : "";
            
            fields[i] = new JTextField(value);
            fields[i].setEditable(false); // Default to read-only view
            
            // Visual style for locked fields (ID and calculated rates)
            if (i == 0 || i >= 17) {
                fields[i].setBackground(new Color(230, 230, 230));
            }
            mainPanel.add(fields[i]);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Footer with logic buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addActionButtons(footer); 
        
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);
        
        add(footer, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void addActionButtons(JPanel buttonPanel) {
        btnEdit = new JButton("Edit Employee");
        btnSave = new JButton("Save Changes");
        btnSave.setVisible(false); // Hidden until Edit is clicked

        btnEdit.addActionListener(e -> {
            setFieldsEditable(true); // Unlock the text fields
            btnEdit.setVisible(false); // Hide Edit button
            btnSave.setVisible(true);  // Show Save button
        });

        btnSave.addActionListener(e -> {
            // 1. Call Service to handle validation and saving
            boolean success = service.updateEmployeeFromForm(currentUser, fields);
            
            if (success) {
                // 2. Show the requested Success Dialog
                JOptionPane.showMessageDialog(this, 
                    "Employee Information Changes Saved", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // 3. Update UI state (Lock fields)
                setFieldsEditable(false);
                btnSave.setVisible(false);
                btnEdit.setVisible(true);
            }
        });

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnSave);
    }

    // Helper to support your toggle logic
    private void setFieldsEditable(boolean active) {
        for (int i = 0; i < fields.length; i++) {
            // Only unlock fields that aren't ID or Calculated Rates
            if (i > 0 && i < 17) {
                fields[i].setEditable(active);
                fields[i].setBackground(active ? Color.WHITE : Color.WHITE);
            }
        }
    }
}