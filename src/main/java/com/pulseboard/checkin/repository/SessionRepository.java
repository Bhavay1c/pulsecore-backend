package com.pulseboard.checkin.repository;

import com.pulseboard.checkin.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}
