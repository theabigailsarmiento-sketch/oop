package service;

import dao.EmployeeDAO;
import java.util.List;   // Connects the Admin Interface
import model.Employee;            // Connects the HR Model
import model.HRStaff;           // Connects the Base Model
import model.IAdminOperations;         // Connects the DAO Interface

public class EmployeeManagementService {
    private final EmployeeDAO dao;

    public EmployeeManagementService(EmployeeDAO dao) {
        this.dao = dao;
    }

    /**
     * Requirement: Admin handles System Governance.
     * Logic: Verifies the 'actor' is an Admin via the Interface.
     */
    public boolean deleteEmployee(IAdminOperations actor, int empId) {
        // Business Rule: Protection for CEO/System Admin
        if (empId == 10001) {
            System.err.println("Logic Error: Cannot delete protected system account.");
            return false;
        }
        
        // 1. Model Logic: The Admin records the deletion intent
        actor.removeEmployee(empId); 
        
        // 2. DAO Logic: The CSVHandler removes the line from the file
        return dao.deleteEmployee(empId); 
    }

    public void updateDetails(HRStaff actor, int empId, Employee newData) {
        actor.updateEmployeeDetails(empId, newData);
        dao.update(newData);
    }

    public int getNextAvailableId() {
    // Business Rule: Find the highest ID and add 1
    java.util.List<Employee> allEmployees = dao.getAll();
    return allEmployees.stream()
            .mapToInt(Employee::getEmpNo)
            .max()
            .orElse(10000) + 1; 
}

// Around line 58
public boolean registerNewEmployee(IAdminOperations actor, Employee newEmp) {
    return dao.addEmployee(newEmp);// Ensure this matches the DAO name 'add'
}

// Around line 63
public List<Employee> getAllEmployees() {
    return dao.getAll(); // Ensure this matches the DAO name
}
}