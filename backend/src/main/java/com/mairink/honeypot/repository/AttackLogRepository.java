package com.mairink.honeypot.repository;

import com.mairink.honeypot.model.AttackLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttackLogRepository extends JpaRepository<AttackLog, Long> {
}
