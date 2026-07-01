package com.fraudfinder.repository;

import com.fraudfinder.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspiciousAccountRepository extends JpaRepository<SuspiciousAccount, Long> {}
