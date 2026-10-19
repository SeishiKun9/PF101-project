package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AssistanceRequest {

    public static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private String requestId;
    private String residentId;
    private String type;
    private String location;
    private LocalDateTime timeNeeded;
    private String additionalDetails;
    private String status;
    private LocalDateTime dateRequested;
    private String assignedStaffId;

    public AssistanceRequest(String requestId, String residentId, String type, String location,
                             LocalDateTime timeNeeded, String additionalDetails, String status,
                             LocalDateTime dateRequested) {
        this.requestId = requestId;
        this.residentId = residentId;
        this.type = type;
        this.location = location;
        this.timeNeeded = timeNeeded;
        this.additionalDetails = additionalDetails;
        this.status = status;
        this.dateRequested = dateRequested;
    }

    // --- GETTERS & SETTERS ---
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getResidentId() { return residentId; }
    public void setResidentId(String residentId) { this.residentId = residentId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getTimeNeeded() { return timeNeeded; }
    public void setTimeNeeded(LocalDateTime timeNeeded) { this.timeNeeded = timeNeeded; }

    public String getAdditionalDetails() { return additionalDetails; }
    public void setAdditionalDetails(String additionalDetails) { this.additionalDetails = additionalDetails; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDateRequested() { return dateRequested; }
    public void setDateRequested(LocalDateTime dateRequested) { this.dateRequested = dateRequested; }

    public String getAssignedStaffId() { return assignedStaffId; }
    public void setAssignedStaffId(String assignedStaffId) { this.assignedStaffId = assignedStaffId; }

    // --- BUSINESS LOGIC ---
    
    public boolean isAssigned() {
        return assignedStaffId != null && !assignedStaffId.trim().isEmpty();
    }

    public void updateStatus(String newStatus) { 
        this.status = newStatus; 
    }

    public void displayDetails() {
        System.out.println(toDisplaySummary());
    }

    public String toDisplaySummary() {
        return "[" + type + "] " + requestId + " - " + status + " - " + additionalDetails;
    }

    // --- FILE SAVING HELPERS ---

    /** Fields shared by every request type, in file order. */
    protected String commonFieldsForFile(String recordType) {
        return String.join("|", 
                "V2", 
                encodeField(recordType), 
                encodeField(requestId),
                encodeField(residentId), 
                encodeField(type), 
                encodeField(location),
                timeNeeded.format(DT_FMT), 
                encodeField(additionalDetails), 
                encodeField(status),
                dateRequested.format(DT_FMT));
    }

    protected String encodeAssigneeField() {
        return assignedStaffId == null ? "" : encodeField(assignedStaffId);
    }

    /** Escapes characters that would break the pipe-separated file format. */
    public static String encodeField(String value) {
        if (value == null) return "";
        return value.replace("%", "%25").replace("|", "%7C")
                .replace("\r", "%0D").replace("\n", "%0A");
    }

    /** Reverses encodeField. */
    public static String decodeField(String value) {
        if (value == null) return "";
        return value.replace("%7C", "|").replace("%0D", "\r")
                .replace("%0A", "\n").replace("%25", "%");
    }

    public abstract String toFileLine();
}