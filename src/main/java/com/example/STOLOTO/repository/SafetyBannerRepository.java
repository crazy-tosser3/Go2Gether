package com.example.STOLOTO.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.STOLOTO.Models.SafetyBanner;
import com.example.STOLOTO.Models.SafetyCategory;

public interface SafetyBannerRepository extends JpaRepository<SafetyBanner, Long> {

    List<SafetyBanner> findByPublishedTrueOrderBySortOrderAscIdAsc();

    List<SafetyBanner> findByPublishedTrueAndCategoryOrderBySortOrderAscIdAsc(SafetyCategory category);

    @Query("""
            select b from SafetyBanner b
            where b.published = true
              and (b.sportId = :sportId or b.sportId is null)
            order by b.sortOrder asc, b.id asc
            """)
    List<SafetyBanner> findFeedForSport(@Param("sportId") Long sportId);

    @EntityGraph(attributePaths = "slides")
    Optional<SafetyBanner> findWithSlidesByIdAndPublishedTrue(Long id);

    List<SafetyBanner> findAllByOrderBySortOrderAscIdAsc();
}