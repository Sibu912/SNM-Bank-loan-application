package io.github.sibu912.snmbank.loans.domain;

import java.math.BigDecimal;

/**
 * Gross monthly income ranges offered on the form.
 * Each band has a fixed income used for the affordability check:
 * the bottom of the band (R25 000 for the lowest band), which is the cautious choice.
 */
public enum IncomeBand {
    LESS_THAN_50K(new BigDecimal("25000")),
    FROM_50K_TO_99K(new BigDecimal("50000")),
    FROM_100K_TO_149K(new BigDecimal("100000")),
    FROM_150K_PLUS(new BigDecimal("150000"));

    private final BigDecimal assessedMonthlyIncome;

    IncomeBand(BigDecimal assessedMonthlyIncome) {
        this.assessedMonthlyIncome = assessedMonthlyIncome;
    }

    public BigDecimal getAssessedMonthlyIncome() {
        return assessedMonthlyIncome;
    }
}
