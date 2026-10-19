package com.example.STOLOTO.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByDemoTrueOrderByIdAsc();

    long countByRoleAndActiveTrue(Role role);
}
