package com.example.STOLOTO.repository;

import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;

import com.example.STOLOTO.Models.User;

public interface UserRepository extends ListCrudRepository<User, String> {
    Optional<User> findByEmail(String email);
}
