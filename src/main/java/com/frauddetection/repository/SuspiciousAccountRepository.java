package com.frauddetection.repository;

import com.frauddetection.entity.SuspiciousAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspiciousAccountRepository extends JpaRepository<SuspiciousAccount, Long> {}
