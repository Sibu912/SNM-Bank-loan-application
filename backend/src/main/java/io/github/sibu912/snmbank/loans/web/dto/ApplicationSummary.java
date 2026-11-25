package io.github.sibu912.snmbank.loans.web.dto;

import io.github.sibu912.snmbank.loans.domain.AffordabilityResult;
import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;
import io.github.sibu912.snmbank.loans.domain.LoanAmountBand;
import io.github.sibu912.snmbank.loans.domain.LoanPurpose;

import java.time.LocalDateTime;

/**
 * One row in the list of applications.
 */
public record ApplicationSummary(
        String referenceNumber,
        String applicantName,
        LoanPurpose loanPurpose,
        LoanAmountBand loanAmountBand,
        ApplicationStatus status,
        AffordabilityResult affordabilityResult,
        LocalDateTime createdAt) {
}
