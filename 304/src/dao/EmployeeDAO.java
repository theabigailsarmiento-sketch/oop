package dao;

import java.util.List;
import model.Employee;
import model.LeaveRequest;

public interface EmployeeDAO {
    // Basic Employee Operations
    Employee findById(int empNo);
    Employee findByUsername(String username);
    List<Employee> getAll();
    boolean addEmployee(Employee newEmp);
    
    // CHANGED: From void to boolean to match your CSVHandler logic
    boolean update(Employee emp);
    
    boolean deleteEmployee(int id);

    // Attendance Operations
    Object[][] getAttendanceByMonth(int empId, String month);
    Object[][] getAttendanceById(int empId);
    void recordAttendance(int empId, String type); // type: "IN" or "OUT"

    // Leave Operations
    void applyForLeave(int empId, String type, String startDate, String endDate, String reason);
    Object[][] getLeaveStatusByEmpId(int empId);
    Object[][] getAllLeaveRequests(); 
    void updateLeaveStatus(int empId, String startDate, String newStatus);
    
    // Service-Layer specific list
    List<LeaveRequest> getAllLeaveRequestsList();

    // Add these two lines to fix the "cannot find symbol" error
    boolean updateEmployee(Employee e); 
    
    void updateEmployeeStatus(int empNo, String newStatus);
    void saveNewPassword(int empNo, String newPassword);

    
}