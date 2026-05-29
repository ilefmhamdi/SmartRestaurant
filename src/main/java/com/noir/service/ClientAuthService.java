package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.dto.request.ChangePasswordRequest;
import com.noir.dto.request.UpdateProfileRequest;
import com.noir.exception.AppException;
import com.noir.model.NewsletterSubscriber;
import com.noir.model.User;
import com.noir.repository.JsonRepository;
import com.noir.security.TokenService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
public class ClientAuthService {

    private final JsonRepository<User> userRepo;
    private final JsonRepository<NewsletterSubscriber> newsletterRepo;
    private final TokenService tokenService;
    private final AppConfig appConfig;

    public ClientAuthService(AppConfig appConfig, TokenService tokenService) {
        this.appConfig = appConfig;
        this.tokenService = tokenService;
        this.userRepo = new JsonRepository<>(appConfig.getDataDir() + "/users.json", User.class);
        this.newsletterRepo = new JsonRepository<>(appConfig.getDataDir() + "/newsletter.json", NewsletterSubscriber.class);
    }

    public Map<String, Object> signup(com.noir.dto.request.SignupRequest req) {
        List<User> users = userRepo.read();
        String email = req.getEmail().trim().toLowerCase();
        if (users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email))) {
            throw new AppException("An account already exists with this email", 409);
        }

        String salt = generateSalt();
        String passwordHash = hashPassword(req.getPassword(), salt);

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setFirstName(req.getFirstName().trim());
        user.setLastName(req.getLastName().trim());
        user.setEmail(email);
        user.setSalt(salt);
        user.setPasswordHash(passwordHash);
        user.setCreatedAt(Instant.now().toString());
        user.setLastLoginAt(Instant.now().toString());
        user.setProfilePicture(null);
        user.setIsPro(false);
        user.setProSince(null);
        user.setPhone(req.getPhone());
        user.setBirthDate(req.getBirthDate());

        users.add(0, user);
        userRepo.write(users);
        syncSignupSubscriber(email);

        return createAuthResponse(user);
    }

    public Map<String, Object> login(com.noir.dto.request.LoginRequest req) {
        List<User> users = userRepo.read();
        String email = req.getEmail().trim().toLowerCase();
        int idx = -1;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equalsIgnoreCase(email)) { idx = i; break; }
        }
        if (idx < 0) throw new AppException("Invalid email or password", 401);

        User user = users.get(idx);
        if (!hashPassword(req.getPassword(), user.getSalt()).equals(user.getPasswordHash())) {
            throw new AppException("Invalid email or password", 401);
        }

        user.setLastLoginAt(Instant.now().toString());
        userRepo.write(users);
        return createAuthResponse(user);
    }

    public User getCurrentUser(String token) {
        Map<String, Object> payload = tokenService.verify(
                token, appConfig.getClient().getTokenSecret(), "Missing client token");
        String sub = (String) payload.get("sub");
        return userRepo.read().stream()
                .filter(u -> u.getId().equals(sub))
                .findFirst()
                .map(this::sanitize)
                .orElseThrow(() -> new AppException("Client account not found", 401));
    }

    private Map<String, Object> createAuthResponse(User user) {
        long exp = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 7);
        Map<String, Object> payload = Map.of("sub", user.getId(), "role", "client", "exp", exp);
        String token = tokenService.sign(payload, appConfig.getClient().getTokenSecret());
        return Map.of("token", token, "user", sanitize(user));
    }

    private User sanitize(User user) {
        User safe = new User();
        safe.setId(user.getId());
        safe.setFirstName(user.getFirstName());
        safe.setLastName(user.getLastName());
        safe.setEmail(user.getEmail());
        safe.setCreatedAt(user.getCreatedAt());
        safe.setLastLoginAt(user.getLastLoginAt());
        safe.setProfilePicture(user.getProfilePicture());
        safe.setIsPro(user.getIsPro() == null ? false : user.getIsPro());
        safe.setPhone(user.getPhone());
        safe.setBirthDate(user.getBirthDate());
        return safe;
    }
    public User updateProfile(User currentUser, UpdateProfileRequest req) {
        List<User> users = userRepo.read();
        String newEmail = req.getEmail().trim().toLowerCase();
        if (!currentUser.getEmail().equalsIgnoreCase(newEmail) &&
            users.stream().anyMatch(u -> u.getId() != currentUser.getId() && u.getEmail().equalsIgnoreCase(newEmail))) {
            throw new AppException("Email already in use", 409);
        }

        User updatedUser = null;
        for (User u : users) {
            if (u.getId().equals(currentUser.getId())) {
                u.setFirstName(req.getFirstName().trim());
                u.setLastName(req.getLastName().trim());
                u.setEmail(newEmail);
                u.setPhone(req.getPhone());
                u.setBirthDate(req.getBirthDate());
                if (req.getProfilePicture() != null) u.setProfilePicture(req.getProfilePicture());
                if (req.getIsPro() != null) {
                    u.setIsPro(req.getIsPro());
                    if (req.getIsPro()) u.setProSince(Instant.now().toString());
                    else u.setProSince(null);
                }
                updatedUser = u;
                break;
            }
        }
        userRepo.write(users);
        return sanitize(updatedUser != null ? updatedUser : currentUser);
    }

    public void changePassword(User currentUser, ChangePasswordRequest req) {
        if (!hashPassword(req.getOldPassword(), currentUser.getSalt()).equals(currentUser.getPasswordHash())) {
            throw new AppException("Incorrect old password", 401);
        }

        List<User> users = userRepo.read();
        String newSalt = generateSalt();
        String newHash = hashPassword(req.getNewPassword(), newSalt);

        for (User u : users) {
            if (u.getId().equals(currentUser.getId())) {
                u.setSalt(newSalt);
                u.setPasswordHash(newHash);
                break;
            }
        }
        userRepo.write(users);
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

    private void syncSignupSubscriber(String email) {
        List<NewsletterSubscriber> subs = newsletterRepo.read();
        if (subs.stream().anyMatch(s -> s.getEmail().equalsIgnoreCase(email))) return;
        NewsletterSubscriber sub = new NewsletterSubscriber();
        sub.setId(UUID.randomUUID().toString());
        sub.setEmail(email);
        sub.setSubscribedAt(Instant.now().toString());
        sub.setSource("signup");
        subs.add(0, sub);
        newsletterRepo.write(subs);
    }
}