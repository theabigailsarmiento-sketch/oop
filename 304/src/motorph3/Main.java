package motorph3; // 1. Matches the folder name 'motorph3'

// 2. Import the classes from your other packages
import dao.CSVHandler;
import dao.EmployeeDAO;
import dao.UserLibrary;
import ui.LoginPanel;

public class Main {
    public static void main(String[] args) {
        
        // 1. Create the data handler once
        EmployeeDAO dao = new CSVHandler();
        
        // 2. Create the auth service once (Passes DAO into UserLibrary)
        UserLibrary auth = new UserLibrary(dao);

        java.awt.EventQueue.invokeLater(() -> {
            // 3. Pass both to the UI (Matches the new LoginPanel constructor)
            new LoginPanel(dao, auth).setVisible(true);
        });
    }
}