package com.noir.dto.request;

import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class ClientReservationRequest {
    @NotBlank @Size(min = 8) private String phone;
    @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") private String date;
    @Min(1) @Max(12) private int guests;
    @NotBlank private String time;
    @NotBlank private String experience;
    private List<String> menuSelections = new ArrayList<>();
    private List<String> allergySelections = new ArrayList<>();
    private String specialRequests = "";

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }
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
}