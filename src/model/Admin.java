
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

    // Create a new user
    public void createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        FileManager fm = new FileManager();

        for (User existing : fm.readUsers()) {
            if (existing.getUserId().equalsIgnoreCase(user.getUserId())) {
                throw new IllegalArgumentException("User ID already exists.");
            }
        }

        fm.saveUser(user);
    }

    // Delete a user
    public void deleteUser(String userId) {
        FileManager fm = new FileManager();
        List<User> users = fm.readUsers();

        users.removeIf(user -> user.getUserId().equals(userId));

        fm.saveAllUsers(users);
    }

    // File a request for a resident
    public void fileRequestFor(String residentId, AssistanceRequest request) {
        if (residentId == null || residentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Select a resident.");
        }

        if (request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException("Request does not match the resident.");
        }

        RequestManager rm = new RequestManager(new FileManager());

        if (rm.findRequest(request.getRequestId()) != null) {
            throw new IllegalArgumentException("Request ID already exists.");
        }

        rm.addRequest(request);
    }

    public void manageUser(String userId) {
    System.out.println(getFullName()
            + " is managing user " + userId + ".");
}

public void viewAllRequest() {
    System.out.println(getFullName()
            + " is viewing all requests.");
}

public void viewUserHistory() {
    System.out.println(getFullName()
            + " is viewing a user's history.");
}

    // Update a request's status
    public void updateRequestStatus(AssistanceRequest request, String status) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required.");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required.");
        }

        request.updateStatus(status);
        new RequestManager(new FileManager()).updateRequest(request);
    }

    // Delete a request
    public void deleteRequest(AssistanceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required.");
        }

        new FileManager().deleteRequestFromFile(request.getRequestId());
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
                AssistanceRequest.encodeField(adminId));
    }
}
