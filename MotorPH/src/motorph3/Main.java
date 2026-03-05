package motorph3;

import dao.AttendanceCSVHandler;
import dao.AttendanceDAO;
import dao.CSVHandler;
import dao.EmployeeDAO; 
import dao.UserLibrary;
import ui.LoginPanel;

public class Main {
    public static void main(String[] args) {
        
        EmployeeDAO employeeDao = new CSVHandler();
        AttendanceDAO attendanceDao = new AttendanceCSVHandler(); 
        
        UserLibrary auth = new UserLibrary(employeeDao);

        java.awt.EventQueue.invokeLater(() -> {
            
            new LoginPanel(employeeDao, attendanceDao, auth).setVisible(true);
        });
    }
}