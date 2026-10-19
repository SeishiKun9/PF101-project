package model;

import storage.FileManager;
import storage.RequestManager;
import ui.ResidentMenuFrame;

/**
 * Resident: a barangay constituent who can submit requests, view only their
 * own requests/history, and cancel only their own pending requests.
 * Residents cannot update request details/status or delete requests.
 * Address and contact number are stored in User.
 */
public class Resident extends User {

    private String residentId;

     public Resident(String fullName, String password, String residentId, String address, String contactNo) {
       super(fullName, password, "Resident");
       this.residentId = residentId;
       setAddress(address);
       setContactNo(contactNo);
   }

    public String getResidentId() { return residentId; }
    public void setResidentId(String residentId) { this.residentId = residentId; }
    @Override public String getUserId() { return residentId; }

    /**
     * Validates that a Resident record is complete. Returns null when valid,
     * or the first validation error message found.
     */
    public String validate() {
        if (getAddress() == null || getAddress().trim().isEmpty()) {
            return "Resident address is required.";
        }
        if (getContactNo() == null || getContactNo().trim().isEmpty()) {
            return "Contact number is required.";
        }
        if (!getContactNo().matches("\\+?[0-9]+")) {
            return "Contact number must be numeric.";
        }
        return null;
    }

    public void submitRequest(AssistanceRequest request) {
        if (request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException("Request must belong to this resident.");
        }
        RequestManager rm = new RequestManager(new FileManager());
        if (rm.findRequest(request.getRequestId()) != null) {
            throw new IllegalArgumentException("Request ID " + request.getRequestId() + " already exists.");
        }
        rm.addRequest(request);
    }

    public void viewMyRequest() { System.out.println(getFullName() + " is viewing their requests."); }

    /**
     * Cancels one of this resident's own requests. Only pending requests may
     * be cancelled; anything else is rejected so status rules hold even for
     * direct method calls (the UI cancel button enforces the same rule).
     */
    public void cancelRequest(AssistanceRequest request) {
        if (request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException("Can only cancel your own requests.");
        }
        if (!"Pending".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Only pending requests can be cancelled.");
        }
        request.updateStatus("Cancelled");
        FileManager fileManager = new FileManager();
        fileManager.recordResidentRequestAction(this, request, "Cancelled");
        new RequestManager(fileManager).updateRequest(request);
    }

    public void cancelRequest() { System.out.println(getFullName() + " is cancelling a request."); }

    @Override
    public void showMenu() {
        new ResidentMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|", "Resident", AssistanceRequest.encodeField(getFullName()),
                AssistanceRequest.encodeField(getPassword()), AssistanceRequest.encodeField(residentId),
                AssistanceRequest.encodeField(getAddress()),
                AssistanceRequest.encodeField(getContactNo()));
    }
}