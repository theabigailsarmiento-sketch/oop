package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import java.util.List;
import model.Attendance;
import model.Deduction;
import model.Employee;

public class PayrollService {
    private final EmployeeDAO employeeDao;
    private final AttendanceDAO attendanceDao;
    private final PayrollCalculator calculator;

    public PayrollService(EmployeeDAO eDao, AttendanceDAO aDao) {
        this.employeeDao = eDao;
        this.attendanceDao = aDao;
        this.calculator = new PayrollCalculator();
    }

    // The UI calls THIS method
    public double getFinalGross(int empNo) {
        Employee emp = employeeDao.findById(empNo);
        // Delegate math to the calculator
        return calculator.calculateGross(emp); 
    }

    /**
     * MASTER SERVICE METHOD: 
     * This coordinates everything for the UI.
     */
  // Inside PayrollService.java
// Inside PayrollService.java
// Version A: The UI calls this one (Passes the Employee object)
// Inside PayrollService.java
public Deduction calculateMonthlyPayroll(Employee emp, String month) {
    if (emp == null) return new Deduction();
    // Rule: Always call the ID-based version to ensure fresh data from DAO
    return calculateMonthlyPayroll(emp.getEmpNo(), month);
}

public Deduction calculateMonthlyPayroll(int empNo, String month) {
    Employee emp = employeeDao.findById(empNo); 
    if (emp == null) return new Deduction();

    // Fix: Declare the 'salary' variable here
    double salary = emp.getBasicSalary(); 

    Deduction d = new Deduction();
    
    // Business Logic: Use the calculator with the now-defined salary
    d.setSss(calculator.getSSSDeduction(salary));
    d.setPhilHealth(calculator.getPhilHealthDeduction(salary));
    d.setPagIbig(calculator.getPagIBIGDeduction(salary));
    
    // Calculate Taxable Income
    double taxableIncome = salary - (d.getSss() + d.getPhilHealth() + d.getPagIbig());
    d.setTax(calculator.getWithholdingTax(taxableIncome));
    
    // Populate Totals
    double allowances = calculator.calculateTotalAllowances(emp);
    d.setGrossPay(salary + allowances); // Rules: Basic + Allowances
    d.setNetPay(d.getGrossPay() - d.getTotal());
    
    return d;
}
    
    public double calculateNetSalary(int empNo, String month) {
        // Fix: Use employeeDao.findById to match your DAO method name
        Employee emp = employeeDao.findById(empNo);
        if (emp == null) return 0.0;

        List<Attendance> logs = attendanceDao.getAttendanceByEmployee(empNo);
        
        // 1. Calculate hours and late minutes for the specific month
        double totalHours = calculateHoursFromLogs(logs, month);
        int lateMinutes = calculateLateMinutes(logs, month);
        
        // 2. Call the new master method in PayrollCalculator
        return calculator.calculateFullNetPay(emp, totalHours, lateMinutes);
    }

    /**
     * Helper to sum up hours.
     */
    private double calculateHoursFromLogs(List<Attendance> logs, String month) {
        double total = 0;
        for (Attendance log : logs) {
            String logMonth = log.getDate().getMonth().name();
            if (month.equalsIgnoreCase("All") || logMonth.equalsIgnoreCase(month)) {
                total += log.getHoursWorked(); 
            }
        }
        return total;
    }

    /**
     * Helper to sum up late minutes (using 8:10 AM grace period).
     */
    private int calculateLateMinutes(List<Attendance> logs, String month) {
        int totalLate = 0;
        java.time.LocalTime gracePeriod = java.time.LocalTime.of(8, 10);
        for (Attendance log : logs) {
            String logMonth = log.getDate().getMonth().name();
            if ((month.equalsIgnoreCase("All") || logMonth.equalsIgnoreCase(month)) 
                 && log.getTimeIn().isAfter(gracePeriod)) {
                totalLate += (int) java.time.Duration.between(gracePeriod, log.getTimeIn()).toMinutes();
            }
        }
        return totalLate;
    }

    // These methods delegate to the Calculator to keep logic centralized
    public double calculateGrossSalary(Employee emp, double hoursWorked) {
        return calculator.calculateGrossIncome(emp, hoursWorked);
    }

   // In PayrollService.java
public double calculateGrossPay(Employee emp) {
    // Business Rule: Gross = Basic + All Allowances
    return emp.getBasicSalary() + 
           emp.getRiceSubsidy() + 
           emp.getPhoneAllowance() + 
           emp.getClothingAllowance();
}

    
}