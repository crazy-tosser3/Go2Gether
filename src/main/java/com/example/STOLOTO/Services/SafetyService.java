package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.safety.SafetyBannerCard;
import com.example.STOLOTO.DTO.safety.SafetyBannerDetails;
import com.example.STOLOTO.DTO.safety.SafetyBannerRequest;
import com.example.STOLOTO.DTO.safety.SafetySlideRequest;
import com.example.STOLOTO.DTO.safety.SafetySlideResponse;
import com.example.STOLOTO.Models.SafetyBanner;
import com.example.STOLOTO.Models.SafetyCategory;
import com.example.STOLOTO.Models.SafetySlide;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.SafetyBannerRepository;
import com.example.STOLOTO.repository.SportRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SafetyService {

    public static final String CACHE = "safety";

    static final String DISCLAIMER =
            "Информация носит справочный характер и не заменяет медицинскую помощь. "
                    + "При угрозе жизни звоните 112.";

    private final SafetyBannerRepository banners;
    private final SportRepository sports;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE,
            key = "'feed:' + (#category == null ? '' : #category.name()) + ':' + (#sportId == null ? '' : #sportId)")
    public List<SafetyBannerCard> feed(SafetyCategory category, Long sportId) {
        List<SafetyBanner> list;
        if (sportId != null) {
            list = banners.findFeedForSport(sportId);
        } else if (category != null) {
            list = banners.findByPublishedTrueAndCategoryOrderBySortOrderAscIdAsc(category);
        } else {
            list = banners.findByPublishedTrueOrderBySortOrderAscIdAsc();
        }
        return list.stream().map(this::toCard).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE, key = "'banner:' + #id")
    public SafetyBannerDetails details(Long id) {
        SafetyBanner b = banners.findWithSlidesByIdAndPublishedTrue(id)
                .orElseThrow(() -> ApiException.notFound("Материал не найден"));
        return toDetails(b);
    }

    @Transactional(readOnly = true)
    public List<SafetyBannerCard> adminList() {
        return banners.findAllByOrderBySortOrderAscIdAsc().stream().map(this::toCard).toList();
    }

    @Transactional(readOnly = true)
    public SafetyBannerDetails adminDetails(Long id) {
        return toDetails(require(id));
    }

    @CacheEvict(cacheNames = CACHE, allEntries = true)
    public SafetyBannerDetails create(SafetyBannerRequest request) {
        SafetyBanner b = new SafetyBanner();
        b.setCreatedAt(Instant.now());
        apply(b, request);
        return toDetails(banners.save(b));
    }

    @CacheEvict(cacheNames = CACHE, allEntries = true)
    public SafetyBannerDetails update(Long id, SafetyBannerRequest request) {
        SafetyBanner b = require(id);
        apply(b, request);
        return toDetails(banners.save(b));
    }

    @CacheEvict(cacheNames = CACHE, allEntries = true)
    public SafetyBannerCard setPublished(Long id, boolean published) {
        SafetyBanner b = require(id);
        b.setPublished(published);
        b.setUpdatedAt(Instant.now());
        return toCard(b);
    }

    @CacheEvict(cacheNames = CACHE, allEntries = true)
    public void delete(Long id) {
        banners.delete(require(id));
    }

    private SafetyBanner require(Long id) {
        return banners.findById(id).orElseThrow(() -> ApiException.notFound("Материал не найден"));
    }

    private void apply(SafetyBanner b, SafetyBannerRequest r) {
        if (r.sportId() != null && !sports.existsById(r.sportId())) {
            throw ApiException.validation("sport_id", "Направление не найдено: " + r.sportId());
        }
        b.setTitle(r.title().trim());
        b.setSubtitle(r.subtitle());
        b.setCategory(r.category());
        b.setCoverUrl(r.coverUrl());
        b.setAccentColor(r.accentColor());
        b.setSportId(r.sportId());
        if (r.sortOrder() != null) {
            b.setSortOrder(r.sortOrder());
        }
        if (r.published() != null) {
            b.setPublished(r.published());
        }
        b.setUpdatedAt(Instant.now());

        List<SafetySlideRequest> in = r.slides();
        b.replaceSlides(IntStream.range(0, in.size())
                .mapToObj(i -> toSlide(in.get(i), i + 1))
                .toList());
    }

    private SafetySlide toSlide(SafetySlideRequest r, int position) {
        SafetySlide s = new SafetySlide();
        s.setPosition(position);
        s.setTitle(r.title());
        s.setBody(r.body());
        s.setImageUrl(r.imageUrl());
        return s;
    }

    private SafetyBannerCard toCard(SafetyBanner b) {
        return new SafetyBannerCard(b.getId(), b.getTitle(), b.getSubtitle(), b.getCategory(),
                b.getCoverUrl(), b.getAccentColor(), b.getSportId(), b.isPublished());
    }

    private SafetyBannerDetails toDetails(SafetyBanner b) {
        List<SafetySlideResponse> slides = b.getSlides().stream()
                .map(s -> new SafetySlideResponse(s.getPosition(), s.getTitle(), s.getBody(), s.getImageUrl()))
                .toList();
        return new SafetyBannerDetails(b.getId(), b.getTitle(), b.getSubtitle(), b.getCategory(),
                b.getCoverUrl(), b.getAccentColor(), b.getSportId(), b.isPublished(), slides, DISCLAIMER);
    }
}