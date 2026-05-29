package com.noir.controller;

import com.noir.dto.request.ChangePasswordRequest;
import com.noir.dto.request.OrderStatusUpdateRequest;
import com.noir.dto.request.RejectRequest;
import com.noir.dto.request.SpecialPlateRequest;
import com.noir.dto.request.StaffLoginRequest;
import com.noir.dto.request.UpdateStaffProfileRequest;
import com.noir.exception.AppException;
import com.noir.model.StaffUser;
import com.noir.service.KitchenService;
import com.noir.service.StaffAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/kitchen")
public class KitchenController {

    private final StaffAuthService staffAuthService;
    private final KitchenService kitchenService;

    public KitchenController(StaffAuthService staffAuthService, KitchenService kitchenService) {
        this.staffAuthService = staffAuthService;
        this.kitchenService = kitchenService;
    }

    // ---------------------------------------------------------------
    // Auth
    // ---------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody StaffLoginRequest req) {
        // Validate that the user has a kitchen-compatible role (chef or kitchen)
        // This is enforced inside StaffAuthService by checking role before signing token
        var result = staffAuthService.login(req);
        // If role is "delivery", this endpoint still returns a token but it won't
        // work for kitchen routes — that's fine; delivery staff should use /api/delivery/login
        return ResponseEntity.ok(Map.of("success", true, "data", result));
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
    // Orders
    // ---------------------------------------------------------------
    @GetMapping("/orders")
    public ResponseEntity<?> listOrders() {
        return ResponseEntity.ok(Map.of("success", true, "data", kitchenService.listKitchenOrders()));
    }

    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<?> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(Map.of("success", true, "data", kitchenService.getKitchenOrder(orderNumber)));
    }

    @PatchMapping("/orders/{orderNumber}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody OrderStatusUpdateRequest req,
            HttpServletRequest request) {
        StaffUser staff = staffUser(request);
        return ResponseEntity.ok(Map.of("success", true,
                "data", kitchenService.updateOrderStatus(orderNumber, req, staff.getUsername())));
    }

    @PatchMapping("/orders/{orderNumber}/reject")
    public ResponseEntity<?> rejectOrder(
            @PathVariable String orderNumber,
            @Valid @RequestBody RejectRequest req,
            HttpServletRequest request) {
        StaffUser staff = requireChef(request);
        return ResponseEntity.ok(Map.of("success", true,
                "data", kitchenService.rejectOrder(orderNumber, req.getReason(), staff.getUsername())));
    }

    // ---------------------------------------------------------------
    // Reservations (for today and upcoming — with allergy info)
    // ---------------------------------------------------------------
    @GetMapping("/reservations")
    public ResponseEntity<?> listReservations() {
        return ResponseEntity.ok(Map.of("success", true, "data", kitchenService.listKitchenReservations()));
    }

    @GetMapping("/menu")
    public ResponseEntity<?> listMenu() {
        return ResponseEntity.ok(Map.of("success", true, "data", kitchenService.listMenu()));
    }

    @PatchMapping("/reservations/{id}/reject")
    public ResponseEntity<?> rejectReservation(
            @PathVariable String id,
            @Valid @RequestBody RejectRequest req,
            HttpServletRequest request) {
        StaffUser staff = requireChef(request);
        return ResponseEntity.ok(Map.of("success", true,
                "data", kitchenService.rejectReservation(id, req.getReason(), staff.getUsername())));
    }

    // ---------------------------------------------------------------
    // Special plates — all staff can view, only chefs can modify
    // ---------------------------------------------------------------
    @GetMapping("/special-plates")
    public ResponseEntity<?> listSpecialPlates() {
        return ResponseEntity.ok(Map.of("success", true, "data", kitchenService.listSpecialPlates()));
    }

    @PostMapping("/special-plates")
    public ResponseEntity<?> createSpecialPlate(
            @Valid @RequestBody SpecialPlateRequest req,
            HttpServletRequest request) {
        StaffUser staff = requireChef(request);
        return ResponseEntity.status(201).body(Map.of("success", true,
                "data", kitchenService.createSpecialPlate(req, staff.getUsername())));
    }

    @PutMapping("/special-plates/{id}")
    public ResponseEntity<?> updateSpecialPlate(
            @PathVariable String id,
            @Valid @RequestBody SpecialPlateRequest req,
            HttpServletRequest request) {
        StaffUser staff = requireChef(request);
        return ResponseEntity.ok(Map.of("success", true,
                "data", kitchenService.updateSpecialPlate(id, req, staff.getUsername())));
    }

    @DeleteMapping("/special-plates/{id}")
    public ResponseEntity<?> deleteSpecialPlate(
            @PathVariable String id,
            HttpServletRequest request) {
        requireChef(request);
        return ResponseEntity.ok(Map.of("success", true, "data", kitchenService.deleteSpecialPlate(id)));
    }

    // ---------------------------------------------------------------
    // Recommend a menu item — chef only
    // ---------------------------------------------------------------
    @PostMapping("/recommend/{menuItemId}")
    public ResponseEntity<?> recommend(
            @PathVariable int menuItemId,
            HttpServletRequest request) {
        StaffUser staff = requireChef(request);
        return ResponseEntity.ok(Map.of("success", true,
                "data", kitchenService.recommendMenuItem(menuItemId, staff.getUsername())));
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private StaffUser staffUser(HttpServletRequest req) {
        return (StaffUser) req.getAttribute("kitchenStaff");
    }

    private StaffUser requireChef(HttpServletRequest req) {
        StaffUser staff = staffUser(req);
        if (staff == null || !"chef".equals(staff.getRole())) {
            throw new AppException("This action requires chef privileges", 403);
        }
        return staff;
    }
}
