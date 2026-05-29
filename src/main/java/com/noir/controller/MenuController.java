package com.noir.controller;

import com.noir.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;
    public MenuController(MenuService menuService) { this.menuService = menuService; }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(Map.of("success", true, "data", menuService.list(category, vegetarian, search)));
    }
}