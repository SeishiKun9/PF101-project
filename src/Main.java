import storage.FileManager;
import model.*;
import ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.LocalDateTime;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        setupUI();
        
        FileManager fileManager = new FileManager();
        seedDummyDataIfEmpty(fileManager);

        // Start the application on the Login Screen
        SwingUtilities.invokeLater(() -> new LoginFrame(fileManager).setVisible(true));
    }

    private static void setupUI() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
    }

    /** default users and requests if they are empty. */
    private static void seedDummyDataIfEmpty(FileManager fm) {
        if (fm.readUsers().isEmpty()) {
            fm.saveUser(new Admin("admin", "admin123", "ADM1000"));
            fm.saveUser(new MedicalStaff("Dr. Santos", "med123", "STF2000"));
            fm.saveUser(new FoodStaff("Aling Nena", "food123", "STF2001"));
            fm.saveUser(new TransportationStaff("Mang Tomas", "trans123", "STF2002"));
            fm.saveUser(new Resident("Juan Dela Cruz", "res123", "RSD3000", "123 Rizal St.", "917123456"));
        }

        // Default Requests
        if (fm.readRequests().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            
            MedicalRequest dummyRequest = new MedicalRequest(
                "REQ4000", 
                "RSD3000", 
                "123 Rizal St.",
                now.plusDays(1).withHour(9).withMinute(0), 
                "Needed for hypertension", 
                "Pending", 
                now,
                Arrays.asList(new MedicalItem("Amlodipine", 1, "box"))
            );
            
            fm.saveRequest(dummyRequest);
        }
    }
}
