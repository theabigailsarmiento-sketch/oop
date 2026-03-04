package service;

import dao.EmployeeDAO;
import model.Employee;
import model.HRStaff;
import model.LeaveRequest;
import java.time.LocalDate;
import java.util.List;

public class HRSerbisyo {
    private final EmployeeDAO dao;

    public HRSerbisyo(EmployeeDAO dao) {
        this.dao = dao;
    }

    // --- YOUR ORIGINAL METHODS (PRESERVED) ---

    public boolean registerNewEmployee(Employee e) {
        if (e.getEmpNo() <= 0 || e.getLastName().isEmpty()) {
            return false; 
        }
        return dao.addEmployee(e); 
    }

    public boolean updateEmployeeRecord(Employee e) {
        return dao.updateEmployee(e);
    }

    public boolean removeEmployee(int id) {
        if (id == 10001) { // CEO Protection Rule
            System.out.println("Error: System Admin cannot be deleted.");
            return false;
        }
        return dao.deleteEmployee(id);
    }

    public Employee getEmployeeById(int id) {
        return dao.findById(id); 
    }

    public Object[][] getAllLeaveRequestsForTable() {
        return dao.getAllLeaveRequests(); 
    }

    public int getPendingLeaveCount() {
        Object[][] allLeaves = dao.getAllLeaveRequests();
        int count = 0;
        for (Object[] row : allLeaves) {
            if (row != null && row.length >= 8 && "Pending".equalsIgnoreCase(row[7].toString())) {
                count++;
            }
        }
        return count;
    }

    public boolean updateEmployeeProfile(Employee emp) {
        if (emp.getPhone() == null || emp.getPhone().trim().isEmpty()) {
            return false; 
        }
        dao.update(emp);
        return true;
    }

    // --- NEW ADDITIONS FROM METHOD DICTIONARY ---

    /**
     * Justification: Bridges the HRStaff model's intent to the DAO's file stream.
     * Logic: Validates existence in CSV before allowing the update.
     */
    public boolean hrUpdateEmployeeDetails(HRStaff actor, int targetID, Employee newData) {
        if (dao.findById(targetID) == null) {
            System.err.println("Error: empID " + targetID + " does not exist in CSV.");
            return false;
        }
        actor.updateEmployeeDetails(targetID, newData); // Model Logic
        return dao.update(newData); // DAO Streaming
    }

    /**
     * Justification: Handles specific Leave Approval workflow.
     * Logic: Updates status to Approved in the CSV.
     */
    public void approveLeave(HRStaff actor, String leaveID, int empID, String startDate) {
        actor.approveLeaveRequest(leaveID);
        dao.updateLeaveStatus(empID, startDate, "Approved");
    }

    /**
     * Justification: Handles rejection logic with a reason record.
     */
    public void rejectLeave(HRStaff actor, String leaveID, int empID, String startDate, String reason) {
        actor.rejectLeaveRequest(leaveID, reason);
        dao.updateLeaveStatus(empID, startDate, "Rejected");
    }

    /**
     * Justification: Required to fulfill the "viewAllLeaveRequests" dictionary entry.
     */
    public List<LeaveRequest> fetchAllLeavesAsList() {
        // Business Rule: Service converts DAO data into Model List for HR review
        return dao.getAllLeaveRequestsList(); 
    }

    // Fixed version of your updateLeaveStatus to ensure it uses proper error handling
    public boolean updateLeaveStatus(int empId, String startDate, String status) {
        try {
            dao.updateLeaveStatus(empId, startDate, status);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    
}