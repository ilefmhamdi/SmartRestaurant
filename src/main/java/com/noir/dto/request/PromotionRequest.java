package com.noir.dto.request;

import jakarta.validation.constraints.*;

public class PromotionRequest {
    @NotBlank @Size(min = 2) private String title;
    @NotBlank @Size(min = 2) private String code;
    @NotBlank private String discount;
    @NotBlank @Size(min = 10) private String description;
    private String status = "active";
    private String startsAt = "";
    private String endsAt = "";

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStartsAt() { return startsAt; }
    public void setStartsAt(String startsAt) { this.startsAt = startsAt; }
    public String getEndsAt() { return endsAt; }
    public void setEndsAt(String endsAt) { this.endsAt = endsAt; }
}