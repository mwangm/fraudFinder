package com.frauddetection.repository;

import com.frauddetection.entity.PayeeRisk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayeeRiskRepository extends JpaRepository<PayeeRisk, Long> {}
