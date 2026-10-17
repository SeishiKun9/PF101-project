package model;

import storage.FileManager;
import storage.RequestManager;
import ui.StaffMenuFrame;

/**
 * Staff: base class for barangay personnel who process assistance requests
 * of a specific assigned type (Medical, Food, Transportation).
 */
public class Staff extends User {
    private static final long serialVersionUID = 1L;
    private String staffId;
    private String assignedType;

    public Staff(String name, String password, String staffId, String assignedType) {
        this(name, password, "Staff", staffId, assignedType);
    }

    // Protected-style constructor used by subclasses to set their own role label.
    protected Staff(String name, String password, String role, String staffId, String assignedType) {
        super(name, password, role);
        this.staffId = staffId;
        this.assignedType = assignedType;
    }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    @Override public String getUserId() { return staffId; }
    public String getAssignedType() { return assignedType; }
    public void setAssignedType(String assignedType) { this.assignedType = assignedType; }

    // Conceptual entry points; full logic lives in StaffMenuFrame + FileManager.
    public void viewRequests() { System.out.println(getFullName() + " is viewing requests."); }
    public void searchRequest() { System.out.println(getFullName() + " is searching a request."); }

    /** True when this staff member is allowed to touch the given request (same assigned type). */
    public boolean canHandle(AssistanceRequest request) {
        return request != null && assignedType != null
                && assignedType.equalsIgnoreCase(request.getType());
    }

    private void requireSameType(AssistanceRequest request) {
        if (!canHandle(request)) {
            throw new IllegalArgumentException(
                    "Staff (" + assignedType + ") cannot process "
                            + (request == null ? "null" : request.getType()) + " requests.");
        }
    }

    /**
     * Files an assistance request on behalf of a resident.
     * The request is stored under the resident's ID and must match this
     * staff member's assigned assistance type.
     */
    public void fileRequestFor(String residentId, AssistanceRequest request) {
        if (residentId == null || residentId.trim().isEmpty()) {
            throw new IllegalArgumentException("A resident must be selected.");
        }
        if (request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException("Request must belong to the selected resident.");
        }
        requireSameType(request);
        FileManager fileManager = new FileManager();
        new RequestManager(fileManager).addRequest(request);
        fileManager.recordStaffRequestAction(this, request, "Filed");
    }

    /** Updates the status of a request of the staff member's own assigned type. */
    public void updateRequestStatus(AssistanceRequest request, String newStatus) {
        requireSameType(request);
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required.");
        }
        request.updateStatus(newStatus);
        FileManager fileManager = new FileManager();
        fileManager.recordStaffRequestAction(this, request, newStatus);
        new RequestManager(fileManager).updateRequest(request);
    }

    /**
     * Deletes a request of the staff member's own assigned type.
     * The deletion is recorded in staff history first so the audit trail survives.
     */
    public void deleteRequest(AssistanceRequest request) {
        requireSameType(request);
        FileManager fileManager = new FileManager();
        fileManager.recordStaffRequestAction(this, request, "Deleted");
        fileManager.deleteRequestFromFile(request.getRequestId());
    }

    @Override
    public void showMenu() {
        new StaffMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|", "Staff", getFullName(), getPassword(), staffId, assignedType);
    }
}
