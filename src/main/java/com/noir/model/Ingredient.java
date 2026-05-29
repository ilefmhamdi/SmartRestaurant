package com.noir.model;

public class Ingredient {
    private String id;
    private String name;
    private Integer stock;
    private String unit;
    private Boolean available;
    private String notes;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
