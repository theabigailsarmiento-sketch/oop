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
    private static final String ATTENDANCE_CSV = "resources/MotorPH_AttendanceRecord.csv";
    private static final String LEAVE_FILE = "resources/MotorPH_LeaveRequests.csv";

    // Matches your CSV format: M/d/yyyy (e.g. 9/24/1948)
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    public CSVHandler() {
        loadAllIntoCache(); 
    }

    private void loadAllIntoCache() {
        employeeCache.clear();
        usernameCache.clear();
        
        // 1. Load Employees
        try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_DATA_CSV))) {
            br.readLine(); // Skip Header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; 
                
                String[] data = splitCSVLine(line);
                if (data.length < 19) continue;
                
                LocalDate birthday = null;
                String bdayStr = clean(data[3]);
                if (!bdayStr.isEmpty()) {
                    try { birthday = LocalDate.parse(bdayStr, dateFormatter); } 
                    catch (Exception e) { /* Date parse fail */ }
                }
                
                Employee emp = mapToEmployee(data, birthday);
                if (emp != null) employeeCache.put(emp.getEmpNo(), emp);
            }
        } catch (IOException e) { System.err.println("Employee File Error: " + e.getMessage()); }

        // 2. Load Logins & Upgrade Roles
        try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_DATA_CSV))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] loginData = splitCSVLine(line);
                if (loginData.length >= 6) {
                    try {
                        int id = Integer.parseInt(loginData[0].trim());
                        String realUsername = loginData[1].trim().toLowerCase(); 
                        String roleStr = loginData[5].trim();
                        Employee baseEmp = employeeCache.get(id);
                        if (baseEmp != null) {
                            Employee upgraded = upgradeEmployeeRole(baseEmp, roleStr);
                            upgraded.setPassword(loginData[4].trim());
                            employeeCache.put(id, upgraded);
                            usernameCache.put(realUsername, upgraded);
                        }
                    } catch (Exception e) { }
                }
            }
        } catch (IOException e) { System.err.println("Login File Error: " + e.getMessage()); }
    }

    // THE CORE FIX: Parses CSV lines while respecting double quotes
    private String[] splitCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder curVal = new StringBuilder();
        boolean inQuotes = false;
        
        for (char ch : line.toCharArray()) {
            if (ch == '\"') {
                inQuotes = !inQuotes; 
            } else if (ch == ',' && !inQuotes) {
                result.add(curVal.toString().trim());
                curVal.setLength(0); 
            } else {
                curVal.append(ch);
            }
        }
        result.add(curVal.toString().trim());
        return result.toArray(new String[0]);
    }
   
    private Employee mapToEmployee(String[] data, LocalDate birthday) {
        try {
            int id = Integer.parseInt(clean(data[0])); 
            double basicSalary = parseCurrency(data[13]); 

            String roleStr = (data.length >= 20) ? clean(data[19]) : "REGULAR_STAFF";
            // Use your upgrade method immediately during initial load
        Employee base = new RegularStaff(id, clean(data[1]), clean(data[2]), birthday, basicSalary);
        Employee emp = upgradeEmployeeRole(base, roleStr);
            
            
            
            emp.setAddress(clean(data[4]));
            emp.setPhone(clean(data[5]));
            emp.setSss(clean(data[6]));
            emp.setPhilhealth(clean(data[7]));
            emp.setTin(clean(data[8]));
            emp.setPagibig(clean(data[9]));
            emp.setStatus(clean(data[10]));
            emp.setPosition(clean(data[11]));
            emp.setSupervisor(clean(data[12])); 
            
            emp.setRiceSubsidy(parseCurrency(data[14]));
            emp.setPhoneAllowance(parseCurrency(data[15]));
            emp.setClothingAllowance(parseCurrency(data[16]));
            emp.setGrossRate(parseCurrency(data[17]));
            emp.setHourlyRate(parseCurrency(data[18]));
            return emp;
        } catch (Exception e) { return null; }
    }

    private Employee upgradeEmployeeRole(Employee o, String roleName) {
        Role role = Role.fromString(roleName);
        Employee upgraded = switch (role) {
            case ADMIN -> new Admin(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary());
            case HR_STAFF -> new HRStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary());
            case IT_STAFF -> new ITStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary());
            case ACCOUNTING -> new AccountingStaff(o.getEmpNo(), o.getLastName(), o.getFirstName(), o.getBirthday(), o.getBasicSalary());
            default -> o;
        };
        upgraded.setRole(role);
        copyFinancials(o, upgraded);
        return upgraded;
    }

    private void copyFinancials(Employee from, Employee to) {
        to.setRiceSubsidy(from.getRiceSubsidy());
        to.setPhoneAllowance(from.getPhoneAllowance());
        to.setClothingAllowance(from.getClothingAllowance());
        to.setGrossRate(from.getGrossRate());
        to.setHourlyRate(from.getHourlyRate());
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

    @Override
    public void applyForLeave(int id, String type, String start, String end, String reason) {
        Employee emp = findById(id);
        String last = (emp != null) ? emp.getLastName() : "Unknown";
        String first = (emp != null) ? emp.getFirstName() : "User";
        // Wrapped reason in quotes for CSV safety
        String line = String.format("%d,%s,%s,%s,%s,%s,\"%s\",Pending", id, last, first, type, start, end, reason.replace("\"", ""));
        
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(LEAVE_FILE, true)))) {
            out.println(line);
        } catch (IOException e) { System.err.println("Write Error: " + e.getMessage()); }
    }

    @Override
    public Object[][] getLeaveStatusByEmpId(int empId) {
        List<Object[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(LEAVE_FILE))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = splitCSVLine(line); // FIXED: Use splitCSVLine
                if (d.length >= 8 && clean(d[0]).equals(String.valueOf(empId))) {
                    records.add(d); 
                }
            }
        } catch (IOException e) { }
        return records.toArray(new Object[0][]);
    }

    @Override 
    public Object[][] getAllLeaveRequests() { 
        List<Object[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(LEAVE_FILE))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = splitCSVLine(line); // FIXED: Use splitCSVLine
                if (d.length >= 8) records.add(d);
            }
        } catch (IOException e) { }
        return records.toArray(new Object[0][]);
    }

    @Override 
    public void updateLeaveStatus(int id, String start, String status) {
        List<String[]> allData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(LEAVE_FILE))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = splitCSVLine(line);
                if (d[0].equals(String.valueOf(id)) && d[4].equals(start)) {
                    d[7] = status; 
                }
                allData.add(d);
            }
        } catch (IOException e) { e.printStackTrace(); }

        try (PrintWriter pw = new PrintWriter(new FileWriter(LEAVE_FILE))) {
            pw.println("ID,LastName,FirstName,Type,Start,End,Reason,Status");
            for (String[] row : allData) {
                pw.println(String.format("%s,%s,%s,%s,%s,%s,\"%s\",%s", row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7]));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public Object[][] getAttendanceByMonth(int empId, String month) {
        List<Object[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ATTENDANCE_CSV))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = splitCSVLine(line); // FIXED: Use splitCSVLine
                if (data.length < 6) continue;
                if (clean(data[0]).equals(String.valueOf(empId))) {
                    String dateStr = clean(data[3]); 
                    if (month.equalsIgnoreCase("All") || isMonthMatch(dateStr, month)) {
                        records.add(new Object[]{ dateStr, clean(data[4]), clean(data[5]) });
                    }
                }
            }
        } catch (IOException e) { }
        return records.toArray(new Object[0][]);
    }

    private boolean isMonthMatch(String dateStr, String monthName) {
        try {
            int m = Integer.parseInt(dateStr.split("/")[0]);
            String[] months = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            return months[m].equalsIgnoreCase(monthName.trim());
        } catch (Exception e) { return false; }
    }

    private String clean(String s) { return (s == null) ? "" : s.trim().replace("\"", ""); }

    private double parseCurrency(String s) {
        if (s == null || s.trim().isEmpty()) return 0.0;
        String cleanValue = s.trim().replace("\"", "").replace(",", "");
        try { return Double.parseDouble(cleanValue); } 
        catch (NumberFormatException e) { return 0.0; }
    }




    // FOR IT 

   // FOR IT OPERATIONS
  // Inside CSVHandler.java
