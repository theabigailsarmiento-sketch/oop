package dao;

import model.Attendance;
import java.util.List;

public interface AttendanceDAO {
    // Returns list of models for logic
    List<Attendance> getAttendanceByEmployee(int empNo);
    
    // Returns formatted data for JTable
    Object[][] getAttendanceByMonth(int empNo, String month);
    
    // Action to save to CSV
    void recordAttendance(int empNo, String type);
}