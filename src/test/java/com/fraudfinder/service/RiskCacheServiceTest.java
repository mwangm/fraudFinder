package com.fraudfinder.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fraudfinder.model.PayeeRisk;
import com.fraudfinder.model.SuspiciousAccount;
import com.fraudfinder.repository.PayeeRiskRepository;
import com.fraudfinder.repository.SuspiciousAccountRepository;
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
  void shouldDetectSuspiciousAccount() {
    SuspiciousAccount sa = new SuspiciousAccount("ACC-BAD", "fraud", Instant.now());
    when(suspiciousAccountRepository.findAll()).thenReturn(List.of(sa));
    when(payeeRiskRepository.findAll()).thenReturn(List.of());

    cache.refresh();

    assertThat(cache.isSuspicious("ACC-BAD")).isTrue();
    assertThat(cache.isSuspicious("ACC-GOOD")).isFalse();
  }

  @Test
  void shouldReturnPayeeRisk() {
    PayeeRisk pr = new PayeeRisk("PE-HIGH", "HIGH", "history", Instant.now());
    when(suspiciousAccountRepository.findAll()).thenReturn(List.of());
    when(payeeRiskRepository.findAll()).thenReturn(List.of(pr));

    cache.refresh();

    PayeeRisk result = cache.getPayeeRisk("PE-HIGH");
    assertThat(result).isNotNull();
    assertThat(result.getRiskLevel()).isEqualTo("HIGH");
  }

  @Test
  void shouldReturnNullForUnknownPayee() {
    when(suspiciousAccountRepository.findAll()).thenReturn(List.of());
    when(payeeRiskRepository.findAll()).thenReturn(List.of());

    cache.refresh();

    assertThat(cache.getPayeeRisk("UNKNOWN")).isNull();
  }

  @Test
  void shouldRefreshData() {
    SuspiciousAccount sa = new SuspiciousAccount("ACC-1", "fraud", Instant.now());
    when(suspiciousAccountRepository.findAll()).thenReturn(List.of(sa));
    when(payeeRiskRepository.findAll()).thenReturn(List.of());

    cache.refresh();

    assertThat(cache.isSuspicious("ACC-1")).isTrue();

    // Updated data
    SuspiciousAccount updated = new SuspiciousAccount("ACC-2", "reason", Instant.now());
    when(suspiciousAccountRepository.findAll()).thenReturn(List.of(updated));

    cache.refresh();

    assertThat(cache.isSuspicious("ACC-1")).isFalse();
    assertThat(cache.isSuspicious("ACC-2")).isTrue();
  }
}
