package com.noir.controller;

import com.noir.dto.request.ReservationRequest;
import com.noir.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ReservationRequest req) {
        return ResponseEntity.status(201).body(
                Map.of("success", true, "message", "Reservation confirmed",
                        "data", reservationService.create(req)));
    }
}