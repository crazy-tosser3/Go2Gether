package com.example.STOLOTO.Controller;

import java.time.Duration;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.Services.AvatarStorage;
import com.example.STOLOTO.exception.ApiException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/avatars")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarStorage avatars;

    @GetMapping("/{name}")
    public ResponseEntity<Resource> avatar(@PathVariable String name) {
        var file = avatars.find(name).orElseThrow(() -> ApiException.notFound("Картинка не найдена"));
        return ResponseEntity.ok()
                .contentType(AvatarStorage.mediaType(name))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(new FileSystemResource(file));
    }
}
