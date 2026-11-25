package io.github.sibu912.snmbank.loans.service;

import io.github.sibu912.snmbank.loans.domain.AffordabilityResult;

import java.math.BigDecimal;

/**
 * The outcome of the affordability check.
 */
public record AffordabilityAssessment(BigDecimal monthlyRepayment, AffordabilityResult result) {
}
