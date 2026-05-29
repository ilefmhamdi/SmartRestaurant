package com.noir.dto.request;

import jakarta.validation.constraints.*;

public class ChangePasswordRequest {
    @NotBlank private String oldPassword;
    @NotBlank @Size(min = 8) private String newPassword;

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}