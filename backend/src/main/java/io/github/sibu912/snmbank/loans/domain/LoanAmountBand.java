package io.github.sibu912.snmbank.loans.domain;

import java.math.BigDecimal;

/**
 * The loan amount ranges offered on the form.
 * Each band has a fixed amount used for the affordability check:
 * the top of the band, which is the cautious choice (see docs/DESIGN.md 6.2).
 */
public enum LoanAmountBand {
    LESS_THAN_250K(new BigDecimal("250000")),
    FROM_250K_TO_499K(new BigDecimal("500000")),
    FROM_500K_TO_999K(new BigDecimal("1000000")),
    ONE_MILLION_PLUS(new BigDecimal("1500000"));

    private final BigDecimal assessedAmount;

    LoanAmountBand(BigDecimal assessedAmount) {
        this.assessedAmount = assessedAmount;
    }

    public BigDecimal getAssessedAmount() {
        return assessedAmount;
    }
}
