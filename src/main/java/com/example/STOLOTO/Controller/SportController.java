package com.example.STOLOTO.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.sport.CreateSportRequest;
import com.example.STOLOTO.DTO.sport.SportDetailsResponse;
import com.example.STOLOTO.DTO.sport.SportResponse;
import com.example.STOLOTO.Services.SportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sports")
@RequiredArgsConstructor
public class SportController {

    private final SportService sportService;

    @GetMapping
    public List<SportResponse> search(@RequestParam(required = false) String q) {
        return sportService.search(q);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SportResponse create(@Valid @RequestBody CreateSportRequest request) {
        return sportService.create(request);
    }

    @GetMapping("/{id}")
    public SportDetailsResponse details(@PathVariable String id) {
        return sportService.details(id);
    }
}
