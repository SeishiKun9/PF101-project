package model;

import storage.FileManager;
import storage.RequestManager;
import ui.AdminMenuFrame;
import java.util.List;

public class Admin extends User {
    private String adminId;

    public Admin(String name, String password, String adminId) {
        super(name, password, "Admin");
        this.adminId = adminId;
    }

    public String getAdminId() { 
        return adminId; 
    }

    public void setAdminId(String adminId) { 
        this.adminId = adminId; 
    }

    @Override
    public String getUserId() { 
        return adminId; 
    }

    public void createUser(User user) {
        if (user instanceof Resident) {
            String error = ((Resident) user).validateDetails();
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
        }

        FileManager fm = new FileManager();
        for (User existing : fm.readUsers()) {
            if (existing.getUserId().equalsIgnoreCase(user.getUserId())) {
                throw new IllegalArgumentException("User ID already exists.");
            }
        }
        fm.saveUser(user);
    }

       public void deleteUser(String userId) {
        FileManager fm = new FileManager();
        List<User> users = fm.readUsers();

        for (AssistanceRequest req : new RequestManager(fm).getAllRequests()) {
            if (req.getResidentId().equals(userId)) {
                throw new IllegalArgumentException("This resident still has requests and cannot be deleted.");
            }
        }

        for (AssistanceRequest req : new RequestManager(fm).getAllRequests()) {
            if (userId.equals(req.getAssignedStaffId())) {
                if (!"Completed".equalsIgnoreCase(req.getStatus()) && !"Cancelled".equalsIgnoreCase(req.getStatus())) {
                    throw new IllegalArgumentException("This staff member is currently handling request " 
                            + req.getRequestId() + ".\nThey must finish the request before their account can be deleted.");
                }
            }
        }

        users.removeIf(user -> user.getUserId().equals(userId));
        fm.saveAllUsers(users);
    }

<<<<<<< HEAD
=======


>>>>>>> origin/main
    public void manageUser(String userId) {
        System.out.println(getFullName() + " is managing user " + userId + ".");
    }

    public void viewUserHistory() {
        System.out.println(getFullName() + " is viewing a user's history.");
    }

    public void viewAllRequest() {
        System.out.println(getFullName() + " is viewing all requests.");
    }

    public void updateRequestStatus(AssistanceRequest request, String status) {
        FileManager fm = new FileManager();
        Resident owner = findResident(fm, request.getResidentId());
        if (owner == null) {
            throw new IllegalArgumentException("Resident " + request.getResidentId() + " was not found.");
        }
        request.updateStatus(status);
        new RequestManager(fm).updateRequest(request);
        fm.recordResidentRequestAction(owner, request, "Status changed to " + status + " by " + getUserId());
    }

    private Resident findResident(FileManager fm, String residentId) {
        for (User user : fm.readUsers()) {
            if (user instanceof Resident && residentId.equals(((Resident) user).getResidentId())) {
                return (Resident) user;
            }
        }
        return null;
    }

    public void deleteRequest(AssistanceRequest request) {
        new FileManager().deleteRequestFromFile(request.getRequestId());
    }

    public static boolean isOpen(AssistanceRequest request) {
        String status = request.getStatus();
        return status != null
                && !status.equalsIgnoreCase("Completed")
                && !status.equalsIgnoreCase("Cancelled");
    }

    @Override
    public void showMenu() {
        new AdminMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return "Admin|" 
                + AssistanceRequest.encodeField(getFullName()) + "|"
                + AssistanceRequest.encodeField(getPassword()) + "|"
                + AssistanceRequest.encodeField(adminId);
    }
}