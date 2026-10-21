package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicalRequest extends AssistanceRequest {

    private final List<MedicalItem> medicalItems;

    public MedicalRequest(String requestId, String residentId, String location, LocalDateTime timeNeeded, String additionalDetails, String status, LocalDateTime dateRequested, List<MedicalItem> medicalItems) {
        super(requestId, residentId, "Medical", location, timeNeeded, additionalDetails, status, dateRequested);
        this.medicalItems = new ArrayList<>(medicalItems);
    }

    public List<MedicalItem> getMedicalItems() {
        return Collections.unmodifiableList(medicalItems);
    }

    public void setMedicalItems(List<MedicalItem> items) {
        this.medicalItems.clear();
        this.medicalItems.addAll(items);
    }

    @Override
    public String toDisplaySummary() {
        return "[Medical] " + getRequestId() + " - " + getStatus() + " - " + medicalItems + " - Location: " + getLocation() + " - Needed: " + getTimeNeeded().format(DT_FMT) + " - " + getAdditionalDetails();
    }

    @Override
    public String toFileLine() {
        String line = commonFieldsForFile("MEDICAL") + "|" + medicalItems.size();
        
        for (MedicalItem item : medicalItems) {
            line += "|" + encodeField(item.getName())
                 + "|" + item.getQuantity()
                 + "|" + encodeField(item.getUnit());
        }
        
        return line + "|" + encodeAssigneeField();
    }
}