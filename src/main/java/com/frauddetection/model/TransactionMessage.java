package com.frauddetection.model;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TransactionMessage(
    @NotBlank @Size(max = 128) String transactionId,
    @NotBlank @Size(max = 64) String accountId,
    @NotBlank @Size(max = 64) String payeeId,
    @NotNull @Positive @Digits(integer = 16, fraction = 2) BigDecimal amount) {}
