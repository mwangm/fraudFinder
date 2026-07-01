package com.frauddetection.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionMessage(
    @NotBlank String transactionId,
    @NotBlank String accountId,
    @NotBlank String payeeId,
    @NotNull @Positive BigDecimal amount) {}
