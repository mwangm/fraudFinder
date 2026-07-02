package com.frauddetection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.frauddetection.entity.PayeeRisk;
import com.frauddetection.entity.SuspiciousAccount;
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

  @Mock SuspiciousAccountRepository suspiciousRepo;
  @Mock PayeeRiskRepository payeeRiskRepo;

  RiskCacheService service;

  @BeforeEach
  void setUp() {
    service = new RiskCacheService(suspiciousRepo, payeeRiskRepo);
  }

  @Test
  void givenSuspiciousAccount_whenIsSuspicious_thenReturnsTrue() {
    when(suspiciousRepo.findAll())
        .thenReturn(List.of(new SuspiciousAccount("ACC-BAD", "bad account", Instant.now())));

    service.refresh();

    assertThat(service.isSuspicious("ACC-BAD")).isTrue();
    assertThat(service.isSuspicious("ACC-NORMAL")).isFalse();
  }

  @Test
  void givenSuspiciousAccount_whenIsSuspiciousAccount_thenReturnsTrue() {
    when(suspiciousRepo.findAll())
        .thenReturn(List.of(new SuspiciousAccount("ACC-BAD", "bad account", Instant.now())));

    service.refresh();

    assertThat(service.isSuspiciousAccount("ACC-BAD")).isTrue();
    assertThat(service.isSuspiciousAccount("ACC-NORMAL")).isFalse();
  }

  @Test
  void givenHighRiskPayee_whenIsHighRiskPayee_thenReturnsTrue() {
    when(payeeRiskRepo.findAll())
        .thenReturn(List.of(new PayeeRisk("PE-HIGH", "HIGH", "high risk", Instant.now())));

    service.refresh();

    assertThat(service.isHighRiskPayee("PE-HIGH")).isTrue();
    assertThat(service.isHighRiskPayee("PE-NORMAL")).isFalse();
  }

  @Test
  void givenPayeeRisk_whenGetPayeeRisk_thenReturnsRisk() {
    var risk = new PayeeRisk("PE-HIGH", "HIGH", "high risk", Instant.now());
    when(payeeRiskRepo.findAll()).thenReturn(List.of(risk));

    service.refresh();

    assertThat(service.getPayeeRisk("PE-HIGH")).isEqualTo(risk);
    assertThat(service.getPayeeRisk("PE-NORMAL")).isNull();
  }

  @Test
  void givenRefreshFailure_whenIsSuspicious_thenStillReturnsFalseSafely() {
    when(suspiciousRepo.findAll()).thenThrow(new RuntimeException("DB down"));
    service.refresh();

    // Should not throw — cache is stale/empty but service keeps running
    assertThat(service.isSuspicious("ACC-BAD")).isFalse();
  }
}
