package com.pulseboard.checkin.repository;

import com.pulseboard.checkin.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByNameIgnoreCase(String name);
}
