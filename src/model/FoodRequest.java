package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FoodRequest extends AssistanceRequest {
    private final List<FoodItem> foodItems;

    public FoodRequest(String requestId, String residentId, String location, LocalDateTime timeNeeded, String additionalDetails, String status, LocalDateTime dateRequested, List<FoodItem> foodItems) {
        super(requestId, residentId, "Food", location, timeNeeded, additionalDetails, status, dateRequested);
        this.foodItems = new ArrayList<>(foodItems);
    }

    public List<FoodItem> getFoodItems() {
        return Collections.unmodifiableList(foodItems);
    }

    public void setFoodItems(List<FoodItem> items) {
        this.foodItems.clear();
        this.foodItems.addAll(items);
    }

    @Override
    public String toDisplaySummary() {
        return "[Food] " + getRequestId() + " - " + getStatus() + " - " + foodItems + " - Location: " + getLocation() + " - Needed: " + getTimeNeeded().format(DT_FMT) + " - " + getAdditionalDetails();
    }

    @Override
    public String toFileLine() {
        String line = commonFieldsForFile("FOOD") + "|" + foodItems.size();
    
        for (FoodItem item : foodItems) {
            line += "|" + encodeField(item.getName())
                 + "|" + item.getQuantity()
                 + "|" + encodeField(item.getUnit());
        }
        
        return line + "|" + encodeAssigneeField();
    }
}