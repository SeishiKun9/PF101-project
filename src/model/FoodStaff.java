package model;

import ui.StaffMenuFrame;

public class FoodStaff extends Staff {

    public FoodStaff(String name, String password, String staffId) {
        super(name, password, "FoodStaff", staffId, "Food");
    }

    @Override
    public void showMenu() {
        new StaffMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return "FoodStaff|" 
                + AssistanceRequest.encodeField(getFullName()) + "|"
                + AssistanceRequest.encodeField(getPassword()) + "|"
                + AssistanceRequest.encodeField(getStaffId()) + "|"
                + AssistanceRequest.encodeField(getAssignedType());
    }
}