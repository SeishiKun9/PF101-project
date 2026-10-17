package model;

import storage.FileManager;
import storage.RequestManager;
import ui.ResidentMenuFrame;

/**
 * Resident: a barangay constituent who can submit requests, view only their
 * own requests/history, and cancel only their own pending requests.
 * Residents cannot update request details/status or delete requests.
 */
public class Resident extends User {
    private static final long serialVersionUID = 1L;
    private String residentId;
    private String address;
    private String contactNo;

    public Resident(String fullName, String password, String residentId, String address, String contactNo) {
        super(fullName, password, "Resident");
        this.residentId = residentId;
        this.address = address;
        this.contactNo = contactNo;
    }

    public String getResidentId() { return residentId; }
    public void setResidentId(String residentId) { this.residentId = residentId; }
    @Override public String getUserId() { return residentId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public void submitRequest(AssistanceRequest request) {
        if (request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException("Request must belong to this resident.");
        }
        new RequestManager(new FileManager()).addRequest(request);
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
        return String.join("|", "Resident", getFullName(), getPassword(), residentId, address, contactNo);
    }
}
