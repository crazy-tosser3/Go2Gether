package com.example.STOLOTO.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.safety.SafetyBannerCard;
import com.example.STOLOTO.DTO.safety.SafetyBannerDetails;
import com.example.STOLOTO.Models.SafetyCategory;
import com.example.STOLOTO.Services.SafetyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/safety")
@RequiredArgsConstructor
public class SafetyController {

    private final SafetyService safetyService;

    /** Лента баннеров. ?category=first_aid или ?sport=3 (баннеры спорта + общие). */
    @GetMapping("/banners")
    public List<SafetyBannerCard> feed(
            @RequestParam(required = false) SafetyCategory category,
            @RequestParam(required = false) Long sport) {
        return safetyService.feed(category, sport);
    }

    @GetMapping("/banners/{id}")
    public SafetyBannerDetails details(@PathVariable Long id) {
        return safetyService.details(id);
    }
}