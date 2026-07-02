package com.frauddetection.service;

import com.frauddetection.entity.PayeeRisk;
import com.frauddetection.entity.SuspiciousAccount;
import com.frauddetection.repository.PayeeRiskRepository;
import com.frauddetection.repository.SuspiciousAccountRepository;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** In-memory cache for reference data — refreshed on startup and every 60 seconds. */
@Service
@Slf4j
public class RiskCacheService {

  private final SuspiciousAccountRepository suspiciousAccountRepository;
  private final PayeeRiskRepository payeeRiskRepository;

  private volatile Set<String> suspiciousAccountIds = Set.of();
  private volatile Map<String, PayeeRisk> payeeRiskMap = Map.of();
  private volatile boolean initialized = false;

  public RiskCacheService(
      SuspiciousAccountRepository suspiciousAccountRepository,
      PayeeRiskRepository payeeRiskRepository) {
    this.suspiciousAccountRepository = suspiciousAccountRepository;
    this.payeeRiskRepository = payeeRiskRepository;
  }

  @PostConstruct
  void init() {
    refresh();
    initialized = true;
  }

  @Scheduled(fixedRateString = "${fraud.cache.refresh-interval-seconds:60}000")
  void refresh() {
    try {
      suspiciousAccountIds =
          suspiciousAccountRepository.findAll().stream()
              .map(SuspiciousAccount::getAccountId)
              .collect(Collectors.toUnmodifiableSet());
      payeeRiskMap =
          payeeRiskRepository.findAll().stream()
              .collect(Collectors.toUnmodifiableMap(PayeeRisk::getPayeeId, r -> r));
      initialized = true;
      log.info(
          "Risk cache refreshed: {} suspicious accounts, {} payee risks",
          suspiciousAccountIds.size(),
          payeeRiskMap.size());
    } catch (Exception e) {
      log.error(
          "Risk cache refresh failed — using {} cache (initialized={})",
          initialized ? "stale" : "empty",
          initialized,
          e);
    }
  }

  public boolean isSuspicious(String accountId) {
    return suspiciousAccountIds.contains(accountId);
  }

  public PayeeRisk getPayeeRisk(String payeeId) {
    return payeeRiskMap.get(payeeId);
  }

  /** SpEL-friendly alias: {@code riskCache.isSuspiciousAccount(accountId)}. */
  public boolean isSuspiciousAccount(String accountId) {
    return isSuspicious(accountId);
  }

  /** SpEL-friendly alias: {@code riskCache.isHighRiskPayee(payeeId)}. */
  public boolean isHighRiskPayee(String payeeId) {
    return getPayeeRisk(payeeId) != null;
  }
}
