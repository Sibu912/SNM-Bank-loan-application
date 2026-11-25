package io.github.sibu912.snmbank.loans.web.dto;

import io.github.sibu912.snmbank.loans.domain.AffordabilityResult;
import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubmitApplicationResponse(
        String referenceNumber,
        ApplicationStatus status,
        AffordabilityResult affordabilityResult,
        BigDecimal estimatedMonthlyRepayment,
        LocalDateTime createdAt) {
}
