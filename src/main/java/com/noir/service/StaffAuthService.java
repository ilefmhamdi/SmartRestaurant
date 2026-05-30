package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.dto.request.ChangePasswordRequest;
import com.noir.dto.request.CreateStaffRequest;
import com.noir.dto.request.StaffLoginRequest;
import com.noir.dto.request.StaffUpdateRequest;
import com.noir.dto.request.UpdateStaffProfileRequest;
import com.noir.exception.AppException;
import com.noir.model.StaffUser;
import com.noir.repository.JsonRepository;
import com.noir.security.TokenService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
public class StaffAuthService {

    private static final Set<String> VALID_ROLES = Set.of("chef", "kitchen", "delivery");

    private final JsonRepository<StaffUser> staffRepo;
    private final TokenService tokenService;
    private final AppConfig appConfig;

    public StaffAuthService(AppConfig appConfig, TokenService tokenService) {
        this.appConfig = appConfig;
        this.tokenService = tokenService;
        this.staffRepo = new JsonRepository<>(appConfig.getDataDir() + "/staff.json", StaffUser.class);
    }

    // ---------------------------------------------------------------
    // Login — returns a token scoped to "kitchen" or "delivery" group
    // ---------------------------------------------------------------
    public Map<String, Object> login(StaffLoginRequest req) {
        List<StaffUser> staff = staffRepo.read();
        StaffUser user = staff.stream()
            .filter(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(req.getUsername()))
            .findFirst()
            .orElseThrow(() -> new AppException("Invalid username or password", 401));

        if (!hashPassword(req.getPassword(), user.getSalt()).equals(user.getPasswordHash())) {
            throw new AppException("Invalid username or password", 401);
        }

        // Update last login
        int idx = staff.indexOf(user);
        user.setLastLoginAt(Instant.now().toString());
        staff.set(idx, user);
        staffRepo.write(staff);

        // Token secret depends on role group
        String secret = tokenSecretFor(user.getRole());
        long exp = System.currentTimeMillis() + (1000L * 60 * 60 * 12); // 12h
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getId());
        payload.put("role", user.getRole());
        payload.put("username", user.getUsername());
        payload.put("exp", exp);

