package dao;

public class CSVUtils {
    
 public static String[] splitCSVLine(String line) {
    // This regex is the industry standard for Java CSV parsing without external libraries.
    // It splits by comma only if that comma is NOT inside a pair of double quotes.
    return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
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