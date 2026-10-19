package com.example.STOLOTO.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.STOLOTO.Models.Sport;

public interface SportRepository extends JpaRepository<Sport, Long> {
    Optional<Sport> findByNormalizedName(String normalizedName);

    List<Sport> findAllByOrderByCustomAscNameAsc();
}
