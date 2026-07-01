package com.frauddetection.repository;

import com.frauddetection.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayeeRiskRepository extends JpaRepository<PayeeRisk, Long> {}
