package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.dto.request.ReviewRequest;
import com.noir.model.Review;
import com.noir.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final JsonRepository<Review> repo;

    public ReviewService(AppConfig appConfig) {
        this.repo = new JsonRepository<>(appConfig.getDataDir() + "/reviews.json", Review.class);
    }

    public List<Review> list() { return repo.read(); }

    public Review create(ReviewRequest req) {
        List<Review> reviews = repo.read();
        String avatar = Arrays.stream(req.getName().trim().split(" "))
                .limit(2)
                .map(p -> p.isEmpty() ? "" : String.valueOf(Character.toUpperCase(p.charAt(0))))
                .reduce("", String::concat);

        Review r = new Review();
        r.setId(UUID.randomUUID().toString());
        r.setName(req.getName().trim());
        r.setAvatar(avatar);
        r.setPlatform("Website");
        r.setCreatedAt(Instant.now().toString());
        r.setDate(req.getDate());
        r.setRating(req.getRating());
        r.setText(req.getText());

        reviews.add(0, r);
        repo.write(reviews);
        return r;
    }

    public JsonRepository<Review> getRepo() { return repo; }
}