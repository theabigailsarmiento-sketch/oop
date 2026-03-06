package service;

import dao.EmployeeDAO;
import java.util.List;
import javax.swing.JOptionPane;
import model.Employee;
import model.IAdminOperations;

public class EmployeeManagementService {
    private final EmployeeDAO employeeDao;

    public EmployeeManagementService(EmployeeDAO employeeDao) {
        this.employeeDao = employeeDao;
    }

    /**
     * Business Rule: Validate and Register a new hire.
     * This protects the DAO from malformed data.
     */
 
public boolean registerEmployee(IAdminOperations actor, Employee emp) {
    if (actor == null || emp == null) return false;

    // --- BUSINESS RULE: VALIDATION ---
    if (emp.getFirstName().isEmpty() || emp.getLastName().isEmpty()) {
        JOptionPane.showMessageDialog(null, "First and Last names are required!");
        return false;
    }

    // Validate SSS (Format: 00-0000000-0)
    if (!emp.getSss().matches("^\\d{2}-\\d{7}-\\d{1}$")) {
        JOptionPane.showMessageDialog(null, "Invalid SSS format! Use: 00-0000000-0");
        return false;
    }

    // Validate Phone (Format: 000-000-000)
    if (!emp.getPhone().matches("^\\d{3}-\\d{3}-\\d{3}$")) {
        JOptionPane.showMessageDialog(null, "Invalid Phone format! Use: 000-000-000");
        return false;
    }

    // --- BUSINESS RULE: AUTOMATION ---
    // 1. Automatically get the next ID so the UI doesn't have to
    emp.setEmpNo(employeeDao.getNextAvailableId());

    // 2. Automatically calculate hourly rate (Standard: 21 days, 8 hours)
    double hourly = emp.getBasicSalary() / 21 / 8;
    emp.setHourlyRate(hourly);

    // 3. NEW: Automatically calculate Gross Semi-monthly Rate
    // Formula: Basic Salary + Rice + Phone + Clothing
    double totalGross = emp.getBasicSalary() + 
                       emp.getRiceSubsidy() + 
                       emp.getPhoneAllowance() + 
                       emp.getClothingAllowance();
    emp.setGrossRate(totalGross);

    // --- FINAL STEP: CALL DAO ---
    // Now the DAO receives a "Complete" employee object with all financial math done
    return employeeDao.addEmployee(emp);
}
    /**
     * Private helper for Business Rules/Validation
     */
    private boolean validateEmployeeFormats(Employee emp) {
        // SSS Format: 00-0000000-0
        if (!emp.getSss().matches("^\\d{2}-\\d{7}-\\d{1}$")) {
            showError("Invalid SSS format! Expected: 00-0000000-0");
            return false;
        }
        // Phone Format: 000-000-000
        if (!emp.getPhone().matches("^\\d{3}-\\d{3}-\\d{3}$")) {
            showError("Invalid Phone format! Expected: 000-000-000");
            return false;
        }
        // TIN Format: 000-000-000-000
        if (!emp.getTin().matches("^\\d{3}-\\d{3}-\\d{3}-\\d{3}$")) {
            showError("Invalid TIN format! Expected: 000-000-000-000");
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public int getNextAvailableId() {
        return employeeDao.getNextAvailableId();
    }

    public boolean removeEmployee(IAdminOperations actor, int id) {
        if (id == 10001) return false; // Rule: Protect super-admin
        return employeeDao.deleteEmployee(id);
    }

    public List<Employee> getAll() { 
        return employeeDao.getAll();
    }



    // Inside your Service (e.g., EmployeeService.java)
public Object[] getEmployeeDetailsForForm(int empId) {
    Employee emp = employeeDao.findById(empId);
    if (emp == null) return null;

    // 1. Get the base salary from the model
    double baseSalary = emp.getBasicSalary();
    
    // 2. LOGIC: If you need to adjust based on attendance:
    // double workedDays = attendanceService.getWorkedDays(empId);
    // double attendanceAdjustedSalary = (baseSalary / 21) * workedDays; 

    return new Object[] {
        emp.getEmpNo(), emp.getLastName(), emp.getFirstName(), 
        emp.getBirthday(), emp.getAddress(), emp.getPhone(),
        emp.getSss(), emp.getPhilhealth(), emp.getTin(), emp.getPagibig(),
        emp.getStatus(), emp.getPosition(), emp.getSupervisor(),
        baseSalary,               // Show the salary here
        emp.getRiceSubsidy(), 
        emp.getPhoneAllowance(), 
        emp.getClothingAllowance(),
        emp.getGrossRate(), 
        emp.getHourlyRate(), 
        emp.getRole()
    };
}
}