package io.github.sibu912.snmbank.loans.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Settings under "snm.loan" in application.yml.
 *
 * @param annualInterestRate    for example 0.115 for 11.5%
 * @param maxRepaymentToIncome  for example 0.30: the repayment may be at most 30% of monthly income
 */
@Validated
@ConfigurationProperties(prefix = "snm.loan")
public record LoanProperties(
        @NotNull @Positive BigDecimal annualInterestRate,
        @NotNull @Positive BigDecimal maxRepaymentToIncome) {
}
