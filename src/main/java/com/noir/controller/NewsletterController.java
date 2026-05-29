package com.noir.controller;

import com.noir.dto.request.NewsletterRequest;
import com.noir.service.NewsletterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;
    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping
    public ResponseEntity<?> subscribe(@Valid @RequestBody NewsletterRequest req) {
        return ResponseEntity.status(201).body(
                Map.of("success", true, "message", "Subscription confirmed",
                        "data", newsletterService.subscribe(req.getEmail())));
    }
}