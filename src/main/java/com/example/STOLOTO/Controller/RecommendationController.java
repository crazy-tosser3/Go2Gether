package com.example.STOLOTO.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.recommendation.RecommendationResponse;
import com.example.STOLOTO.Services.RecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * С токеном — по интересам, истории записей, городу, уровню и тем, с кем пользователь играл.
     * Без токена — популярные ближайшие события. lat/lng (необязательно) поднимают события рядом.
     */
    @GetMapping("/recommendations/events")
    public List<RecommendationResponse> forMe(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        return recommendationService.forCurrentUser(limit, lat, lng);
    }

    /** Похожие по виду спорта, месту, уровню, организатору и составу участников. */
    @GetMapping("/events/{id}/similar")
    public List<RecommendationResponse> similar(@PathVariable Long id,
            @RequestParam(required = false) Integer limit) {
        return recommendationService.similar(id, limit);
    }
}
