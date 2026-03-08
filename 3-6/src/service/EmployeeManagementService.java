package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField; // Added this
import model.Employee;
import model.IAdminOperations;
import model.RegularStaff;

public class EmployeeManagementService {
    private final EmployeeDAO employeeDao;
    private AttendanceDAO attendanceDao; // Add this

  public EmployeeManagementService(EmployeeDAO employeeDao, AttendanceDAO attendanceDao) {
        this.employeeDao = employeeDao;
        this.attendanceDao = attendanceDao;
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

    // --- ADDED GENDER BUSINESS RULE ---
    if (updatedData.getGender() == null || updatedData.getGender().trim().isEmpty()) {
        updatedData.setGender("Not");
    }

    // 2. Business Logic: Recalculate Rates
    double hourly = updatedData.getBasicSalary() / 21 / 8;
    updatedData.setHourlyRate(hourly);

    double totalGross = updatedData.getBasicSalary() + updatedData.getRiceSubsidy() + 
                       updatedData.getPhoneAllowance() + updatedData.getClothingAllowance();
    updatedData.setGrossRate(totalGross);

    // 3. Call DAO to update cache and save to CSV
    // Note: ensure your DAO method name is correct (either .update or .updateEmployee)
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

// Inside EmployeeManagementService.java
public boolean updateEmployeeFromForm(Employee actor, JTextField[] fields) {
    try {
        // 1. Permission & Name Validation
        if (!(actor instanceof model.IAdminOperations)) {
            JOptionPane.showMessageDialog(null, "Access Denied.");
            return false;
        }
        if (fields[1].getText().trim().isEmpty() || fields[2].getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Last Name and First Name are required.");
            return false;
        }

        // 2. Date Validation (SHIFTED TO INDEX 4 because Index 3 is now Gender)
        String bdayText = fields[4].getText().trim();
        if (bdayText.isEmpty() || bdayText.equals("MM/dd/yyyy")) {
            JOptionPane.showMessageDialog(null, "Please select or enter a valid Birthday (MM/dd/yyyy).");
            return false;
        }

        LocalDate birthday;
        try {
            DateTimeFormatter csvFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            if (bdayText.contains("-")) {
                birthday = LocalDate.parse(bdayText); // Handles yyyy-MM-dd
            } else {
                birthday = LocalDate.parse(bdayText, csvFormatter); // Handles MM/dd/yyyy
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Birthday format error. Please use MM/dd/yyyy.");
            return false;
        }

        // 3. Financial Validation (SHIFTED INDICES 14-17)
        long basic = (long) parseDouble(fields[14].getText());
        long rice = (long) parseDouble(fields[15].getText());
        long phone = (long) parseDouble(fields[16].getText());
        long cloth = (long) parseDouble(fields[17].getText());

        // Business Rule: Rice and Phone validation
        if (rice < 1000 || rice > 9999) {
            JOptionPane.showMessageDialog(null, "Rice Subsidy must be a 4-digit amount.");
            return false;
        }
        if (phone < 500 || phone > 5000) {
            JOptionPane.showMessageDialog(null, "Phone Allowance must be between 500 and 5,000.");
            return false;
        }

        // 4. Pag-ibig Validation (SHIFTED TO INDEX 10)
        String pagibigText = fields[10].getText().trim();
        if (pagibigText.length() != 12 || !pagibigText.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Pag-ibig number must be exactly 12 digits.");
            return false;
        }

        // 5. Map validated data to the Model (ALL INDICES SHIFTED BY +1 AFTER FIRST NAME)
        Employee emp = new model.RegularStaff();
        emp.setEmpNo(Integer.parseInt(fields[0].getText().trim()));
        emp.setLastName(fields[1].getText().trim());
        emp.setFirstName(fields[2].getText().trim());
        
        // --- NEW: Capture Gender from Index 3 ---
        emp.setGender(fields[3].getText().trim()); 
        
        emp.setBirthday(birthday);                         // Index 4
        emp.setAddress(fields[5].getText().trim());        // Index 5
        emp.setPhone(fields[6].getText().trim());          // Index 6
        emp.setSss(fields[7].getText().trim());            // Index 7
        emp.setPhilhealth(fields[8].getText().trim());     // Index 8
        emp.setTin(fields[9].getText().trim());            // Index 9
        emp.setPagibig(pagibigText);                       // Index 10
        emp.setStatus(fields[11].getText().trim());        // Index 11
        emp.setPosition(fields[12].getText().trim());      // Index 12
        emp.setSupervisor(fields[13].getText().trim());    // Index 13

        // 6. Final Calculations
        emp.setBasicSalary((double) basic);                // Index 14
        emp.setRiceSubsidy((double) rice);                 // Index 15
        emp.setPhoneAllowance((double) phone);             // Index 16
        emp.setClothingAllowance((double) cloth);          // Index 17
        
        emp.setGrossRate((double) (basic + rice + phone + cloth));
        emp.setHourlyRate((double) Math.round((double) basic / 21 / 8));

        // 7. Push clean data to DAO
        return employeeDao.update(emp);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "System Error: " + e.getMessage());
        return false;
    }
}


public boolean saveOrUpdateFromUI(Employee actor, JTextField[] uiFields, boolean isNewHire) {
    try {
        // 1. BUSINESS RULE: Check permissions through the Service
        if (!(actor instanceof IAdminOperations)) {
            JOptionPane.showMessageDialog(null, "Access Denied: You do not have permission to modify employee records.");
            return false;
        }

        // 2. ID VALIDATION (Numbers only, specific lengths)
        String sssRaw = uiFields[6].getText().replaceAll("[^\\d]", "");
        String philRaw = uiFields[7].getText().replaceAll("[^\\d]", "");
        String tinRaw = uiFields[8].getText().replaceAll("[^\\d]", "");
        String pagibigRaw = uiFields[9].getText().replaceAll("[^\\d]", "");

        // Business Rule: Validate required lengths
        if (pagibigRaw.length() != 12 || philRaw.length() != 12) {
            JOptionPane.showMessageDialog(null, "Pag-ibig and Philhealth must be exactly 12 digits.");
            return false;
        }

        // 3. FINANCIAL NORMALIZATION (Strip decimals to save as whole numbers)
        long basic = Long.parseLong(uiFields[13].getText().replaceAll("[^\\d]", ""));
        long rice = Long.parseLong(uiFields[14].getText().replaceAll("[^\\d]", ""));
        long phone = Long.parseLong(uiFields[15].getText().replaceAll("[^\\d]", ""));
        long cloth = Long.parseLong(uiFields[16].getText().replaceAll("[^\\d]", ""));

        // 4. MODEL MAPPING
        Employee emp = new RegularStaff();
        
        // Handle Employee Number
        emp.setEmpNo(isNewHire ? employeeDao.getNextAvailableId() : Integer.parseInt(uiFields[0].getText().trim()));
        
        // Basic Info
        emp.setLastName(uiFields[1].getText().trim());
        emp.setFirstName(uiFields[2].getText().trim());
        emp.setAddress(uiFields[4].getText().trim());
        emp.setPhone(uiFields[5].getText().trim());
        
        // Identification Strings (using UI formatted versions for SSS/TIN to keep dashes)
        emp.setSss(uiFields[6].getText()); 
        emp.setPhilhealth(philRaw); 
        emp.setTin(uiFields[8].getText());
        emp.setPagibig(pagibigRaw);

        // Employment Details
        emp.setStatus(uiFields[10].getText().trim());
        emp.setPosition(uiFields[11].getText().trim());
        emp.setSupervisor(uiFields[12].getText().trim());

        // Date Parsing
        String bdayText = uiFields[3].getText().trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        try {
            // Check if user left placeholder or empty
            if(bdayText.isEmpty() || bdayText.equals("MM/dd/yyyy")) {
                 JOptionPane.showMessageDialog(null, "Please enter or pick a valid Birthday.");
                 return false;
            }
            // Handles both DatePicker (/) and Standard ISO (-) formats
            emp.setBirthday(bdayText.contains("-") ? LocalDate.parse(bdayText) : LocalDate.parse(bdayText, formatter));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Birthday format error. Please use MM/dd/yyyy");
            return false;
        }

        // 5. CALCULATIONS (Protected logic before DAO)
        emp.setBasicSalary((double) basic);
        emp.setRiceSubsidy((double) rice);
        emp.setPhoneAllowance((double) phone);
        emp.setClothingAllowance((double) cloth);
        
        // Semi-monthly Gross calculation
        double totalGross = (double) (basic + rice + phone + cloth);
        emp.setGrossRate(totalGross);
        
        // Hourly rate calculation based on 21 days, 8 hours
        double hourly = (double) Math.round((double) basic / 21 / 8);
        emp.setHourlyRate(hourly);

        // 6. DAO CALL: Final persistence to CSV
        return isNewHire ? employeeDao.addEmployee(emp) : employeeDao.update(emp);

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Financial error: Please enter whole numbers in salary fields.");
        return false;
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "System Error: " + e.getMessage());
        return false;
    }
}

// Helper methods to keep code clean
private double validateDouble(String input, String fieldName) throws Exception {
    try {
        return Double.parseDouble(input.trim().replace(",", "")); //
    } catch (NumberFormatException e) {
        throw new Exception(fieldName + " must be a valid number."); //
    }
}









private String cleanNull(String input) {
    return (input == null || input.trim().equalsIgnoreCase("null")) ? "" : input.trim(); //
}

private double parseDouble(String input) {
    try {
        return Double.parseDouble(input.trim().replace(",", ""));
    } catch (Exception e) {
        return 0.0;
    }
}
private String parseDate(String input) {
    String date = cleanNull(input);
    return date.isEmpty() ? "01/01/1900" : date; // Default date if missing
}





public boolean[] getTodaysStatus(int empNo) {
    // Business Logic: Determine if user can Check-in or Check-out
    Object[][] todayData = attendanceDao.getAttendanceByMonth(empNo, 
                            LocalDate.now().getMonth().name(), 
                            String.valueOf(LocalDate.now().getYear()));
    
    boolean checkedIn = false;
    boolean checkedOut = false;
    String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy"));

    for (Object[] row : todayData) {
        if (row[0].equals(todayStr)) {
            if (!row[1].equals("N/A")) checkedIn = true;
            if (!row[2].equals("N/A")) checkedOut = true;
        }
    }
    // Returns [CanCheckIn, CanCheckOut]
    return new boolean[]{!checkedIn, (checkedIn && !checkedOut)};
}




// Keep ONLY this version in EmployeeManagementService.java
public void recordTimeLog(int empNo, String type) {
    // 1. Fetch names so the DAO doesn't have to guess
    Employee emp = employeeDao.findById(empNo);
    String lName = (emp != null) ? emp.getLastName() : "Unknown";
    String fName = (emp != null) ? emp.getFirstName() : "Unknown";

    // 2. Standardize: convert "Check In" or "In" to "Check-in"
    String action = type.toLowerCase().contains("in") ? "Check-in" : "Check-out";

    // 3. Call the DAO that actually handles the File I/O
    attendanceDao.recordAttendance(empNo, lName, fName, action);
}





public Object[] getEmployeeDetailsForForm(int empId) {
    Employee emp = employeeDao.findById(empId);
    if (emp == null) return null;

    // Business Rule: Format financial data as whole numbers and include Gender
    return new Object[] {
        emp.getEmpNo(),               // 0
        emp.getLastName(),            // 1
        emp.getFirstName(),           // 2
        emp.getBirthday(),            // 3
        emp.getAddress(),             // 4
        emp.getPhone(),               // 5
        emp.getSss(),                 // 6
        emp.getPhilhealth(),          // 7
        emp.getTin(),                 // 8
        emp.getPagibig(),             // 9
        emp.getStatus(),              // 10
        emp.getPosition(),            // 11
        emp.getSupervisor(),          // 12
        formatWholeNumber(emp.getBasicSalary()),      // 13
        formatWholeNumber(emp.getRiceSubsidy()),      // 14
        formatWholeNumber(emp.getPhoneAllowance()),   // 15
        formatWholeNumber(emp.getClothingAllowance()),// 16
        formatWholeNumber(emp.getGrossRate()),        // 17
        formatWholeNumber(emp.getHourlyRate()),       // 18
        emp.getRole(),                                // 19
        emp.getGender()                               // 20 - THE FIX IS HERE
    };
}

/**
 * Helper to ensure the UI receives a clean string without ".0"
 */
private String formatWholeNumber(double value) {
    return String.format("%.0f", value);
}





// Inside EmployeeManagementService.java
public String[] getSupervisorsForPosition(String position) {
    if (position == null) return new String[]{"N/A"};

    switch (position) {
        case "Chief Operating Officer":
        case "Chief Finance Officer":
        case "Chief Marketing Officer":
        case "Account Manager":
            return new String[]{"Garcia, Manuel III"};

        case "IT Operations and Systems":
        case "HR Manager":
            return new String[]{"Lim, Antonio"};

        case "HR Team Leader":
            return new String[]{"Villanueva, Andrea Mae"};

        case "HR Rank and File":
            return new String[]{"San Jose, Brad"};

        case "Accounting Head":
            return new String[]{"Aquino, Bianca Sofia"};

        case "Payroll Manager":
            return new String[]{"Alvaro, Roderick"};

        case "Payroll Team Leader":
        case "Payroll Rank and File":
            return new String[]{"Salcedo, Anthony"};

        case "Account Team Leader":
            return new String[]{"Romualdez, Fredrick"};

        case "Account Rank and File":
            return new String[]{"Mata, Christian", "De Leon, Selena"};

        // Mapping for the specific positions you requested:
        case "Sales & Marketing":
        case "Supply Chain and Logistics":
        case "Customer Service and Relations":
            return new String[]{"Reyes, Isabella"};

        case "Chief Executive Officer":
            return new String[]{"N/A"};

        default:
            return new String[]{"N/A"};
    }
}


// Inside EmployeeManagementService.java




// Inside EmployeeManagementService.java

// Inside EmployeeManagementService.java

public Object[][] getAttendanceLogs(int empNo, String month, String year) {
    // Pass all three arguments to the DAO as required by your CSVHandler
    return attendanceDao.getAttendanceByMonth(empNo, month, year);
}

// Inside EmployeeManagementService.java

/**
 * FIX 3: Improved Button State Logic
 * This determines if the Check-In or Check-Out buttons should be greyed out.
 */

public boolean[] getButtonStates(int empNo) {
    String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    // Get ALL logs to avoid filter issues
    Object[][] data = attendanceDao.getAttendanceByMonth(empNo, "All", "All");

    boolean hasIn = false;
    boolean hasOut = false;

    if (data != null) {
        for (Object[] row : data) {
            String csvDate = row[0].toString().trim();
            if (csvDate.equals(todayStr)) {
                // If it's today, check the In/Out columns
                String timeIn = row[1].toString();
                String timeOut = row[2].toString();

                if (!timeIn.equalsIgnoreCase("N/A") && !timeIn.isEmpty()) {
                    hasIn = true; 
                }
                if (!timeOut.equalsIgnoreCase("N/A") && !timeOut.isEmpty() && !timeOut.equals("00:00")) {
                    hasOut = true;
                }
            }
        }
    }
    // Debug result
    System.out.println("Logic Check -> HasIn: " + hasIn + " | HasOut: " + hasOut);
    
    // Return: [Enable In?, Enable Out?]
    return new boolean[]{!hasIn, (hasIn && !hasOut)};
}


// Add this inside EmployeeManagementService.java
public EmployeeDAO getEmployeeDao() {
    return this.employeeDao;
}

public String[] getFormattedDataForForm(Object[] rawData) {
    String[] formatted = new String[21]; // Matches your 21 labels
    if (rawData == null) return formatted;

    // 1. Map ID, Last Name, First Name (0, 1, 2)
    formatted[0] = String.valueOf(rawData[0]);
    formatted[1] = String.valueOf(rawData[1]);
    formatted[2] = String.valueOf(rawData[2]);

    // 2. THE FIX: Pull Gender from CSV Index 20 -> UI Index 3
    if (rawData.length > 20) {
        formatted[3] = String.valueOf(rawData[20]); 
    } else {
        formatted[3] = "Not Set";
    }

    // 3. Map Birthday (CSV Index 3 -> UI Index 4)
    formatted[4] = String.valueOf(rawData[3]);

    // 4. Map the rest (CSV Indices 4-19 -> UI Indices 5-20)
    for (int i = 5; i < 21; i++) {
        formatted[i] = String.valueOf(rawData[i - 1]);
    }

    return formatted;
}

}