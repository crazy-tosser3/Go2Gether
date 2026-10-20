package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.sport.CreateSportRequest;
import com.example.STOLOTO.DTO.sport.SportDetailsResponse;
import com.example.STOLOTO.DTO.sport.SportResponse;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.Sport;
import com.example.STOLOTO.Models.SportCategory;
import com.example.STOLOTO.cache.CacheRegions;
import com.example.STOLOTO.cache.PageCache;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.SportRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;

@Service
@Transactional
@RequiredArgsConstructor
public class SportService {

    public static final String CACHE = "sports";

    private final SportRepository sports;
    private final EventRepository events;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;
    private final PageCache cache;

    public static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace('ё', 'е').replaceAll("\\s+", " ");
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE, key = "#q == null ? '' : #q.trim().toLowerCase()")
    public List<SportResponse> search(String q) {
        String needle = q == null || q.isBlank() ? null : normalize(q);
        return sports.findAllByOrderByCustomAscNameAsc().stream()
                .filter(s -> needle == null || s.getNormalizedName().contains(needle))
                .sorted(Comparator.comparing((Sport s) -> needle != null && !s.getNormalizedName().startsWith(needle)))
                .map(mapper::toSport)
                .toList();
    }

    @CacheEvict(cacheNames = CACHE, allEntries = true)
    public SportResponse create(CreateSportRequest request) {
        Long userId = currentUser.require().getId();
        return mapper.toSport(findOrCreate(request.name(), request.category(), userId));
    }

    @Transactional(readOnly = true)
    public SportDetailsResponse details(Long id) {
        return cache.get(CacheRegions.SPORT_STATS, String.valueOf(id), CacheRegions.SPORT_STATS_TTL,
                new TypeReference<SportDetailsResponse>() {}, () -> loadDetails(id));
    }

    private SportDetailsResponse loadDetails(Long id) {
        Sport s = require(id);
        long active = events.countBySportIdAndStatusAndStartsAtAfter(id, EventStatus.PUBLISHED, Instant.now());
        return new SportDetailsResponse(s.getId(), s.getName(), s.getCategory(), s.getIcon(), s.isCustom(), active);
    }

    public Sport require(Long id) {
        return sports.findById(id).orElseThrow(() -> ApiException.notFound("Направление не найдено"));
    }

    @CacheEvict(cacheNames = CACHE, allEntries = true)
    public Sport findOrCreate(String name, SportCategory category, Long createdBy) {
        if (name == null || name.isBlank()) {
            throw ApiException.validation("name", "Название направления обязательно");
        }
        String normalized = normalize(name);
        return sports.findByNormalizedName(normalized).orElseGet(() -> {
            Sport s = new Sport();
            s.setName(name.trim());
            s.setNormalizedName(normalized);
            s.setCategory(category == null ? SportCategory.OTHER : category);
            s.setCustom(createdBy != null);
            s.setCreatedBy(createdBy);
            s.setCreatedAt(Instant.now());
            return sports.save(s);
        });
    }

    public List<Long> validateIds(Collection<Long> ids, String field) {
        List<Long> distinct = ids.stream().distinct().toList();
        for (Long id : distinct) {
            if (id == null || !sports.existsById(id)) {
                throw ApiException.validation(field, "Направление не найдено: " + id);
            }
        }
        return distinct;
    }
}
