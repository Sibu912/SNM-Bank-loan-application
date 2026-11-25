package io.github.sibu912.snmbank.loans.service;

import io.github.sibu912.snmbank.loans.config.LoanProperties;
import io.github.sibu912.snmbank.loans.domain.AffordabilityResult;
import io.github.sibu912.snmbank.loans.domain.IncomeBand;
import io.github.sibu912.snmbank.loans.domain.LoanAmountBand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain unit test: no Spring, no database. The expected values are the
 * test cases in docs/DESIGN.md section 6.2 (11.5% interest, 30% limit).
 */
class AffordabilityCalculatorTest {

    private final AffordabilityCalculator calculator = new AffordabilityCalculator(
            new LoanProperties(new BigDecimal("0.115"), new BigDecimal("0.30")));

    @Test
    void calculatesMonthlyRepaymentToTheCent() {
        BigDecimal repayment = calculator.monthlyRepayment(new BigDecimal("500000"), 5);

        assertThat(repayment).isEqualByComparingTo("10996.30");
        assertThat(repayment.scale()).isEqualTo(2);
    }

    @ParameterizedTest(name = "{0} over {1} years with income {2} -> R{3} ({4})")
    @CsvSource({
            "FROM_250K_TO_499K, 5,  FROM_50K_TO_99K, 10996.30, PASS",
            "FROM_500K_TO_999K, 5,  FROM_50K_TO_99K, 21992.61, FAIL",
            "FROM_500K_TO_999K, 10, FROM_50K_TO_99K, 14059.54, PASS",
            "ONE_MILLION_PLUS,  20, LESS_THAN_50K,   15996.44, FAIL",
            "ONE_MILLION_PLUS,  30, FROM_50K_TO_99K, 14854.37, PASS",
            "LESS_THAN_250K,    10, LESS_THAN_50K,   3514.89,  PASS"
    })
    void assessesTheDesignTestCases(LoanAmountBand amountBand, int termYears, IncomeBand incomeBand,
                                    BigDecimal expectedRepayment, AffordabilityResult expectedResult) {
        AffordabilityAssessment assessment = calculator.assess(amountBand, termYears, incomeBand);

        assertThat(assessment.monthlyRepayment()).isEqualByComparingTo(expectedRepayment);
        assertThat(assessment.result()).isEqualTo(expectedResult);
    }

    @Test
    void aStricterLimitTurnsAPassIntoAFail() {
        // R250 000 over 5 years = R5 498.15 a month. Income band LESS_THAN_50K is assessed at R25 000.
        // With a 30% limit (R7 500) that passes; with a 20% limit (R5 000) it fails.
        AffordabilityCalculator strict = new AffordabilityCalculator(
                new LoanProperties(new BigDecimal("0.115"), new BigDecimal("0.20")));

        AffordabilityAssessment normal = calculator.assess(LoanAmountBand.LESS_THAN_250K, 5, IncomeBand.LESS_THAN_50K);
        AffordabilityAssessment stricter = strict.assess(LoanAmountBand.LESS_THAN_250K, 5, IncomeBand.LESS_THAN_50K);

        assertThat(normal.monthlyRepayment()).isEqualByComparingTo("5498.15");
        assertThat(normal.result()).isEqualTo(AffordabilityResult.PASS);
        assertThat(stricter.result()).isEqualTo(AffordabilityResult.FAIL);
    }
}
