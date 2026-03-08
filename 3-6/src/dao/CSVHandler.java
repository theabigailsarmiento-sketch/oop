package dao;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.*;

public class CSVHandler implements EmployeeDAO {

    private final Map<Integer, Employee> employeeCache = new HashMap<>();
    private final Map<String, Employee> usernameCache = new HashMap<>();
    
    private static final String EMPLOYEE_DATA_CSV = "resources/MotorPH_EmployeeData.csv";
    private static final String LOGIN_DATA_CSV = "resources/MotorPH_EmployeeLogin.csv";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    public CSVHandler() {
        loadAllIntoCache(); 
    }

    @Override
    public boolean addEmployee(Employee emp) {
        if (emp == null) return false;
        employeeCache.put(emp.getEmpNo(), emp);
        return saveAllToCSV();
    }

   // --- DAO: File Streaming & Data Mapping ---

private void loadAllIntoCache() {
    employeeCache.clear();
    usernameCache.clear();

    // 1. Load Main Employee Data
    try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_DATA_CSV))) {
        br.readLine(); // Skip Header
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue; 
            
            // Regex split to handle addresses with commas inside quotes
            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            
            // Cleanup ID (handling BOM and spaces)
            if (data.length > 0 && data[0] != null) {
                data[0] = data[0].replace("\uFEFF", "").trim();
            }
            
            // Basic data integrity check
            if (data.length < 19) continue;
            
            // --- FLEXIBLE DATE PARSING (Integrated Fix) ---
            String bdayStr = (data[3] != null) ? data[3].trim() : "";
            LocalDate birthday = null;

            if (!bdayStr.isEmpty()) {
                try {
                    if (bdayStr.contains("-")) {
                        // Handles ISO format: 1987-10-21
                        birthday = LocalDate.parse(bdayStr); 
                    } else if (bdayStr.contains("/")) {
                        // Handles CSV format: 10/21/1987 (MM/dd/yyyy)
                        birthday = LocalDate.parse(bdayStr, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    }
                } catch (Exception e) {
                    System.err.println("Birthday parse error for ID " + data[0] + ": " + bdayStr);
                    birthday = LocalDate.of(1900, 1, 1); // Default fallback to prevent crash
                }
            }

            // Map the base data using the parsed birthday
            Employee emp = mapToEmployee(data, birthday);
            
            if (emp != null) {
                // --- GENDER SAFETY CHECK (Index 20) ---
                if (data.length > 20) {
                    emp.setGender(data[20].trim());
                } else {
                    emp.setGender("Not Set"); 
                }
                
                employeeCache.put(emp.getEmpNo(), emp);
            }
        }
    } catch (IOException e) { 
        System.err.println("DAO Error: " + e.getMessage()); 
    }

    // 2. Load Login & Upgrade Roles
    try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_DATA_CSV))) {
        br.readLine(); // Skip Header
        String line;
        while ((line = br.readLine()) != null) {
            // Using your simplified split for logins
            String[] loginData = line.split(","); 
            if (loginData.length >= 6) {
                try {
                    int id = Integer.parseInt(loginData[0].replace("\uFEFF", "").trim());
                    Employee baseEmp = employeeCache.get(id);
                    
                    if (baseEmp != null) {
                        String roleStr = loginData[5].trim();
                        // Upgrade the role but KEEP the existing data
                        Employee upgraded = upgradeEmployeeRole(baseEmp, roleStr);
                        upgraded.setPassword(loginData[4].trim());
                        
                        // Re-sync with caches
                        employeeCache.put(id, upgraded);
                        usernameCache.put(loginData[1].trim().toLowerCase(), upgraded);
                    }
                } catch (Exception e) {
                    // Skip malformed login lines silently
                }
            }
        }
    } catch (IOException e) { 
        System.err.println("Login Error: " + e.getMessage()); 
    }
}
private Employee mapToEmployee(String[] data, LocalDate birthday) {
    try {
        // 1. Basic ID and Salary Parsing
        int id = Integer.parseInt(CSVUtils.clean(data[0])); 
        double basicSalary = CSVUtils.parseCurrency(data[13]); 
        
        // 2. Initialize the Employee object
        Employee emp = new RegularStaff(id, CSVUtils.clean(data[1]), CSVUtils.clean(data[2]), birthday, basicSalary);
        
        // 3. Standard Information
        emp.setAddress(CSVUtils.clean(data[4]));
        emp.setPhone(CSVUtils.clean(data[5]));
        emp.setSss(CSVUtils.clean(data[6]));
        emp.setPhilhealth(CSVUtils.clean(data[7]));
        emp.setTin(CSVUtils.clean(data[8]));
        emp.setPagibig(CSVUtils.clean(data[9]));
        emp.setStatus(CSVUtils.clean(data[10]));
        emp.setPosition(CSVUtils.clean(data[11]));    
        emp.setSupervisor(CSVUtils.clean(data[12]));  
        
        // 4. Financial Allowances
        emp.setRiceSubsidy(CSVUtils.parseCurrency(data[14]));      
        emp.setPhoneAllowance(CSVUtils.parseCurrency(data[15]));   
        emp.setClothingAllowance(CSVUtils.parseCurrency(data[16]));
       
        // 5. Rates
        emp.setGrossRate(CSVUtils.parseCurrency(data[17]));        
        emp.setHourlyRate(CSVUtils.parseCurrency(data[18]));       

        // 6. Handle Role (Index 19)
        if (data.length > 19) {
            try { 
                emp.setRole(Role.valueOf(data[19].toUpperCase().trim())); 
            } catch(Exception e) {
                // Default role if parsing fails
                emp.setRole(Role.REGULAR_STAFF); 
            }
        }

        // 7. Handle Gender (Index 20)
        // If length is 20, max index is 19. We need length to be at least 21 to have index 20.
        if (data.length > 20) {
            String genderVal = CSVUtils.clean(data[20]);
            emp.setGender(genderVal.isEmpty() ? "Not Set" : genderVal);
        } else {
            emp.setGender("Not Set");
        }

        return emp;
        
    } catch (Exception e) { 
        System.err.println("DAO Mapping error for Employee ID " + (data.length > 0 ? data[0] : "Unknown") + ": " + e.getMessage());
        return null; 
    }
}
private void copyFinancials(Employee from, Employee to) {
    // Ensure all data is transferred to the new specialized role object
    to.setBasicSalary(from.getBasicSalary());
    to.setRiceSubsidy(from.getRiceSubsidy());
    to.setPhoneAllowance(from.getPhoneAllowance());
    to.setClothingAllowance(from.getClothingAllowance());
    to.setGrossRate(from.getGrossRate());
    to.setHourlyRate(from.getHourlyRate());
    to.setGender(from.getGender());
    to.setAddress(from.getAddress());
    to.setPhone(from.getPhone());
    to.setSss(from.getSss());
    to.setPhilhealth(from.getPhilhealth());
    to.setTin(from.getTin());
    to.setPagibig(from.getPagibig());
    to.setStatus(from.getStatus());
    to.setPosition(from.getPosition());
    to.setSupervisor(from.getSupervisor());
}

