package com.example.STOLOTO.Models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "safety_banners")
public class SafetyBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subtitle;

    @Enumerated(EnumType.STRING)
    private SafetyCategory category;

    private String coverUrl;
    private String accentColor;

    private Long sportId;

    private int sortOrder;

    @Column(name = "is_published")
    private boolean published = true;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "banner", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<SafetySlide> slides = new ArrayList<>();

    public void replaceSlides(List<SafetySlide> newSlides) {
        slides.clear();
        newSlides.forEach(s -> {
            s.setBanner(this);
            slides.add(s);
        });
    }
}