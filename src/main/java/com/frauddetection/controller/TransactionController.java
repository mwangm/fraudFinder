package com.frauddetection.controller;

import com.frauddetection.entity.FraudRecord;
import com.frauddetection.entity.Transaction;
import com.frauddetection.repository.FraudRecordRepository;
import com.frauddetection.repository.TransactionRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final TransactionRepository transactionRepo;
  private final FraudRecordRepository fraudRecordRepo;

  public TransactionController(
      TransactionRepository transactionRepo, FraudRecordRepository fraudRecordRepo) {
    this.transactionRepo = transactionRepo;
    this.fraudRecordRepo = fraudRecordRepo;
  }

  @Transactional(readOnly = true)
  @GetMapping("/{transactionId}")
  public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String transactionId) {
    return transactionRepo
        .findById(transactionId)
        .map(
            txn -> {
              var fraud = fraudRecordRepo.findByTransactionId(transactionId);
              return ResponseEntity.ok(TransactionResponse.from(txn, fraud));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  public record TransactionResponse(
      String transactionId,
      String accountId,
      String payeeId,
      String amount,
      boolean fraud,
      List<FraudDetail> fraudDetails,
      String createdAt) {

    static TransactionResponse from(Transaction txn, java.util.Optional<FraudRecord> fraud) {
      return new TransactionResponse(
          txn.getTransactionId(),
          txn.getAccountId(),
          txn.getPayeeId(),
          txn.getAmount().toPlainString(),
          fraud.isPresent(),
          fraud
              .map(
                  f ->
                      f.getDetails().stream()
                          .map(d -> new FraudDetail(d.getRuleName(), d.getScore(), d.getReason()))
                          .toList())
              .orElse(List.of()),
          txn.getCreatedAt().toString());
    }
  }

  public record FraudDetail(String ruleName, int score, String reason) {}
}
