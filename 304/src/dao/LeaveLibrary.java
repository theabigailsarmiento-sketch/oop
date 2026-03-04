package dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class LeaveLibrary {
    private static final String LEAVE_FILE = "resources/MotorPH_LeaveRequests.csv";

    public List<String[]> fetchAllLeaves() {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(LEAVE_FILE))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                data.add(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
            }
        } catch (IOException e) { 
            System.err.println("File Error: " + e.getMessage()); 
        }
        return data;
    }

    public void saveLeave(String csvLine) {
        try (FileWriter fw = new FileWriter(LEAVE_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(csvLine);
        } catch (IOException e) { 
            System.err.println("Save Error: " + e.getMessage()); 
        }
    }
}