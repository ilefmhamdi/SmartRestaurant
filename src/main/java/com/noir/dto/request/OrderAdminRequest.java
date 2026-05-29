package com.noir.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class OrderAdminRequest {
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    @NotBlank private String status;
    private String eta;
    private String deliveryAddress;
    private String deliveryTime;
    private String contactPhone;
    private String notes;
    private String kitchenNotes;
    private String chefId;
    private String chefName;
    private String kitchenStaffId;
    private String kitchenStaffName;
    private String deliveryStaffId;
    private String deliveryStaffName;
    private String undeliveredReason;
    private String rejectedReason;
    @NotNull @Min(0) private Double total;
    private List<OrderItemPayload> items = new ArrayList<>();
    private List<OrderStepPayload> steps = new ArrayList<>();

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getKitchenNotes() { return kitchenNotes; }
    public void setKitchenNotes(String kitchenNotes) { this.kitchenNotes = kitchenNotes; }
    public String getChefId() { return chefId; }
    public void setChefId(String chefId) { this.chefId = chefId; }
    public String getChefName() { return chefName; }
    public void setChefName(String chefName) { this.chefName = chefName; }
    public String getKitchenStaffId() { return kitchenStaffId; }
    public void setKitchenStaffId(String kitchenStaffId) { this.kitchenStaffId = kitchenStaffId; }
    public String getKitchenStaffName() { return kitchenStaffName; }
    public void setKitchenStaffName(String kitchenStaffName) { this.kitchenStaffName = kitchenStaffName; }
    public String getDeliveryStaffId() { return deliveryStaffId; }
    public void setDeliveryStaffId(String deliveryStaffId) { this.deliveryStaffId = deliveryStaffId; }
    public String getDeliveryStaffName() { return deliveryStaffName; }
    public void setDeliveryStaffName(String deliveryStaffName) { this.deliveryStaffName = deliveryStaffName; }
    public String getUndeliveredReason() { return undeliveredReason; }
    public void setUndeliveredReason(String undeliveredReason) { this.undeliveredReason = undeliveredReason; }
    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public List<OrderItemPayload> getItems() { return items; }
    public void setItems(List<OrderItemPayload> items) { this.items = items; }
    public List<OrderStepPayload> getSteps() { return steps; }
    public void setSteps(List<OrderStepPayload> steps) { this.steps = steps; }

    public static class OrderItemPayload {
        private String name;
        @Min(1) private Integer quantity;
        @Min(0) private Double price;
        private String image;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
    }

    public static class OrderStepPayload {
        private String label;
        private String time;
        private String state;
        private String icon;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }
}
