package model;

import storage.FileManager;
import storage.RequestManager;
import ui.StaffMenuFrame;

public class Staff extends User {
    private String staffId;
    private String assignedType;

    public Staff(String name, String password, String role, String staffId, String assignedType) {
        super(name, password, role);
        this.staffId = staffId;
        this.assignedType = (assignedType == null) ? "" : assignedType;
    }

    public String getStaffId() { 
        return staffId; 
    }

    public void setStaffId(String staffId) { 
        this.staffId = staffId; 
    }

    public String getAssignedType() { 
        return assignedType; 
    }

    public void setAssignedType(String assignedType) { 
        this.assignedType = (assignedType == null) ? "" : assignedType; 
    }

    @Override
    public String getUserId() { 
        return staffId; 
    }

    public boolean canHandle(AssistanceRequest request) {
        return request != null && assignedType.equalsIgnoreCase(request.getType());
    }

    public void claimRequest(AssistanceRequest request) {
        if (!canHandle(request)) {
            throw new IllegalArgumentException("You cannot handle this request type.");
        }
        if (!"Pending".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Only Pending requests can be claimed.");
        }
        if (request.isAssigned() && !staffId.equals(request.getAssignedStaffId())) {
            throw new IllegalStateException("This request is already assigned to another staff member.");
        }

        request.setAssignedStaffId(staffId);
        saveAndRecord(request, "Claimed");
    }

    public void updateRequestStatus(AssistanceRequest request, String newStatus) {
        if (!Admin.isOpen(request)) {
            throw new IllegalStateException("Completed or Cancelled requests cannot be updated.");
        }

        request.updateStatus(newStatus);
        saveAndRecord(request, newStatus);
    }

    private void saveAndRecord(AssistanceRequest request, String action) {
        FileManager fm = new FileManager();
        new RequestManager(fm).updateRequest(request);
        fm.recordStaffRequestAction(this, request, action);
    }

    public void viewRequests() {
        System.out.println(getFullName() + " is viewing requests.");
    }

    public void searchRequest() {
        System.out.println(getFullName() + " is searching for a request.");
    }

    @Override
    public void showMenu() {
        new StaffMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return "Staff|" 
                + AssistanceRequest.encodeField(getFullName()) + "|"
                + AssistanceRequest.encodeField(getPassword()) + "|"
                + AssistanceRequest.encodeField(staffId) + "|"
                + AssistanceRequest.encodeField(assignedType);
    }
}