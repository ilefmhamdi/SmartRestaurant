package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.exception.AppException;
import com.noir.model.NewsletterSubscriber;
import com.noir.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NewsletterService {

    private final JsonRepository<NewsletterSubscriber> repo;

    public NewsletterService(AppConfig appConfig) {
        this.repo = new JsonRepository<>(appConfig.getDataDir() + "/newsletter.json", NewsletterSubscriber.class);
    }

    public NewsletterSubscriber subscribe(String email) {
        List<NewsletterSubscriber> subs = repo.read();
        String normalized = email.trim().toLowerCase();
        if (subs.stream().anyMatch(s -> s.getEmail().equals(normalized))) {
            throw new AppException("This email is already subscribed", 409);
        }

        NewsletterSubscriber sub = new NewsletterSubscriber();
        sub.setId(UUID.randomUUID().toString());
        sub.setEmail(normalized);
        sub.setSubscribedAt(Instant.now().toString());
        sub.setSource("newsletter");

        subs.add(0, sub);
        repo.write(subs);
        return sub;
    }

    public JsonRepository<NewsletterSubscriber> getRepo() { return repo; }
}