        String token = tokenService.sign(payload, secret);
        return Map.of(
            "token", token,
            "user", sanitize(user)
        );
    }

    // ---------------------------------------------------------------
    // Verify a kitchen token (chef or kitchen role)
    // ---------------------------------------------------------------
    public StaffUser verifyKitchenToken(String token) {
        Map<String, Object> payload = tokenService.verify(
                token, appConfig.getKitchen().getTokenSecret(), "Missing kitchen token");
        String role = (String) payload.get("role");
        if (!"chef".equals(role) && !"kitchen".equals(role)) {
            throw new AppException("Access denied: kitchen staff only", 403);
        }
        return loadStaffFromPayload(payload);
    }

    // ---------------------------------------------------------------
    // Verify a delivery token
    // ---------------------------------------------------------------
    public StaffUser verifyDeliveryToken(String token) {
        Map<String, Object> payload = tokenService.verify(
                token, appConfig.getDelivery().getTokenSecret(), "Missing delivery token");
        String role = (String) payload.get("role");
        if (!"delivery".equals(role)) {
            throw new AppException("Access denied: delivery staff only", 403);
        }
        return loadStaffFromPayload(payload);
    }

    // ---------------------------------------------------------------
    // Admin: create staff
    // ---------------------------------------------------------------
    public Map<String, Object> createStaff(CreateStaffRequest req) {
        if (!VALID_ROLES.contains(req.getRole())) {
            throw new AppException("Invalid role. Must be chef, kitchen, or delivery", 400);
        }
        List<StaffUser> staff = staffRepo.read();
        if (staff.stream().anyMatch(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(req.getUsername()))) {
            throw new AppException("Username already taken", 409);
        }

        String salt = generateSalt();
        StaffUser user = new StaffUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(req.getUsername().trim());
        user.setRole(req.getRole());
        user.setSalt(salt);
        user.setPasswordHash(hashPassword(req.getPassword(), salt));
        user.setCreatedAt(Instant.now().toString());
        user.setLastLoginAt(null);

        staff.add(0, user);
        staffRepo.write(staff);
        return sanitize(user);
    }

    // ---------------------------------------------------------------
    // Admin: list staff (no passwords)
    // ---------------------------------------------------------------
    public Map<String, Object> updateStaff(String id, StaffUpdateRequest req) {
        List<StaffUser> staff = staffRepo.read();
        int idx = -1;
        for (int i = 0; i < staff.size(); i++) {
            if (id.equals(staff.get(i).getId())) { idx = i; break; }
        }
        if (idx < 0) throw new AppException("Staff member not found", 404);

        StaffUser user = staff.get(idx);
        if (user.getUsername() != null && !user.getUsername().equalsIgnoreCase(req.getUsername()) &&
            staff.stream().anyMatch(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(req.getUsername()))) {
            throw new AppException("Username already taken", 409);
        }

        user.setUsername(req.getUsername().trim());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setSalt(generateSalt());
            user.setPasswordHash(hashPassword(req.getPassword(), user.getSalt()));
        }
        if (!VALID_ROLES.contains(req.getRole())) {
            throw new AppException("Invalid role. Must be chef, kitchen, or delivery", 400);
        }
        user.setRole(req.getRole());

        staff.set(idx, user);
        staffRepo.write(staff);
        return sanitize(user);
    }

    public List<Map<String, Object>> listStaff() {
        return staffRepo.read().stream().map(this::sanitize).toList();
    }

    // ---------------------------------------------------------------
    // Admin: delete staff
    // ---------------------------------------------------------------
    public Map<String, Boolean> deleteStaff(String id) {
        List<StaffUser> staff = staffRepo.read();
        boolean removed = staff.removeIf(u -> u.getId() != null && u.getId().equals(id));
        if (!removed) throw new AppException("Staff member not found", 404);
        staffRepo.write(staff);
        return Map.of("success", true);
    }

    // ---------------------------------------------------------------
    // Staff self-update
    // ---------------------------------------------------------------
    public StaffUser updateStaffProfile(StaffUser current, UpdateStaffProfileRequest req) {
        List<StaffUser> staff = staffRepo.read();
        String newUsername = req.getUsername().trim();
        if (!current.getUsername().equalsIgnoreCase(newUsername) &&
            staff.stream().anyMatch(u -> u.getId() != current.getId() && u.getUsername().equalsIgnoreCase(newUsername))) {
            throw new AppException("Username already in use", 409);
        }

        StaffUser updatedUser = null;
        for (StaffUser u : staff) {
            if (u.getId().equals(current.getId())) {
                u.setUsername(newUsername);
                if (req.getFirstName() != null) u.setFirstName(req.getFirstName().trim());
                if (req.getLastName() != null) u.setLastName(req.getLastName().trim());
                if (req.getEmail() != null) u.setEmail(req.getEmail().trim());
                if (req.getPhone() != null) u.setPhone(req.getPhone().trim());
                if (req.getBirthDate() != null) u.setBirthDate(req.getBirthDate());
                if (req.getProfilePicture() != null) u.setProfilePicture(req.getProfilePicture());
                updatedUser = u;
                break;
            }
        }
        staffRepo.write(staff);
        return updatedUser != null ? updatedUser : current;
    }

    public void changeStaffPassword(StaffUser current, ChangePasswordRequest req) {
        if (!hashPassword(req.getOldPassword(), current.getSalt()).equals(current.getPasswordHash())) {
            throw new AppException("Incorrect old password", 401);
        }

        List<StaffUser> staff = staffRepo.read();
        String newSalt = generateSalt();
        String newHash = hashPassword(req.getNewPassword(), newSalt);

        for (StaffUser u : staff) {
            if (u.getId().equals(current.getId())) {
                u.setSalt(newSalt);
                u.setPasswordHash(newHash);
                break;
            }
        }
        staffRepo.write(staff);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private StaffUser loadStaffFromPayload(Map<String, Object> payload) {
        String sub = (String) payload.get("sub");
        return staffRepo.read().stream()
                .filter(u -> u.getId().equals(sub))
                .findFirst()
                .orElseThrow(() -> new AppException("Staff account not found", 401));
    }

    private String tokenSecretFor(String role) {
        return switch (role) {
            case "chef", "kitchen" -> appConfig.getKitchen().getTokenSecret();
            case "delivery"        -> appConfig.getDelivery().getTokenSecret();
            default -> throw new AppException("Unknown role: " + role, 500);
        };
    }

    public Map<String, Object> sanitize(StaffUser u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          u.getId());
        m.put("username",    u.getUsername());
        m.put("role",        u.getRole());
        m.put("createdAt",   u.getCreatedAt());
        m.put("lastLoginAt", u.getLastLoginAt());
        m.put("firstName",   u.getFirstName());
        m.put("lastName",    u.getLastName());
        m.put("email",       u.getEmail());
        m.put("phone",       u.getPhone());
        m.put("birthDate",   u.getBirthDate());
        m.put("profilePicture", u.getProfilePicture());
        return m;
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(), HexFormat.of().parseHex(salt), 120000, 512);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            return HexFormat.of().formatHex(factory.generateSecret(spec).getEncoded());
        } catch (Exception e) {
            throw new AppException("Password hashing failed", 500);
        }
    }
}
