package com.noir.dto.request;

import jakarta.validation.constraints.*;

public class ReviewRequest {
    @NotBlank @Size(min = 2) private String name;
    @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") private String date;
    @Min(1) @Max(5) private int rating;
    @NotBlank @Size(min = 10, max = 500) private String text;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}