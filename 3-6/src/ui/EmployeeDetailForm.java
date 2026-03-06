package ui;

import java.awt.*;
import javax.swing.*;

public class EmployeeDetailForm extends JFrame {
    private JTextField[] fields;
    private final String[] labels = {
        "Employee #", "Last Name", "First Name", "Birthday", "Address", "Phone #",
        "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
        "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
        "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate", "Role"
    };

    public EmployeeDetailForm(Object[] data) {
        setTitle("Employee Full Details");
        setSize(550, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // 1. Define the mainPanel here
        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        fields = new JTextField[labels.length];

        // 2. Loop through labels and fill with data from Service
        for (int i = 0; i < labels.length; i++) {
            mainPanel.add(new JLabel(labels[i] + ":"));
            
            // Safe data check to prevent NullPointerException
            String value = (data != null && i < data.length && data[i] != null) ? data[i].toString() : "0.0";
            
            fields[i] = new JTextField(value);
            
            // Business Rule: Lock sensitive fields
            if (i == 0 || i >= 17) {
                fields[i].setEditable(false);
                fields[i].setBackground(new Color(245, 245, 245));
            }
            mainPanel.add(fields[i]);
        }

        // 3. Add the panel to a ScrollPane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        // 4. Footer
        JPanel footer = new JPanel();
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }
}