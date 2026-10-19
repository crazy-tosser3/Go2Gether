package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.STOLOTO.DTO.sport.CreateSportRequest;
import com.example.STOLOTO.DTO.sport.SportDetailsResponse;
import com.example.STOLOTO.DTO.sport.SportResponse;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.Sport;
import com.example.STOLOTO.Models.SportCategory;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.SportRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SportService {

    private final SportRepository sports;
    private final EventRepository events;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    public static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace('ё', 'е').replaceAll("\\s+", " ");
    }

    public List<SportResponse> search(String q) {
        String needle = q == null || q.isBlank() ? null : normalize(q);
        return sports.findAll().stream()
                .filter(s -> needle == null || s.getNormalizedName().contains(needle))
                .sorted(Comparator.comparing((Sport s) -> needle != null && !s.getNormalizedName().startsWith(needle))
                        .thenComparing(Sport::isCustom)
                        .thenComparing(Sport::getName))
                .map(mapper::toSport)
                .toList();
    }

    public SportResponse create(CreateSportRequest request) {
        String userId = currentUser.require().getId();
        return mapper.toSport(findOrCreate(request.name(), request.category(), userId));
    }

    public SportDetailsResponse details(String id) {
        Sport s = require(id);
        long active = events.findBySportIdAndStatus(id, EventStatus.PUBLISHED).stream()
                .filter(e -> !e.isPast())
                .count();
        return new SportDetailsResponse(s.getId(), s.getName(), s.getCategory(), s.getIcon(), s.isCustom(), active);
    }

    public Sport require(String id) {
        return sports.findById(id).orElseThrow(() -> ApiException.notFound("Направление не найдено"));
    }

    public Sport findOrCreate(String name, SportCategory category, String createdBy) {
        if (name == null || name.isBlank()) {
            throw ApiException.validation("name", "Название направления обязательно");
        }
        String normalized = normalize(name);
        return sports.findByNormalizedName(normalized).orElseGet(() -> {
            Sport s = new Sport();
            s.setId(UUID.randomUUID().toString());
            s.setName(name.trim());
            s.setNormalizedName(normalized);
            s.setCategory(category == null ? SportCategory.OTHER : category);
            s.setCustom(createdBy != null);
            s.setCreatedBy(createdBy);
            s.setCreatedAt(Instant.now());
            return sports.save(s);
        });
    }

    public List<String> validateIds(List<String> ids, String field) {
        if (ids == null) {
            return null;
        }
        List<String> distinct = ids.stream().distinct().toList();
        for (String id : distinct) {
            if (id == null || !sports.existsById(id)) {
                throw ApiException.validation(field, "Направление не найдено: " + id);
            }
        }
        return distinct;
    }
}
