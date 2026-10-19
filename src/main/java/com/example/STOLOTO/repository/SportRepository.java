package com.example.STOLOTO.repository;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;

import com.example.STOLOTO.Models.Sport;

public interface SportRepository extends ListCrudRepository<Sport, String> {
    Optional<Sport> findByNormalizedName(String normalizedName);
}
