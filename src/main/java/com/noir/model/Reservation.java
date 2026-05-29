package com.noir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reservation {
    private String id;
    private String clientId;
    private String confirmationCode;
    private String status;
    private String createdAt;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String date;
    private Integer guests;
    private String time;
    private String experience;
    private List<String> menuSelections;
    private List<String> allergySelections;
    private String specialRequests;
    private String notes;
    private String tableId;
    private Integer tableSeats;
    private String rejectedReason;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Integer getGuests() { return guests; }
    public void setGuests(Integer guests) { this.guests = guests; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public List<String> getMenuSelections() { return menuSelections; }
    public void setMenuSelections(List<String> menuSelections) { this.menuSelections = menuSelections; }
    public List<String> getAllergySelections() { return allergySelections; }
    public void setAllergySelections(List<String> allergySelections) { this.allergySelections = allergySelections; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }
    public Integer getTableSeats() { return tableSeats; }
    public void setTableSeats(Integer tableSeats) { this.tableSeats = tableSeats; }
    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }
}