@Override
public void updateEmployeeStatus(int empNo, String newStatus) {
    // 1. Update the local cache (the HashMap)
    Employee emp = findById(empNo);
    if (emp != null) {
        emp.setStatus(newStatus);
        
        // 2. Persist to file (File Streaming)
        saveAllToCSV(new ArrayList<>(employeeCache.values()));
        System.out.println("CSV Updated: Employee " + empNo + " is now " + newStatus);
    }
}

    @Override
    public void saveNewPassword(int empNo, String newPassword) {
        Employee emp = findById(empNo);
        if (emp != null) {
            emp.setPassword(newPassword); // Update cache
            saveLoginsToCSV();            // We need a method to save the Login CSV specifically
            System.out.println("CSVHandler: Password persisted for " + empNo);
        }
    }

    // Helper method to save the separate Login CSV
private void saveLoginsToCSV() {
    try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(LOGIN_DATA_CSV)))) {
        out.println("Employee #,Username,Last Name,First Name,Password,Role");
        for (Employee e : employeeCache.values()) {
            String pass = (e.getPassword() != null) ? e.getPassword() : "password123";
            // Get the actual role from the upgraded object
            String roleName = (e.getRole() != null) ? e.getRole().name() : "REGULAR_STAFF";
            
            out.printf("%d,%s,%s,%s,%s,%s%n", 
                e.getEmpNo(), 
                e.getFirstName().toLowerCase() + e.getEmpNo(), 
                e.getLastName(), e.getFirstName(), pass, roleName);
        }
    } catch (IOException e) { System.err.println("Login Save Error: " + e.getMessage()); }
}

    @Override public Employee findById(int id) { return employeeCache.get(id); }
    @Override public Employee findByUsername(String u) { return usernameCache.get(u.trim().toLowerCase()); }
    @Override public List<Employee> getAll() { return new ArrayList<>(employeeCache.values()); }
    
    @Override 
    public boolean update(Employee emp) { 
        employeeCache.put(emp.getEmpNo(), emp); 
        return saveAllToCSV(new ArrayList<>(employeeCache.values())); 
    }

    @Override public boolean updateEmployee(Employee e) { return update(e); }
    @Override public boolean addEmployee(Employee e) { List<Employee> all = getAll(); all.add(e); return saveAllToCSV(all); }
    @Override public boolean deleteEmployee(int id) { List<Employee> all = getAll(); all.removeIf(e -> e.getEmpNo() == id); return saveAllToCSV(all); }
    @Override public Object[][] getAttendanceById(int id) { return getAttendanceByMonth(id, "All"); }
    @Override public void recordAttendance(int id, String type) {} 

    @Override
    public List<LeaveRequest> getAllLeaveRequestsList() {
        List<LeaveRequest> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(LEAVE_FILE))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = splitCSVLine(line);
                if (d.length >= 8) {
                    list.add(new LeaveRequest(
                        Integer.parseInt(clean(d[0])), clean(d[1]), clean(d[2]), clean(d[3]), 
                        LocalDate.parse(clean(d[4]), dateFormatter), 
                        LocalDate.parse(clean(d[5]), dateFormatter), clean(d[6])
                    ));
                }
            }
        } catch (Exception e) { }
        return list;
    }

