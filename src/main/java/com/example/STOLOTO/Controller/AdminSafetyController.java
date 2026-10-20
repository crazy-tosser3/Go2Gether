package com.example.STOLOTO.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.safety.SafetyBannerCard;
import com.example.STOLOTO.DTO.safety.SafetyBannerDetails;
import com.example.STOLOTO.DTO.safety.SafetyBannerRequest;
import com.example.STOLOTO.Services.SafetyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/safety/banners")
@RequiredArgsConstructor
public class AdminSafetyController {

    private final SafetyService safetyService;

    @GetMapping
    public List<SafetyBannerCard> list() {
        return safetyService.adminList();
    }

    @GetMapping("/{id}")
    public SafetyBannerDetails details(@PathVariable Long id) {
        return safetyService.adminDetails(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SafetyBannerDetails create(@Valid @RequestBody SafetyBannerRequest request) {
        return safetyService.create(request);
    }

    @PutMapping("/{id}")
    public SafetyBannerDetails update(@PathVariable Long id, @Valid @RequestBody SafetyBannerRequest request) {
        return safetyService.update(id, request);
    }

    @PostMapping("/{id}/publish")
    public SafetyBannerCard publish(@PathVariable Long id) {
        return safetyService.setPublished(id, true);
    }

    @PostMapping("/{id}/unpublish")
    public SafetyBannerCard unpublish(@PathVariable Long id) {
        return safetyService.setPublished(id, false);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        safetyService.delete(id);
    }
}