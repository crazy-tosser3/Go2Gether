package com.example.STOLOTO.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.example.STOLOTO.Models.Organization;
import com.example.STOLOTO.Models.OrganizationStatus;

public interface OrganizationRepository extends ListCrudRepository<Organization, String> {
    List<Organization> findByOwnerId(String ownerId);
    List<Organization> findByStatus(OrganizationStatus status);
}
