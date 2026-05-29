package com.noir.dto.request;

import jakarta.validation.constraints.NotBlank;

public class OrderStatusUpdateRequest {
    @NotBlank private String status;
    private String kitchenNotes;  // optional notes from kitchen staff

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getKitchenNotes() { return kitchenNotes; }
    public void setKitchenNotes(String kitchenNotes) { this.kitchenNotes = kitchenNotes; }
}
