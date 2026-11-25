package io.github.sibu912.snmbank.loans.service;

import io.github.sibu912.snmbank.loans.config.LoanProperties;
import io.github.sibu912.snmbank.loans.domain.AffordabilityResult;
import io.github.sibu912.snmbank.loans.domain.IncomeBand;
import io.github.sibu912.snmbank.loans.domain.LoanAmountBand;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Works out the monthly repayment and whether the loan is affordable.
 * Plain calculations only - no database - so it is easy to unit test.
 */
@Component
public class AffordabilityCalculator {

    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);

    /** 16 significant digits while calculating; rounded to cents at the end. */
    private static final MathContext PRECISION = MathContext.DECIMAL64;

    private final LoanProperties properties;

    public AffordabilityCalculator(LoanProperties properties) {
        this.properties = properties;
    }

    public AffordabilityAssessment assess(LoanAmountBand amountBand, int termYears, IncomeBand incomeBand) {
        BigDecimal repayment = monthlyRepayment(amountBand.getAssessedAmount(), termYears);
        BigDecimal limit = incomeBand.getAssessedMonthlyIncome().multiply(properties.maxRepaymentToIncome());

        AffordabilityResult result = repayment.compareTo(limit) <= 0
                ? AffordabilityResult.PASS
                : AffordabilityResult.FAIL;
        return new AffordabilityAssessment(repayment, result);
    }

    /**
     * Standard amortisation formula: M = P * r * (1 + r)^n / ((1 + r)^n - 1)
     * where r is the monthly interest rate and n is the number of monthly payments.
     */
    public BigDecimal monthlyRepayment(BigDecimal principal, int termYears) {
        BigDecimal monthlyRate = properties.annualInterestRate().divide(MONTHS_PER_YEAR, PRECISION);
        int numberOfPayments = termYears * 12;

        BigDecimal growthFactor = BigDecimal.ONE.add(monthlyRate).pow(numberOfPayments, PRECISION); // (1 + r)^n

        BigDecimal repayment = principal
                .multiply(monthlyRate)
                .multiply(growthFactor)
                .divide(growthFactor.subtract(BigDecimal.ONE), PRECISION);

        return repayment.setScale(2, RoundingMode.HALF_UP);
    }
}
