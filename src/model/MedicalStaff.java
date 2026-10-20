package model;

import ui.StaffMenuFrame;

public class MedicalStaff extends Staff {
    public MedicalStaff(String name, String password, String staffId) {
        super(name, password, "MedicalStaff", staffId, "Medical");
    }

    @Override
    public void showMenu() {
        new StaffMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|", "MedicalStaff", AssistanceRequest.encodeField(getFullName()),
                AssistanceRequest.encodeField(getPassword()), AssistanceRequest.encodeField(getStaffId()),
                AssistanceRequest.encodeField(getAssignedType()),
                AssistanceRequest.encodeField(getAddress()), AssistanceRequest.encodeField(getContactNo()));
    }
}