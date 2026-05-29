package com.noir.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public class MenuItemRequest {
    @NotBlank @Size(min = 2) private String name;
    @NotNull private String cat;
    @NotBlank @Size(min = 3) private String image;
    @Positive private double price;
    @NotBlank @Size(min = 10) private String desc;
    private List<String> allergens = List.of();
    @Min(0) @Max(5) private double rating;
    @Min(0) private int reviews;
    private List<String> badges = List.of();
    private Boolean veg;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCat() { return cat; }
    public void setCat(String cat) { this.cat = cat; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public List<String> getAllergens() { return allergens; }
    public void setAllergens(List<String> allergens) { this.allergens = allergens; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getReviews() { return reviews; }
    public void setReviews(int reviews) { this.reviews = reviews; }
    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }
    public Boolean getVeg() { return veg; }
    public void setVeg(Boolean veg) { this.veg = veg; }
}