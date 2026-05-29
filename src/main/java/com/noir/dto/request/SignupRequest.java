package com.noir.dto.request;

import jakarta.validation.constraints.*;

public class SignupRequest {
    @NotBlank @Size(min = 2) private String firstName;
    @NotBlank @Size(min = 2) private String lastName;
    @Email @NotBlank private String email;
    @Size(min = 8) @NotBlank private String password;
    private String phone;
    private String birthDate;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
}
