package com.noir.controller;

import com.noir.dto.request.ChangePasswordRequest;
import com.noir.dto.request.RejectRequest;
import com.noir.dto.request.StaffLoginRequest;
import com.noir.dto.request.UpdateStaffProfileRequest;
import com.noir.model.StaffUser;
import com.noir.service.DeliveryService;
import com.noir.service.StaffAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final StaffAuthService staffAuthService;
    private final DeliveryService deliveryService;

    public DeliveryController(StaffAuthService staffAuthService, DeliveryService deliveryService) {
        this.staffAuthService = staffAuthService;
        this.deliveryService = deliveryService;
    }

    // ---------------------------------------------------------------
    // Auth
    // ---------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody StaffLoginRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", staffAuthService.login(req)));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        StaffUser staff = (StaffUser) req.getAttribute("staff");
        return ResponseEntity.ok(Map.of("success", true, "data", staffAuthService.sanitize(staff)));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateStaffProfileRequest req, HttpServletRequest request) {
        StaffUser staff = (StaffUser) request.getAttribute("staff");
        StaffUser updated = staffAuthService.updateStaffProfile(staff, req);
        return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully", "data", staffAuthService.sanitize(updated)));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest req, HttpServletRequest request) {
        StaffUser staff = (StaffUser) request.getAttribute("staff");
        staffAuthService.changeStaffPassword(staff, req);
        return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully"));
    }

    // ---------------------------------------------------------------
    // Orders — delivery sees ready + out_for_delivery
    // ---------------------------------------------------------------
    @GetMapping("/orders")
    public ResponseEntity<?> listOrders() {
        return ResponseEntity.ok(Map.of("success", true, "data", deliveryService.listDeliveryOrders()));
    }

    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<?> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(Map.of("success", true, "data", deliveryService.getDeliveryOrder(orderNumber)));
    }

    // ---------------------------------------------------------------
    // Status transitions
    // ---------------------------------------------------------------
    @PatchMapping("/orders/{orderNumber}/pickup")
    public ResponseEntity<?> pickup(@PathVariable String orderNumber, HttpServletRequest request) {
        StaffUser staff = deliveryUser(request);
        return ResponseEntity.ok(Map.of("success", true,
                "data", deliveryService.pickup(orderNumber, staff.getUsername())));
    }

    @PatchMapping("/orders/{orderNumber}/delivered")
    public ResponseEntity<?> delivered(@PathVariable String orderNumber, HttpServletRequest request) {
        StaffUser staff = deliveryUser(request);
        return ResponseEntity.ok(Map.of("success", true,
                "data", deliveryService.markDelivered(orderNumber, staff.getUsername())));
    }

    @PatchMapping("/orders/{orderNumber}/undelivered")
    public ResponseEntity<?> undelivered(@PathVariable String orderNumber,
            @Valid @RequestBody RejectRequest req,
            HttpServletRequest request) {
        StaffUser staff = deliveryUser(request);
        return ResponseEntity.ok(Map.of("success", true,
                "data", deliveryService.markUndelivered(orderNumber, req.getReason(), staff.getUsername())));
    }

    private StaffUser deliveryUser(HttpServletRequest req) {
        return (StaffUser) req.getAttribute("deliveryStaff");
    }
}
