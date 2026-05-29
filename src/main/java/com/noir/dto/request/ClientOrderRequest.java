package com.noir.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public class ClientOrderRequest {
    @NotBlank @Size(min = 2) private String orderName;
    @NotNull @Size(min = 1) private List<OrderItemRef> items;
    @NotBlank @Size(min = 5) private String deliveryAddress;
    @NotBlank private String deliveryTime;
    @NotBlank @Size(min = 8) private String contactPhone;
    private String notes = "";

    public String getOrderName() { return orderName; }
    public void setOrderName(String orderName) { this.orderName = orderName; }
    public List<OrderItemRef> getItems() { return items; }
    public void setItems(List<OrderItemRef> items) { this.items = items; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static class OrderItemRef {
        @Min(1) private int id;
        @Min(1) private int quantity;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}