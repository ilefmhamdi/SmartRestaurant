package com.noir.model;

public class TableInfo {
    private String id;
    private Integer seats;
    private Boolean available;
    private String location;
    private String notes;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
