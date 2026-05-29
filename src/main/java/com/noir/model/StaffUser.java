package com.noir.model;

/**
 * Represents a kitchen or delivery staff member.
 * role: "chef" | "kitchen" | "delivery"
 * - chef: can do everything kitchen does + add/recommend special plates
 * - kitchen: can view orders/reservations and update order status (being_prepared, ready)
 * - delivery: can view ready orders and mark out_for_delivery / delivered
 */
public class StaffUser {
    private String id;
    private String username;
    private String role;          // chef | kitchen | delivery
    private String salt;
    private String passwordHash;
    private String createdAt;
    private String lastLoginAt;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String birthDate;
    private String profilePicture;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}
