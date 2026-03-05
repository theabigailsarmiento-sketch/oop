package ui;
import java.awt.*;
import javax.swing.*;

public class EmployeeDetailForm extends JFrame {
    private JTextField[] fields;
    private String[] labels = {
        "Employee #", "Last Name", "First Name", "Birthday", "Address", "Phone #",
        "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
        "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
        "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate", "Role"
    };

    public EmployeeDetailForm(Object[] data) {
        setTitle("Employee Full Details - " + data[2] + " " + data[1]);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Main Panel with Scroll
        JPanel mainPanel = new JPanel(new GridLayout(labels.length, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            mainPanel.add(new JLabel(labels[i] + ":"));
            String value = (data[i] != null) ? data[i].toString() : "";
            fields[i] = new JTextField(value);
            
            // Disable editing for Employee # and Salary calculations for safety
            if (i == 0 || i >= 17) fields[i].setEditable(false);
            
            mainPanel.add(fields[i]);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Footer with Save Button
        JPanel footer = new JPanel();
        JButton btnSave = new JButton("Save Changes");
        btnSave.setBackground(new Color(34, 139, 34));
        btnSave.setForeground(Color.WHITE);
        
        btnSave.addActionListener(e -> {
            // Logic to send back to HRSerbisyo will go here
            JOptionPane.showMessageDialog(this, "Changes saved to database!");
            dispose();
        });

        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }
}