package com.noir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * A special plate added by a chef — can be available for a date range
 * and/or on specific days of the week.
 *
 * availableDays: list of day names e.g. ["Monday", "Sunday"]
 *               if null/empty → available every day within the date range
 * availableFrom / availableTo: ISO date strings "YYYY-MM-DD"
 *               if both null → always available while active
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpecialPlate {
    private String id;
    private String name;
    private String cat;            // starters | mains | desserts | drinks
    private Double price;
    private String desc;
    private List<String> allergens;
    private Double rating;
    private Integer reviews;
    private List<String> badges;
    private String image;
    private Boolean veg;
    private String availableFrom;  // YYYY-MM-DD or null
    private String availableTo;    // YYYY-MM-DD or null
    private List<String> availableDays; // ["Monday","Sunday"] or null = all days
    private String recommendedBy;  // staff username who created it
    private Boolean recommended;   // chef recommendation flag
    private String createdAt;
    private String updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCat() { return cat; }
    public void setCat(String cat) { this.cat = cat; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public List<String> getAllergens() { return allergens; }
    public void setAllergens(List<String> allergens) { this.allergens = allergens; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getReviews() { return reviews; }
    public void setReviews(Integer reviews) { this.reviews = reviews; }
    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Boolean getVeg() { return veg; }
    public void setVeg(Boolean veg) { this.veg = veg; }
    public String getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(String availableFrom) { this.availableFrom = availableFrom; }
    public String getAvailableTo() { return availableTo; }
    public void setAvailableTo(String availableTo) { this.availableTo = availableTo; }
    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }
    public String getRecommendedBy() { return recommendedBy; }
    public void setRecommendedBy(String recommendedBy) { this.recommendedBy = recommendedBy; }
    public Boolean getRecommended() { return recommended; }
    public void setRecommended(Boolean recommended) { this.recommended = recommended; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
