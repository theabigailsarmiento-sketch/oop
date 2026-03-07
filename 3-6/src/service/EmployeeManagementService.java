package service;

import dao.EmployeeDAO;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import model.Employee;
import model.IAdminOperations; // Added this
import model.RegularStaff;

public class EmployeeManagementService {
    private final EmployeeDAO employeeDao;

    public EmployeeManagementService(EmployeeDAO employeeDao) {
        this.employeeDao = employeeDao; 
    }

    /**
     * N-TIER RULE: The UI calls this with raw data. 
     * The Service creates the Model and performs calculations.
     */
    public boolean processNewHire(Employee actor, String fName, String lName, String sss, double salary) {
        // 1. Permission Check
        if (!(actor instanceof IAdminOperations)) {
            showError("Access Denied: Only Admins can register employees.");
            return false;
        }

        // 2. Instantiate the Model (UI doesn't do this anymore)
        Employee newEmp = new RegularStaff();
        newEmp.setFirstName(fName);
        newEmp.setLastName(lName);
        newEmp.setSss(sss);
        newEmp.setBasicSalary(salary);
        
        // 3. Set Defaults (Rice, Phone, Clothing usually fixed per company policy)
        newEmp.setRiceSubsidy(1500);
        newEmp.setPhoneAllowance(500);
        newEmp.setClothingAllowance(1000);

        // 4. Pass to the existing registration logic for validation and math
        return registerEmployee((IAdminOperations)actor, newEmp);
    }

    public boolean registerEmployee(IAdminOperations actor, Employee emp) {
        if (actor == null || emp == null) return false;

        // Validation Logic
        if (emp.getFirstName().isEmpty() || emp.getLastName().isEmpty()) {
            showError("First and Last names are required!");
            return false;
        }

        // Calculation Logic
        emp.setEmpNo(employeeDao.getNextAvailableId());
        double hourly = emp.getBasicSalary() / 21 / 8;
        emp.setHourlyRate(hourly);

        double totalGross = emp.getBasicSalary() + emp.getRiceSubsidy() + 
                           emp.getPhoneAllowance() + emp.getClothingAllowance();
        emp.setGrossRate(totalGross);

        return employeeDao.addEmployee(emp);
    }

    public List<Employee> getAll() { 
        return employeeDao.getAll();
    }

    public boolean deleteEmployee(Employee actor, int id) {
        if (!(actor instanceof IAdminOperations)) return false;
        if (id == 10001) return false; 
        return employeeDao.deleteEmployee(id);
    }

    // Add this inside EmployeeManagementService.java
public Employee findById(int id) {
    // Business Rule: We simply pass the request to the DAO to fetch the model
    return employeeDao.findById(id);
}

    public Object[] getEmployeeDetailsForForm(int empId) {
        Employee emp = employeeDao.findById(empId);
        if (emp == null) return null;
        return new Object[] {
            emp.getEmpNo(), emp.getLastName(), emp.getFirstName(), 
            emp.getBirthday(), emp.getAddress(), emp.getPhone(),
            emp.getSss(), emp.getPhilhealth(), emp.getTin(), emp.getPagibig(),
            emp.getStatus(), emp.getPosition(), emp.getSupervisor(),
            emp.getBasicSalary(), emp.getRiceSubsidy(), 
            emp.getPhoneAllowance(), emp.getClothingAllowance(),
            emp.getGrossRate(), emp.getHourlyRate(), emp.getRole()
        };
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }


    public int generateNextEmployeeId() {
    // This calls the method we defined in your EmployeeDAO interface
    return employeeDao.getNextAvailableId();
}


// Inside EmployeeManagementService.java
// Inside EmployeeManagementService.java
public boolean updateEmployeeDetails(Employee actor, Employee updatedData) {
    // 1. Validation/Permission check
    if (!(actor instanceof IAdminOperations)) return false;

    // 2. Business Logic: Recalculate Rates
    double hourly = updatedData.getBasicSalary() / 21 / 8;
    updatedData.setHourlyRate(hourly);

    double totalGross = updatedData.getBasicSalary() + updatedData.getRiceSubsidy() + 
                       updatedData.getPhoneAllowance() + updatedData.getClothingAllowance();
    updatedData.setGrossRate(totalGross);

    // 3. Call DAO to update cache and save to CSV
    return employeeDao.update(updatedData);
}



// Inside EmployeeManagementService.java

/**
 * This method satisfies the UI's call to 'removeEmployee'
 * by redirecting it to your existing 'deleteEmployee' logic.
 */
public boolean removeEmployee(IAdminOperations actor, int id) {
    // We cast the IAdminOperations back to Employee to reuse your existing method
    return deleteEmployee((Employee) actor, id);
}

public boolean updateEmployeeFromForm(Employee actor, JTextField[] fields) {
    try {
        // 1. Validation Logic
        if (fields[1].getText().trim().isEmpty() || fields[2].getText().trim().isEmpty()) {
            throw new Exception("Names cannot be empty.");
        }

        // 2. Map fields to a Model object
        Employee emp = new RegularStaff();
        emp.setEmpNo(Integer.parseInt(fields[0].getText()));
        emp.setLastName(fields[1].getText());
        emp.setFirstName(fields[2].getText());
        // ... map other fields ...
        emp.setBasicSalary(Double.parseDouble(fields[13].getText()));
        emp.setRiceSubsidy(Double.parseDouble(fields[14].getText()));
        emp.setPhoneAllowance(Double.parseDouble(fields[15].getText()));
        emp.setClothingAllowance(Double.parseDouble(fields[16].getText()));

        // 3. Business Logic: Recalculate Rates
        double hourly = emp.getBasicSalary() / 21 / 8;
        emp.setHourlyRate(hourly);
        
        double gross = emp.getBasicSalary() + emp.getRiceSubsidy() + 
                       emp.getPhoneAllowance() + emp.getClothingAllowance();
        emp.setGrossRate(gross);

        // 4. Pass to DAO
        return employeeDao.update(emp);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Update Failed: " + e.getMessage());
        return false;
    }
}



}