package com.noir.dto.request;

import jakarta.validation.constraints.*;

public class NewsletterRequest {
    @Email @NotBlank private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}