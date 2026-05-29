package com.noir.dto.request;

import jakarta.validation.constraints.*;

public class UpdateProfileRequest {
    @NotBlank @Size(min = 2) private String firstName;
    @NotBlank @Size(min = 2) private String lastName;
    @Email @NotBlank private String email;
    private String profilePicture;
    private Boolean isPro;
    private String phone;
    private String birthDate;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public Boolean getIsPro() { return isPro; }
    public void setIsPro(Boolean isPro) { this.isPro = isPro; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
}