private Employee upgradeEmployeeRole(Employee o, String roleName) {
    Role role = Role.fromString(roleName);
    
    Employee upgraded = switch (role) {
        // ADD 'o.getGender()' as the 6th parameter here:
        case ADMIN -> new Admin(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary(), o.getGender());
        
        // Do the same for these (assuming they have the 6-param constructor too):
        case HR_STAFF -> new HRStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary(), o.getGender());
        case IT_STAFF -> new ITStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary(), o.getGender());
        case ACCOUNTING -> new AccountingStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary(), o.getGender());
        
        default -> o;
    };

    upgraded.setRole(role);
    copyFinancials(o, upgraded);
    return upgraded;
}
    
    @Override public List<Employee> getAll() { return new ArrayList<>(employeeCache.values()); }
    @Override public Employee findById(int id) { return employeeCache.get(id); }
    @Override public Employee findByUsername(String u) { return usernameCache.get(u.trim().toLowerCase()); }

    @Override
    public boolean update(Employee emp) { 
        employeeCache.put(emp.getEmpNo(), emp); 
        return saveAllToCSV(); 
    }

    @Override public boolean deleteEmployee(int id) { 
        employeeCache.remove(id); 
        return saveAllToCSV(); 
    }

private boolean saveAllToCSV() {
    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(EMPLOYEE_DATA_CSV)))) {
        // 1. Write the Header (Ensuring "Gender" is the 21st label)
        writer.println("Employee #,Last Name,First Name,Birthday,Address,Phone #,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate,Role,Gender");
        
        for (Employee e : employeeCache.values()) {
            // Null-safe checks
            String bdayStr = (e.getBirthday() != null) ? e.getBirthday().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "";
            String addr = (e.getAddress() != null) ? e.getAddress() : "";
            String superv = (e.getSupervisor() != null) ? e.getSupervisor() : "N/A";
            
            // Explicitly handle the Gender fallback
            String gender = (e.getGender() != null && !e.getGender().trim().isEmpty()) ? e.getGender() : "Not Set";
            
            // Handle commas in text fields
            String safeAddress = addr.contains(",") ? "\"" + addr + "\"" : addr;
            String safeSupervisor = superv.contains(",") ? "\"" + superv + "\"" : superv;
            
            // Get Role name safely
            String roleStr = (e.getRole() != null) ? e.getRole().name() : "REGULAR_STAFF";

            // 2. WRITE LINE
            // This format string uses 21 placeholders.
            writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%s%n",
                e.getEmpNo(),           // 1
                e.getLastName(),        // 2
                e.getFirstName(),       // 3
                bdayStr,                // 4
                safeAddress,            // 5
                e.getPhone(),           // 6
                e.getSss(),             // 7
                e.getPhilhealth(),      // 8
                e.getTin(),             // 9
                e.getPagibig(),         // 10
                e.getStatus(),          // 11
                e.getPosition(),        // 12
                safeSupervisor,         // 13
                e.getBasicSalary(),     // 14
                e.getRiceSubsidy(),     // 15
                e.getPhoneAllowance(),  // 16
                e.getClothingAllowance(),// 17
                e.getGrossRate(),       // 18
                e.getHourlyRate(),      // 19
                roleStr,                // 20
                gender                  // 21
            );
        }
        writer.flush(); // Ensure everything is written
        return true;
    } catch (IOException e) { 
        System.err.println("Error saving CSV: " + e.getMessage());
        return false; 
    }
}
    public Object[][] getAttendanceByMonth(int empNo, String month) {
    return new Object[0][3]; 
}


