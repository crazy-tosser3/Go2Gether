package com.example.STOLOTO.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.STOLOTO.Models.Organization;
import com.example.STOLOTO.Models.OrganizationStatus;

public interface OrganizationRepository extends JpaRepository<Organization, Long>,
        JpaSpecificationExecutor<Organization> {
    List<Organization> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    long countByStatus(OrganizationStatus status);
}
