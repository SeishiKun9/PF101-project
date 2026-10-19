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

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    @Override
    public String getUserId() { return adminId; }


    public void createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        String contactError = user.validateContactDetails();
        if (contactError != null) {
            throw new IllegalArgumentException(contactError);
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
        users.removeIf(user -> user.getUserId().equals(userId));
        fm.saveAllUsers(users);
    }

    public void manageUser(String userId) {
        System.out.println(getFullName() + " is managing user " + userId + ".");
    }

    public void viewUserHistory() {
        System.out.println(getFullName() + " is viewing a user's history.");
    }


    public void fileRequestFor(String residentId, AssistanceRequest request) {

        if (residentId == null || residentId.trim().isEmpty() || request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException("A valid resident and matching request are required.");
        }

        RequestManager rm = new RequestManager(new FileManager());

        if (rm.findRequest(request.getRequestId()) != null) {
            throw new IllegalArgumentException("Request ID already exists.");
        }

        rm.addRequest(request);
    }

    public void viewAllRequest() {
        System.out.println(getFullName() + " is viewing all requests.");
    }

    public void updateRequestStatus(AssistanceRequest request, String status) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required.");
        }
        if (!isOpen(request)) {
            throw new IllegalArgumentException("Completed or cancelled requests cannot be updated.");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required.");
        }

        request.updateStatus(status);
        saveRequest(request); 
    }

    public void assignStaff(AssistanceRequest request, Staff staff) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required.");
        }
        if (!"Pending".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException("Only Pending requests can be assigned or reassigned.");
        }
        if (staff != null && !staff.canHandle(request)) {
            throw new IllegalArgumentException("This staff member does not handle " + request.getType() + " requests.");
        }

        request.setAssignedStaffId(staff == null ? null : staff.getStaffId());
        saveRequest(request); 
    }

    public void deleteRequest(AssistanceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required.");
        }
        new FileManager().deleteRequestFromFile(request.getRequestId());
    }

    public static boolean isOpen(AssistanceRequest request) {
        String status = request.getStatus();
        return status != null && !"Completed".equalsIgnoreCase(status) && !"Cancelled".equalsIgnoreCase(status);
    }

    private void saveRequest(AssistanceRequest request) {
        new RequestManager(new FileManager()).updateRequest(request);
    }

    @Override
    public void showMenu() {
        new AdminMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|",
                "Admin",
                AssistanceRequest.encodeField(getFullName()),
                AssistanceRequest.encodeField(getPassword()),
                AssistanceRequest.encodeField(adminId),
                AssistanceRequest.encodeField(getAddress()),
                AssistanceRequest.encodeField(getContactNo()));
    }
}