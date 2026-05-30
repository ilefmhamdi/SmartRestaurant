package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.dto.request.UpdateAdminProfileRequest;
import com.noir.exception.AppException;
import com.noir.model.AdminProfile;
import com.noir.repository.JsonRepository;
import com.noir.security.TokenService;
import org.springframework.stereotype.Service;
import com.noir.dto.request.ChangeAdminPasswordRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AdminAuthService {

    private final AppConfig appConfig;
    private final TokenService tokenService;
    private final JsonRepository<AdminProfile> adminRepo;

    public AdminAuthService(AppConfig appConfig, TokenService tokenService) {
        this.appConfig = appConfig;
        this.tokenService = tokenService;
        this.adminRepo = new JsonRepository<>(appConfig.getDataDir() + "/admin.json", AdminProfile.class);
    }

    public Map<String, Object> authenticate(com.noir.dto.request.AdminLoginRequest req) {
        AppConfig.Admin admin = appConfig.getAdmin();
        
        if (req.getUsername() == null || !req.getUsername().equals(admin.getUsername())) {
            throw new AppException("Invalid username or password", 401);
        }
        if (req.getPassword() == null || !req.getPassword().equals(admin.getPassword())) {
            throw new AppException("Invalid username or password", 401);
        }
        
        long exp = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 7);
        Map<String, Object> payload = Map.of("sub", "admin", "role", "admin", "exp", exp);
        String token = tokenService.sign(payload, admin.getTokenSecret());
        
        return Map.of("token", token, "username", admin.getUsername(), "loginAt", Instant.now().toString());
    }

    public AdminProfile getAdminProfile() {
        List<AdminProfile> profiles = adminRepo.read();
        if (profiles.isEmpty()) {
            AdminProfile profile = createDefaultAdminProfile();
            adminRepo.write(List.of(profile));
            return profile;
        }
        return profiles.get(0);
    }

    public AdminProfile updateAdminProfile(UpdateAdminProfileRequest req) {
        List<AdminProfile> profiles = adminRepo.read();
        AdminProfile profile = profiles.isEmpty() ? createDefaultAdminProfile() : profiles.get(0);
        profile.setFirstName(req.getFirstName());
        profile.setLastName(req.getLastName());
        profile.setEmail(req.getEmail());
        profile.setPhone(req.getPhone());
        profile.setBirthDate(req.getBirthDate());
        profile.setProfilePicture(req.getProfilePicture());
        if (profiles.isEmpty()) profiles.add(profile);
        adminRepo.write(profiles);
        return profile;
    }

    private AdminProfile createDefaultAdminProfile() {
        AdminProfile profile = new AdminProfile();
        profile.setUsername(appConfig.getAdmin().getUsername());
        profile.setRole("admin");
        profile.setFirstName("");
        profile.setLastName("");
        profile.setEmail("");
        profile.setPhone("");
        profile.setBirthDate("");
        profile.setProfilePicture(null);
        return profile;
    }

    public void changeAdminPassword(ChangeAdminPasswordRequest req) {
        AppConfig.Admin admin = appConfig.getAdmin();
        if (!req.getOldPassword().equals(admin.getPassword())) {
            throw new AppException("Incorrect current password", 401);
        }
        admin.setPassword(req.getNewPassword());
        // Persist new password to application.properties
        // Note: since the password is stored in application.properties (plain text),
        // runtime changes are in-memory only. To persist, update the properties file.
    }
}
