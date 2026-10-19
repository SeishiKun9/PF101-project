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
        this.assignedType = assignedType;
    }

    public String getStaffId() {
         return staffId; }

    public void setStaffId(String staffId) {
         this.staffId = staffId; }
    
    public String getAssignedType() {
         return assignedType; }

    public void setAssignedType(String assignedType) {
         this.assignedType = assignedType; }

    @Override
    public String getUserId() {
         return staffId; }

    public boolean canHandle(AssistanceRequest request) {
        return request != null && assignedType != null && assignedType.equalsIgnoreCase(request.getType());
    }

    private void checkRequestType(AssistanceRequest request) {
        if (!canHandle(request)) {
            throw new IllegalArgumentException("You cannot handle this request type.");
        }
    }

    private void checkOwnership(AssistanceRequest request) {
        if (request == null || !staffId.equals(request.getAssignedStaffId())) {
            throw new IllegalStateException("Claim the request first.");
        }
    }

    private void checkUpdatable(AssistanceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required.");
        }
        String status = request.getStatus();
        if ("Completed".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Completed or Cancelled requests cannot be updated.");
        }
    }


    public void fileRequestFor(String residentId, AssistanceRequest request) {
        if (residentId == null || residentId.trim().isEmpty() || request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException("A valid resident and matching request are required.");
        }

        checkRequestType(request);

        FileManager fm = new FileManager();
        RequestManager rm = new RequestManager(fm);

        if (rm.findRequest(request.getRequestId()) != null) {
            throw new IllegalArgumentException("Request ID already exists.");
        }

        rm.addRequest(request);
        fm.recordStaffRequestAction(this, request, "Filed");
    }

    public void claimRequest(AssistanceRequest request) {
        checkRequestType(request);

        if (!"Pending".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Only Pending requests can be claimed.");
        }

        if (request.isAssigned() && !staffId.equals(request.getAssignedStaffId())) {
            throw new IllegalStateException("This request is already assigned to another staff member.");
        }

        if (staffId.equals(request.getAssignedStaffId())) {
            return;
        }

        request.setAssignedStaffId(staffId);
        saveAndRecord(request, "Claimed");
    }

    public void updateRequestStatus(AssistanceRequest request, String newStatus) {
        checkRequestType(request);
        checkOwnership(request);
        checkUpdatable(request);

        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required.");
        }
        
        request.updateStatus(newStatus);
        saveAndRecord(request, newStatus);
    }

    public void releaseRequest(AssistanceRequest request) {
        checkRequestType(request);
        checkOwnership(request);
        
        request.setAssignedStaffId(null);
        saveAndRecord(request, "Released");
    }

    public void deleteRequest(AssistanceRequest request) {
        checkRequestType(request);
        FileManager fm = new FileManager();
        fm.recordStaffRequestAction(this, request, "Deleted");
        fm.deleteRequestFromFile(request.getRequestId());
    }

    private void saveAndRecord(AssistanceRequest request, String actionName) {
        FileManager fm = new FileManager();
        fm.recordStaffRequestAction(this, request, actionName);
        new RequestManager(fm).updateRequest(request);
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
        return String.join("|",
                "Staff",
                AssistanceRequest.encodeField(getFullName()),
                AssistanceRequest.encodeField(getPassword()),
                AssistanceRequest.encodeField(staffId),
                AssistanceRequest.encodeField(assignedType == null ? "" : assignedType),
                AssistanceRequest.encodeField(getAddress()),
                AssistanceRequest.encodeField(getContactNo()));
    }
}