package com.noir.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IngredientRequest {
    @NotBlank private String name;
    @NotNull @Min(0) private Integer stock;
    @NotBlank private String unit;
    @NotNull private Boolean available;
    private String notes;

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