@Override
public Employee getById(int id) {
    // This assumes you already have a method named findById 
    // or a list named 'employees'
    return findById(id); 
}



@Override
public boolean create(Employee emp) {
    if (emp == null) return false;

    try {
        // 1. Add to the local cache so the UI updates immediately
        employeeCache.put(emp.getEmpNo(), emp);
        
        // 2. Add to the username cache if a username/password exists
        // (Assuming you might need this for login later)
        if (emp.getFirstName() != null) {
            usernameCache.put(emp.getFirstName().toLowerCase(), emp);
        }

        // 3. Use your existing private method to write the entire cache to the CSV
        // This returns a boolean, which matches your Interface requirement!
        return saveAllToCSV();

    } catch (Exception e) {
        System.err.println("Error creating employee: " + e.getMessage());
        return false;
    }
}





    @Override public void applyForLeave(int id, String type, String start, String end, String reason) {}
    @Override public Object[][] getLeaveStatusByEmpId(int empId) { return new Object[0][0]; }
    @Override public Object[][] getAllLeaveRequests() { return new Object[0][0]; }
    @Override public void updateLeaveStatus(String reqId, String stat) {}
    @Override public void updateEmployeeStatus(int id, String stat) { update(findById(id)); }
    @Override public void saveNewPassword(int id, String pass) {}
    @Override public List<LeaveRequest> getAllLeaveRequestsList() { return new ArrayList<>(); }

    

// Correct DAO Implementation
@Override
public int getLastEmployeeNumber() {
    if (employeeCache.isEmpty()) {
        return 10000;
    }
    return Collections.max(employeeCache.keySet());
}

@Override
public int getNextAvailableId() {
    // In the DAO, we just return the next number based on the cache
    return getLastEmployeeNumber() + 1;
}
}