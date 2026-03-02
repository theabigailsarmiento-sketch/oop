package service;

import dao.EmployeeDAO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import model.Employee;

/**
 * Service Layer: Handles business rules, validation, and data formatting.
 * Protects the DAO and prepares data for the UI.
 */
public class LeaveStuff {
    private final EmployeeDAO dao; 
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public LeaveStuff(EmployeeDAO dao) {
        this.dao = dao;
    }

    /**
     * Business Rule: Validates dates and reason before passing to DAO.
     */
    public void submitLeave(int empNo, String type, LocalDate start, LocalDate end, String reason) {
        // 1. Validation Logic
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Error: End date cannot be before start date.");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            reason = "No reason provided";
        }

        // 2. Data Preparation
        String startStr = start.format(formatter);
        String endStr = end.format(formatter);
        
        // 3. DAO Call
        dao.applyForLeave(empNo, type, startStr, endStr, reason);
    }

    /**
     * Business Rule: Maps raw CSV data (8 columns) to UI Table format (6 columns).
     * Fixes the alignment so 'Status' appears in the correct column.
     */
    public Object[][] getLeaveHistory(int empNo) {
        // 1. Fetch raw data from DAO (Streamed from CSV)
        Object[][] rawData = dao.getLeaveStatusByEmpId(empNo);
        
        if (rawData == null || rawData.length == 0) {
            return new Object[0][6]; 
        }
        
        // 2. Fetch employee model for names
        Employee emp = dao.findById(empNo);
        String lastName = (emp != null) ? emp.getLastName() : "Unknown";
        String firstName = (emp != null) ? emp.getFirstName() : "Unknown";

        // 3. Re-map indices for the UI
        // UI expects: [LastName, FirstName, Start, End, Reason, Status]
        Object[][] formattedData = new Object[rawData.length][6];

        for (int i = 0; i < rawData.length; i++) {
            formattedData[i][0] = lastName;
            formattedData[i][1] = firstName;
            
            // rawData indices based on your CSV structure:
            // [0]Emp#, [1]LName, [2]FName, [3]Type, [4]Start, [5]End, [6]Reason, [7]Status
            formattedData[i][2] = rawData[i][4]; // Start Date
            formattedData[i][3] = rawData[i][5]; // End Date
            formattedData[i][4] = rawData[i][6]; // Reason
            formattedData[i][5] = rawData[i][7]; // Status
        }
        
        return formattedData;
    }

    public void updateStatus(int empId, String startDate, String newStatus) {
        dao.updateLeaveStatus(empId, startDate, newStatus);
    }

    public Object[][] getAllPendingLeaves() {
        return dao.getAllLeaveRequests(); 
    }

    public Object[][] getEmployeeAttendance(int empId) {
        return dao.getAttendanceById(empId);
    }

    public boolean updateEmployeeProfile(Employee emp) {
        if (emp == null || emp.getEmpNo() <= 0) return false;
        try {
            return dao.update(emp); 
        } catch (Exception e) {
            System.err.println("Service Error: Could not update profile. " + e.getMessage());
            return false;
        }
    }

    public Employee getEmployeeSalaryInfo(int empId) {
        if (empId <= 0) return null;
        return dao.findById(empId); 
    }
}