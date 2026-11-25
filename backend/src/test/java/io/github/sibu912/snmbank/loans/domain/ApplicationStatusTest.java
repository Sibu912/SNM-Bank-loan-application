package io.github.sibu912.snmbank.loans.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationStatusTest {

    @ParameterizedTest(name = "{0} -> {1} allowed: {2}")
    @CsvSource({
            // allowed
            "SUBMITTED,    UNDER_REVIEW, true",
            "UNDER_REVIEW, APPROVED,     true",
            "UNDER_REVIEW, DECLINED,     true",
            // not allowed
            "SUBMITTED,    APPROVED,     false",
            "SUBMITTED,    DECLINED,     false",
            "SUBMITTED,    SUBMITTED,    false",
            "UNDER_REVIEW, SUBMITTED,    false",
            "APPROVED,     DECLINED,     false",
            "APPROVED,     UNDER_REVIEW, false",
            "DECLINED,     APPROVED,     false"
    })
    void knowsWhichStatusChangesAreAllowed(ApplicationStatus from, ApplicationStatus to, boolean allowed) {
        assertThat(from.canMoveTo(to)).isEqualTo(allowed);
    }
}
