package com.example.STOLOTO.repository;

import org.springframework.data.repository.ListCrudRepository;

import com.example.STOLOTO.Models.ModerationLog;

public interface ModerationLogRepository extends ListCrudRepository<ModerationLog, String> {
}
