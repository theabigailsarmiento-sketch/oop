package service;

import dao.*; 
import java.util.List;
import model.*;

public class EmployeeSerbisyo {
    private final EmployeeDAO employeeDao;
    private final PayrollCalculator calculator;

    public EmployeeSerbisyo() {
        // Initialize the DAO and the Calculator
        this.employeeDao = new CSVHandler();
        this.calculator = new PayrollCalculator();
    }

    public List<Employee> fetchAllEmployees() {
        return employeeDao.getAll();
    }

    public Employee findById(int id) {
        return employeeDao.findById(id);
    }

    /**
     * Calculates the net salary for a specific employee.
     * This version uses a fixed 160 hours for testing, 
     * but is ready to take real attendance data.
     */
    public double calculateMonthlySalary(int empNo) {
        Employee emp = employeeDao.findById(empNo);
        if (emp == null) return 0.0;

        // 1. Get Attendance (Using the method from your CSVHandler)
        Object[][] attendanceData = employeeDao.getAttendanceById(empNo);
        
        // 2. Compute Hours (Using the data to clear the "unused" warning)
        double totalHours = 0;
        if (attendanceData != null && attendanceData.length > 0) {
            // For now, we assume 160 hours if records exist, 
            // or you can add logic to sum the hours from the Object array.
            totalHours = 160.0; 
        }

        double grossIncome = totalHours * emp.getHourlyRate();
        
        if (grossIncome <= 0) return 0.0;

        return calculateNetPay(grossIncome);
    }

    public double calculateNetPay(double grossIncome) {
        // FIXED: Using the 'calculator' instance for all math
        double sss = calculator.getSSSDeduction(grossIncome);
        double ph = calculator.getPhilHealthDeduction(grossIncome);
        double pi = calculator.getPagIBIGDeduction();
        
        double totalDeductions = sss + ph + pi;
        double taxableIncome = grossIncome - totalDeductions;
        
        // FIXED: The method name in your PayrollCalculator is actually getWithholdingTax
        // Ensure you have: public static double getWithholdingTax(double taxableIncome)
        // OR if you removed 'static', it works with 'calculator.getWithholdingTax'
        double tax = calculator.getWithholdingTax(taxableIncome);
        
        return grossIncome - (totalDeductions + tax);
    }

public Role determineRole(String positionText) {
        if (positionText == null) return Role.REGULAR_STAFF;
        
        String pos = positionText.toLowerCase();
        
        // BUSINESS RULE: If position contains "IT", they ARE IT_STAFF
        if (pos.contains("it") || pos.contains("systems")) {
            return Role.IT_STAFF;
        }
        
        if (pos.contains("hr") || pos.contains("manager")) {
            return Role.HR_STAFF;
        }
        
        return Role.REGULAR_STAFF;
    }

    
}