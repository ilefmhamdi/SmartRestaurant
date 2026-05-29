package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.model.MenuItem;
import com.noir.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class MenuService {

    private final JsonRepository<MenuItem> repo;

    public MenuService(AppConfig appConfig) {
        this.repo = new JsonRepository<>(appConfig.getDataDir() + "/menu.json", MenuItem.class);
    }

    public List<MenuItem> list(String category, Boolean vegetarian, String search) {
        Stream<MenuItem> stream = repo.read().stream();

        if (category != null && !category.equals("all")) {
            stream = stream.filter(item -> category.equals(item.getCat()));
        }
        if (Boolean.TRUE.equals(vegetarian)) {
            stream = stream.filter(item -> Boolean.TRUE.equals(item.getVeg()));
        }
        if (search != null && !search.isBlank()) {
            String term = search.trim().toLowerCase();
            stream = stream.filter(item -> {
                boolean nameMatch = item.getName() != null && item.getName().toLowerCase().contains(term);
                boolean descMatch = item.getDesc() != null && item.getDesc().toLowerCase().contains(term);
                boolean allergenMatch = item.getAllergens() != null &&
                        item.getAllergens().stream().anyMatch(a -> a.toLowerCase().contains(term));
                return nameMatch || descMatch || allergenMatch;
            });
        }
        return stream.toList();
    }

    public JsonRepository<MenuItem> getRepo() { return repo; }
}