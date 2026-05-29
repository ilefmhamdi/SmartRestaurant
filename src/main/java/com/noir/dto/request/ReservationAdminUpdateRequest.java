package com.noir.dto.request;

public class ReservationAdminUpdateRequest {
    private String status;
    private String notes;
    private String tableId;
    private Integer tableSeats;
    private String rejectedReason;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }
    public Integer getTableSeats() { return tableSeats; }
    public void setTableSeats(Integer tableSeats) { this.tableSeats = tableSeats; }
    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }
}