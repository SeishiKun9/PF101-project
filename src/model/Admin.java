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

        User target = findUser(users, userId);
        if (target == null) {
            return;
        }

        if (target instanceof Admin && countAdmins(users) <= 1) {
            throw new IllegalArgumentException("You cannot delete the last Admin account.");
        }
        if (target instanceof Resident && residentHasRequests(fm, userId)) {
            throw new IllegalArgumentException("This resident still has requests and cannot be deleted.");
        }

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equalsIgnoreCase(userId)) {
                users.remove(i);
                break;
            }
        }
        fm.saveAllUsers(users);
    }

    private User findUser(List<User> users, String userId) {
        for (User user : users) {
            if (user.getUserId().equalsIgnoreCase(userId)) {
                return user;
            }
        }
        return null;
    }

    private int countAdmins(List<User> users) {
        int count = 0;
        for (User user : users) {
            if (user instanceof Admin) {
                count++;
            }
        }
        return count;
    }

    private boolean residentHasRequests(FileManager fm, String residentId) {
        for (AssistanceRequest request : fm.readRequests()) {
            if (residentId.equalsIgnoreCase(request.getResidentId())) {
                return true;
            }
        }
        return false;
    }

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