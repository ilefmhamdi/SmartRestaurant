package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.dto.request.ReservationRequest;
import com.noir.model.Reservation;
import com.noir.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final JsonRepository<Reservation> repo;

    public ReservationService(AppConfig appConfig) {
        this.repo = new JsonRepository<>(appConfig.getDataDir() + "/reservations.json", Reservation.class);
    }

    public Reservation create(ReservationRequest req) {
        List<Reservation> list = repo.read();
        String code = "NOIR-" + java.time.Year.now().getValue() + "-" +
                String.format("%04d", list.size() + 1);

        Reservation r = new Reservation();
        r.setId(UUID.randomUUID().toString());
        r.setConfirmationCode(code);
        r.setStatus("confirmed");
        r.setCreatedAt(Instant.now().toString());
        r.setFirstName(req.getFirstName().trim());
        r.setLastName(req.getLastName().trim());
        r.setEmail(req.getEmail());
        r.setPhone(req.getPhone());
        r.setDate(req.getDate());
        r.setGuests(req.getGuests());
        r.setTime(req.getTime());
        r.setExperience(req.getExperience());
        r.setMenuSelections(req.getMenuSelections());
        r.setAllergySelections(req.getAllergySelections());
        r.setSpecialRequests(req.getSpecialRequests());

        list.add(0, r);
        repo.write(list);
        return r;
    }

    public JsonRepository<Reservation> getRepo() { return repo; }
}