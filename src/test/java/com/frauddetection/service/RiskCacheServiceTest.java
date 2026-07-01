package com.frauddetection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.frauddetection.model.PayeeRisk;
import com.frauddetection.model.SuspiciousAccount;
import com.frauddetection.repository.PayeeRiskRepository;
import com.frauddetection.repository.SuspiciousAccountRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RiskCacheServiceTest {

  @Mock SuspiciousAccountRepository suspiciousAccountRepository;
  @Mock PayeeRiskRepository payeeRiskRepository;

  RiskCacheService cache;

  @BeforeEach
  void setUp() {
    cache = new RiskCacheService(suspiciousAccountRepository, payeeRiskRepository);
  }

  @Test
  void givenBlacklistedAccount_whenCheck_thenReturnsTrue() {
    SuspiciousAccount sa = new SuspiciousAccount("ACC-BAD", "fraud", Instant.now());
    when(suspiciousAccountRepository.findAll()).thenReturn(List.of(sa));
    when(payeeRiskRepository.findAll()).thenReturn(List.of());

    cache.refresh();

    assertThat(cache.isSuspicious("ACC-BAD")).isTrue();
    assertThat(cache.isSuspicious("ACC-GOOD")).isFalse();
  }

  @Test
  void givenPayeeWithRisk_whenCheck_thenReturnsRisk() {
    PayeeRisk pr = new PayeeRisk("PE-HIGH", "HIGH", "history", Instant.now());
    when(suspiciousAccountRepository.findAll()).thenReturn(List.of());
    when(payeeRiskRepository.findAll()).thenReturn(List.of(pr));

    cache.refresh();

    PayeeRisk result = cache.getPayeeRisk("PE-HIGH");
    assertThat(result).isNotNull();
    assertThat(result.getRiskLevel()).isEqualTo("HIGH");
  }

  @Test
  void givenUnknownPayee_whenCheck_thenReturnsNull() {
    when(suspiciousAccountRepository.findAll()).thenReturn(List.of());
    when(payeeRiskRepository.findAll()).thenReturn(List.of());

    cache.refresh();

    assertThat(cache.getPayeeRisk("UNKNOWN")).isNull();
  }

  @Test
  void givenCacheRefresh_whenCheckAgain_thenReturnsUpdatedData() {
    SuspiciousAccount sa = new SuspiciousAccount("ACC-1", "fraud", Instant.now());
    when(suspiciousAccountRepository.findAll()).thenReturn(List.of(sa));
    when(payeeRiskRepository.findAll()).thenReturn(List.of());

    cache.refresh();
    assertThat(cache.isSuspicious("ACC-1")).isTrue();

    SuspiciousAccount updated = new SuspiciousAccount("ACC-2", "reason", Instant.now());
    when(suspiciousAccountRepository.findAll()).thenReturn(List.of(updated));

    cache.refresh();
    assertThat(cache.isSuspicious("ACC-1")).isFalse();
    assertThat(cache.isSuspicious("ACC-2")).isTrue();
  }
}
