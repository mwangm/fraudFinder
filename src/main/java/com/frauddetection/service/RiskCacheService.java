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
  private final AlertService alertService;

  private volatile CacheState state = CacheState.EMPTY;

  public RiskCacheService(
      SuspiciousAccountRepository suspiciousAccountRepository,
      PayeeRiskRepository payeeRiskRepository,
      AlertService alertService) {
    this.suspiciousAccountRepository = suspiciousAccountRepository;
    this.payeeRiskRepository = payeeRiskRepository;
    this.alertService = alertService;
  }

  @PostConstruct
  void init() {
    refresh();
    if (!state.initialized) {
      throw new IllegalStateException(
          "Risk cache failed to initialize — check database connectivity");
    }
  }

  @Scheduled(fixedDelayString = "${fraud.cache.refresh-interval-seconds:60}000")
  void refresh() {
    try {
      Set<String> accounts =
          suspiciousAccountRepository.findAll().stream()
              .map(SuspiciousAccount::getAccountId)
              .collect(Collectors.toUnmodifiableSet());
      Map<String, PayeeRisk> payees =
          payeeRiskRepository.findAll().stream()
              .collect(Collectors.toUnmodifiableMap(PayeeRisk::getPayeeId, r -> r));

      // Atomic replacement — readers see either full old or full new state, never a mix
      state = new CacheState(accounts, payees, true);

      log.info(
          "Risk cache refreshed: {} suspicious accounts, {} payee risks",
          accounts.size(),
          payees.size());
    } catch (Exception e) {
      log.error(
          "Risk cache refresh failed — using {} cache (initialized={})",
          state.initialized ? "stale" : "empty",
          state.initialized,
          e);
      if (state.initialized) {
        alertService.send(
            "FRAUD-DETECTION: Risk cache refresh failed",
            "Risk cache refresh failed, using stale data. Error: " + e.getMessage());
      }
    }
  }

  public boolean isSuspicious(String accountId) {
    return state.suspiciousAccounts.contains(accountId);
  }

  public PayeeRisk getPayeeRisk(String payeeId) {
    return state.payeeRisks.get(payeeId);
  }

  /** SpEL-friendly alias: {@code riskCache.isSuspiciousAccount(accountId)}. */
  public boolean isSuspiciousAccount(String accountId) {
    return isSuspicious(accountId);
  }

  /** SpEL-friendly alias: {@code riskCache.isHighRiskPayee(payeeId)}. */
  public boolean isHighRiskPayee(String payeeId) {
    return getPayeeRisk(payeeId) != null;
  }

  private record CacheState(
      Set<String> suspiciousAccounts, Map<String, PayeeRisk> payeeRisks, boolean initialized) {

    static final CacheState EMPTY = new CacheState(Set.of(), Map.of(), false);
  }
}