private boolean saveAllToCSV(List<Employee> employees) {
    employees.sort(Comparator.comparingInt(Employee::getEmpNo));
    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(EMPLOYEE_DATA_CSV)))) {
        // 1. Added Role to header
        writer.println("Employee #,Last Name,First Name,Birthday,Address,Phone #,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate,Role");
        
        for (Employee e : employees) {
            String bday = (e.getBirthday() != null) ? e.getBirthday().format(dateFormatter) : "";
            // 2. Added %s at the end for the Role
            writer.printf("%d,%s,%s,%s,\"%s\",%s,%s,%s,%s,%s,%s,%s,\"%s\",%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s%n",
                e.getEmpNo(), e.getLastName(), e.getFirstName(), bday, 
                e.getAddress().replace("\"", ""), e.getPhone(), e.getSss(), e.getPhilhealth(), 
                e.getTin(), e.getPagibig(), e.getStatus(), e.getPosition(), 
                e.getSupervisor().replace("\"", ""), e.getBasicSalary(), 
                e.getRiceSubsidy(), e.getPhoneAllowance(), e.getClothingAllowance(), 
                e.getGrossRate(), e.getHourlyRate(),
                e.getRole()); // <--- New Role field
        }
        return true;
    } catch (IOException e) { return false; }
}
}