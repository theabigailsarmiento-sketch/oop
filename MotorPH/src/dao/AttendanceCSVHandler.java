package dao;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.Attendance;

public class AttendanceCSVHandler implements AttendanceDAO {
    private static final String FILE_PATH = "resources/MotorPH_AttendanceRecord.csv";
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

    @Override
    public List<Attendance> getAttendanceByEmployee(int empNo) {
        List<Attendance> attendanceList = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                // FIXED: Using shared utility logic to handle quotes safely
                String[] parts = CSVUtils.splitCSVLine(line);
                
                if (parts.length >= 6 && Integer.parseInt(parts[0].trim()) == empNo) {
                    try {
                        LocalDate date = LocalDate.parse(parts[3].trim(), dateFormat);
                        LocalTime logIn = LocalTime.parse(parts[4].trim(), timeFormat);
                        
                        // If logOut is empty, default to logIn or null
                        String logOutStr = parts[5].trim();
                        LocalTime logOut = logOutStr.isEmpty() ? null : LocalTime.parse(logOutStr, timeFormat);

                        attendanceList.add(new Attendance(empNo, date, logIn, logOut));
                    } catch (Exception e) {
                        // Skip malformed rows
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading attendance: " + e.getMessage());
        }
        return attendanceList;
    }

    @Override
    public Object[][] getAttendanceByMonth(int empNo, String month) {
        List<Attendance> allLogs = getAttendanceByEmployee(empNo);
        List<Object[]> filteredRows = new ArrayList<>();

        for (Attendance a : allLogs) {
            String logMonth = a.getDate().getMonth().name(); // e.g., "JUNE"
            
            if (month.equalsIgnoreCase("All") || logMonth.equalsIgnoreCase(month)) {
                filteredRows.add(new Object[]{
                    a.getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                    a.getTimeIn() != null ? a.getTimeIn().toString() : "N/A",
                    a.getTimeOut() != null ? a.getTimeOut().toString() : "N/A"
                });
            }
        }
        return filteredRows.toArray(new Object[0][]);
    }

    @Override
    public void recordAttendance(int empNo, String type) {
        // Implementation for writing to CSV goes here
        // 1. Get current date and time
        // 2. Append a new row to MotorPH_AttendanceRecord.csv
        System.out.println("DEBUG: Recording " + type + " for Employee #" + empNo);
    }
}