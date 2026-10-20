package model;

import storage.FileManager;
import storage.RequestManager;
import ui.ResidentMenuFrame;

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

    @Override 
    public String getUserId() { return residentId; }

    // --- VALIDATION & LOGIC ---

    private void checkOwnership(AssistanceRequest request, String errorMessage) {
        if (request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public void submitRequest(AssistanceRequest request) {
        checkOwnership(request, "Request must belong to this resident.");
        
        // Notice: I also added the missing contact validation here just in case!
        String error = validateContactDetails();
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        RequestManager rm = new RequestManager(new FileManager());
        if (rm.findRequest(request.getRequestId()) != null) {
            throw new IllegalArgumentException("Request ID " + request.getRequestId() + " already exists.");
        }
        
        rm.addRequest(request);
    }

    public void cancelRequest(AssistanceRequest request) {
        checkOwnership(request, "Can only cancel your own requests.");

        if (!"Pending".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Only pending requests can be cancelled.");
        }

        request.updateStatus("Cancelled");
        
        FileManager fileManager = new FileManager();
        fileManager.recordResidentRequestAction(this, request, "Cancelled");
        new RequestManager(fileManager).updateRequest(request);
    }

    public void viewMyRequest() {
        System.out.println(getFullName() + " is viewing their requests."); 
    }

    // --- UI & SAVING ---

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