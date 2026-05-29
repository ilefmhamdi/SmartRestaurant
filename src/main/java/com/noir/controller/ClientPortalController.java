package com.noir.controller;

import com.noir.dto.request.ClientOrderRequest;
import com.noir.dto.request.ClientReservationRequest;
import com.noir.dto.request.ClientReviewRequest;
import com.noir.model.User;
import com.noir.service.ClientPortalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/client")
public class ClientPortalController {

    private final ClientPortalService portalService;
    public ClientPortalController(ClientPortalService portalService) {
        this.portalService = portalService;
    }

    private User client(HttpServletRequest req) {
        return (User) req.getAttribute("client");
    }

    @GetMapping("/portal")
    public ResponseEntity<?> portal(HttpServletRequest req) throws Exception {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.getPortal(client(req))));
    }

    @GetMapping("/menu")
    public ResponseEntity<?> menu() {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.listMenu()));
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> createReservation(@Valid @RequestBody ClientReservationRequest body,
                                               HttpServletRequest req) {
        return ResponseEntity.status(201).body(
                Map.of("success", true, "data", portalService.createReservation(client(req), body)));
    }

    @GetMapping("/reservations")
    public ResponseEntity<?> listReservations(HttpServletRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.listReservations(client(req))));
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@Valid @RequestBody ClientOrderRequest body,
                                         HttpServletRequest req) {
        return ResponseEntity.status(201).body(
                Map.of("success", true, "data", portalService.createOrder(client(req), body)));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> listOrders(HttpServletRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.listOrders(client(req))));
    }

    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<?> trackOrder(@PathVariable String orderNumber, HttpServletRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.trackOrder(client(req), orderNumber)));
    }

    @PatchMapping("/orders/{orderNumber}/confirm-delivery")
    public ResponseEntity<?> confirmDelivery(@PathVariable String orderNumber, HttpServletRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.confirmOrderDelivery(client(req), orderNumber)));
    }

    @DeleteMapping("/orders/{orderNumber}")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderNumber, HttpServletRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.cancelOrder(client(req), orderNumber)));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable String id, HttpServletRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.cancelReservation(client(req), id)));
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(@Valid @RequestBody ClientReviewRequest body,
                                          HttpServletRequest req) {
        return ResponseEntity.status(201).body(
                Map.of("success", true, "data", portalService.createReview(client(req), body)));
    }

    @GetMapping("/loyalty")
    public ResponseEntity<?> loyalty(HttpServletRequest req) {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.getLoyalty(client(req))));
    }

    @GetMapping("/offers")
    public ResponseEntity<?> offers() {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.getOffers()));
    }

    @GetMapping("/chef")
    public ResponseEntity<?> chef() {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.getChef()));
    }

    @GetMapping("/about")
    public ResponseEntity<?> about() {
        return ResponseEntity.ok(Map.of("success", true, "data", portalService.getAbout()));
    }
}