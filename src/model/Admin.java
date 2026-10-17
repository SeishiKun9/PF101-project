package model;

import storage.FileManager;
import storage.RequestManager;
import ui.AdminMenuFrame;
import java.util.List;

/**
 * Admin: manages user accounts and has oversight of all requests.
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;
    private String adminId;

    public Admin(String name, String password, String adminId) {
        super(name, password, "Admin");
        this.adminId = adminId;
    }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    @Override public String getUserId() { return adminId; }

    public void createUser(User user) {
        if (user == null) throw new IllegalArgumentException("User is required.");
        new FileManager().saveUser(user);
    }

    public void manageUser(String userId) {
        System.out.println(getFullName() + " is managing user " + userId + ".");
    }

    public void deleteUser(String userId) {
        FileManager fileManager = new FileManager();
        List<User> users = fileManager.readUsers();
        for (int i = users.size() - 1; i >= 0; i--) {
            if (users.get(i).getUserId().equals(userId)) {
                users.remove(i);
            }
        }
        fileManager.saveAllUsers(users);
    }

    public void viewAllRequest() { System.out.println(getFullName() + " is viewing all requests."); }
    public void viewUserHistory() { System.out.println(getFullName() + " is viewing a user's history."); }

    /**
     * Updates the status of any request (any assistance type), including
     * cancellation. The change is recorded in staff history under this
     * admin's ID so the audit trail keeps old/new status and timestamp.
     */
    public void updateRequestStatus(AssistanceRequest request, String newStatus) {
        if (request == null) throw new IllegalArgumentException("Request is required.");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required.");
        }
        request.updateStatus(newStatus);
        FileManager fileManager = new FileManager();
        fileManager.recordStaffRequestAction(
                new Staff(getFullName(), getPassword(), adminId, request.getType()),
                request, newStatus);
        new RequestManager(fileManager).updateRequest(request);
    }

    /** Cancels any request (any assistance type). Recorded in history. */
    public void cancelRequest(AssistanceRequest request) {
        updateRequestStatus(request, "Cancelled");
    }

    /**
     * Deletes any request (any assistance type).
     * Recorded in staff history first so the audit trail survives deletion.
     */
    public void deleteRequest(AssistanceRequest request) {
        if (request == null) throw new IllegalArgumentException("Request is required.");
        FileManager fileManager = new FileManager();
        fileManager.recordStaffRequestAction(
                new Staff(getFullName(), getPassword(), adminId, request.getType()),
                request, "Deleted");
        fileManager.deleteRequestFromFile(request.getRequestId());
    }

    /**
     * Files an assistance request on behalf of a resident (walk-in / phone-in).
     * The request must carry the resident's ID; the admin never files as themselves.
     */
    public void fileRequestFor(String residentId, AssistanceRequest request) {
        if (residentId == null || residentId.trim().isEmpty()) {
            throw new IllegalArgumentException("A resident must be selected.");
        }
        if (request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException("Request must belong to the selected resident.");
        }
        new RequestManager(new FileManager()).addRequest(request);
    }

    @Override
    public void showMenu() {
        new AdminMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|", "Admin", getFullName(), getPassword(), adminId);
    }
}
