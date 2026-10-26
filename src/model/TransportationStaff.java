package model;

import ui.StaffMenuFrame;

public class TransportationStaff extends Staff {

    public TransportationStaff(String fullName, String password, String staffId) {
        super(fullName, password, "TransportationStaff", staffId, "Transportation");
    }

    @Override
    public void showMenu() {
        new StaffMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return "TransportationStaff|" 
                + AssistanceRequest.encodeField(getFullName()) + "|"
                + AssistanceRequest.encodeField(getPassword()) + "|"
                + AssistanceRequest.encodeField(getStaffId()) + "|"
                + AssistanceRequest.encodeField(getAssignedType());
    }
}