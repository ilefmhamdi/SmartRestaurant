package com.noir.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItem {
    private Integer id;
    private String name;
    private String cat;
    private Double price;
    private String desc;
    private List<String> allergens;
    private Double rating;
    private Integer reviews;
    private List<String> badges;
    private String image;
    private Boolean veg;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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
}
