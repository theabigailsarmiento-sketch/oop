package dao;

import model.Employee;
import model.Role;

/**
 * UserLibrary: The Session Manager
 * Holds the current logged-in user and handles the "Login" logic.
 */
public class UserLibrary {
    private final EmployeeDAO employeeDAO;
    
    // Static so the entire app can access the current session
    private static Role userRole;
    private static Employee loggedInEmployee;

    public UserLibrary(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public boolean authenticate(String username, String password) {
        // DAO does the work of searching the CSV/Cache
        Employee emp = employeeDAO.findByUsername(username);

        if (emp != null) {
            // Using the password field we populated in the CSVHandler
            String storedPass = emp.getPassword(); 
            
            if (storedPass != null && storedPass.equals(password)) {
                // Store in static variables for global access
                userRole = emp.getRole(); 
                loggedInEmployee = emp; 
                
                System.out.println("Session Started: " + emp.getFirstName() + " as " + userRole);
                return true;
            } else {
                System.out.println("Login Denied: Invalid Password.");
            }
        } else {
            System.out.println("Login Denied: User not found.");
        }
        return false;
    }

    // --- SESSION MANAGEMENT ---

    public static void logout() {
        userRole = null;
        loggedInEmployee = null;
        System.out.println("Session Ended.");
    }

    public static Role getUserRole() {
        return userRole;
    }

    public static Employee getLoggedInEmployee() {
        return loggedInEmployee;
    }
    
    public static boolean isLoggedIn() {
        return loggedInEmployee != null;
    }
    // Inside UserLibrary.java
public static void loginUser(Employee emp) {
    loggedInEmployee = emp;
    userRole = emp.getRole();
}
}