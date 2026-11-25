package io.github.sibu912.snmbank.loans.web.dto;

import io.github.sibu912.snmbank.loans.domain.AffordabilityResult;
import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;
import io.github.sibu912.snmbank.loans.domain.Channel;
import io.github.sibu912.snmbank.loans.domain.IncomeBand;
import io.github.sibu912.snmbank.loans.domain.LoanAmountBand;
import io.github.sibu912.snmbank.loans.domain.LoanPurpose;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Everything about one application.
 */
public record ApplicationDetail(
        String referenceNumber,
        ApplicantDto applicant,
        LoanPurpose loanPurpose,
        LoanAmountBand loanAmountBand,
        int termYears,
        IncomeBand incomeBand,
        BigDecimal estimatedMonthlyRepayment,
        AffordabilityResult affordabilityResult,
        ApplicationStatus status,
        Channel channel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
