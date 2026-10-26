package model;

import storage.FileManager;
import storage.RequestManager;
import ui.ResidentMenuFrame;

public class Resident extends User {
    private String residentId;
    private String address;
    private String contactNo;

    public Resident(String fullName, String password, String residentId, String address, String contactNo) {
        super(fullName, password, "Resident");
        this.residentId = residentId;
        setAddress(address);
        setContactNo(contactNo);
    }

    public String getResidentId() {
         return residentId; 
        }

    public void setResidentId(String residentId) {
         this.residentId = residentId;
         }

    public String getAddress() {
         return address; 
        }

    public void setAddress(String address) { 
        this.address = (address == null) ? "" : address.trim(); 
    }

    public String getContactNo() {
         return contactNo; 
        }

    public void setContactNo(String contactNo) { 
        this.contactNo = (contactNo == null) ? "" : contactNo.trim(); 
    }

    @Override 
    public String getUserId() {
         return residentId; 
        }

<<<<<<< HEAD
  
=======
    // Validation for Resident details
>>>>>>> origin/main
    public String validateDetails() {
        if (address.isEmpty()) {
            return "Address is required for residents.";
        }
        if (contactNo.isEmpty()) {
            return "Contact number is required for residents.";
        }
        if (!contactNo.matches("\\+?[0-9]+")) {
            return "Contact number must be numeric.";
        }
        return null;
    }

    public void submitRequest(AssistanceRequest request) {
        if (request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException("Request must belong to this resident.");
        }

        String error = validateDetails();
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

    @Override
    public void showMenu() {
        new ResidentMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return "Resident|" 
                + AssistanceRequest.encodeField(getFullName()) + "|"
                + AssistanceRequest.encodeField(getPassword()) + "|"
                + AssistanceRequest.encodeField(residentId) + "|"
                + AssistanceRequest.encodeField(address) + "|"
                + AssistanceRequest.encodeField(contactNo);
    }
}