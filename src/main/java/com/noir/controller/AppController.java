package com.noir.controller;

import com.noir.config.AppConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.Instant;
import java.util.Map;

@RestController
public class AppController {

    private final AppConfig appConfig;
    
    public AppController(AppConfig appConfig) { 
        this.appConfig = appConfig;
    }

    @GetMapping("/api/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("success", true, "data",
                Map.of("status", "ok", "timestamp", Instant.now().toString())));
    }

    @GetMapping("/admin")
    public Resource adminPage() {
        return new FileSystemResource(new File(appConfig.getPublicDir() + "/admin.html"));
    }
    @GetMapping("/kitchen")
    public Resource kitchenPage() {
        return new FileSystemResource(new File(appConfig.getPublicDir() + "/kitchen.html"));
    }
    @GetMapping("/delivery")
    public Resource deliveryPage() {
        return new FileSystemResource(new File(appConfig.getPublicDir() + "/delivery.html"));
    }
    @GetMapping(value = {"/"})
    public Resource indexPage() {
        return new FileSystemResource(new File(appConfig.getPublicDir() + "/index.html"));
    }
}