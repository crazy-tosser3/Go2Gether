package com.example.STOLOTO.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.event.OrganizerEventResponse;
import com.example.STOLOTO.DTO.participation.MyParticipationResponse;
import com.example.STOLOTO.DTO.user.PublicUserResponse;
import com.example.STOLOTO.DTO.user.UpdateProfileRequest;
import com.example.STOLOTO.DTO.user.UserResponse;
import com.example.STOLOTO.Models.ParticipationStatus;
import com.example.STOLOTO.Services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public PublicUserResponse publicProfile(@PathVariable Long id) {
        return userService.publicProfile(id);
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateMe(request);
    }

    @GetMapping("/me/events")
    public List<OrganizerEventResponse> myEvents() {
        return userService.myEvents();
    }

    @GetMapping("/me/participations")
    public List<MyParticipationResponse> myParticipations(
            @RequestParam(required = false) ParticipationStatus status) {
        return userService.myParticipations(status);
    }
}
