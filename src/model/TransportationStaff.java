package model;

import ui.StaffMenuFrame;

public class TransportationStaff extends Staff {

    public TransportationStaff(String name, String password, String staffId) {
        super(name, password, "TransportationStaff", staffId, "Transportation");
    }

    @Override
    public void showMenu() {
        new StaffMenuFrame(this).setVisible(true);
    }

      @Override
    public String toFileLine() {
        return String.join("|", "FoodStaff", AssistanceRequest.encodeField(getFullName()),
                AssistanceRequest.encodeField(getPassword()), AssistanceRequest.encodeField(getStaffId()),
                AssistanceRequest.encodeField(getAssignedType()),
                AssistanceRequest.encodeField(getAddress()), AssistanceRequest.encodeField(getContactNo()));
    }
}
