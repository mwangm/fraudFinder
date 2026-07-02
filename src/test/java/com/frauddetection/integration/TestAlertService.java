package com.frauddetection.integration;

import com.frauddetection.entity.FraudRecord;
import com.frauddetection.model.DetectionResultDetail;
import com.frauddetection.service.AlertService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/** Test double that records triggered alerts instead of publishing to SNS. */
@Service
@Primary
public class TestAlertService extends AlertService {

  private final List<DetectionResultDetail> triggeredRules = new ArrayList<>();
  private final List<String> alerts = new ArrayList<>();

  public TestAlertService() {
    super(null, null);
  }

  @Override
  public void publish(FraudRecord record) {
    triggeredRules.addAll(
        record.getDetails().stream()
            .map(d -> new DetectionResultDetail(d.getRuleName(), d.getScore(), d.getReason()))
            .toList());
    alerts.add(
        String.format(
            "ALERT: txnId=%s score=%d/%d rules=%s",
            record.getTransactionId(),
            record.getTotalScore(),
            record.getThreshold(),
            record.getDetails().stream().map(d -> d.getRuleName()).toList()));
  }

  public List<DetectionResultDetail> getTriggeredRules() {
    return new ArrayList<>(triggeredRules);
  }

  public List<String> getAlerts() {
    return new ArrayList<>(alerts);
  }

  public void reset() {
    triggeredRules.clear();
    alerts.clear();
  }
}
