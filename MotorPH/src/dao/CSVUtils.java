package dao;

import java.util.ArrayList;
import java.util.List;

public class CSVUtils {
    
    public static String[] splitCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder curVal = new StringBuilder();
        boolean inQuotes = false;
        for (char ch : line.toCharArray()) {
            if (ch == '\"') inQuotes = !inQuotes; 
            else if (ch == ',' && !inQuotes) {
                result.add(curVal.toString().trim());
                curVal.setLength(0); 
            } else curVal.append(ch);
        }
        result.add(curVal.toString().trim());
        return result.toArray(new String[0]);
    }

    public static String clean(String s) {
        return (s == null) ? "" : s.trim().replace("\"", "");
    }

    public static double parseCurrency(String s) {
        try {
            return Double.parseDouble(s.replace("\"", "").replace(",", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }
}