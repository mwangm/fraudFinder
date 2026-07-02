package com.frauddetection.repository;

import com.frauddetection.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayeeRiskRepository extends JpaRepository<PayeeRisk, Long> {}
