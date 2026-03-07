package motorph3;

import dao.AttendanceCSVHandler;
import dao.AttendanceDAO;
import dao.CSVHandler;
import dao.EmployeeDAO; 
import dao.UserLibrary;
import service.EmployeeManagementService;
import ui.LoginPanel;

public class Main {
    public static void main(String[] args) {
        // 1. DAOs
        EmployeeDAO employeeDao = new CSVHandler();
        AttendanceDAO attendanceDao = new AttendanceCSVHandler(); 
        
        // 2. Service
        EmployeeManagementService employeeService = new EmployeeManagementService(employeeDao);
        
        // 3. Auth
        // Note: Even though we create an instance to pass the DAO, 
        // inside the UI we should use UserLibrary.getLoggedInEmployee()
        UserLibrary auth = new UserLibrary(employeeDao);

        java.awt.EventQueue.invokeLater(() -> {
            // This call is fine because it passes the instance to the constructor
            new LoginPanel(employeeService, attendanceDao, auth).setVisible(true);
        });
    }
}