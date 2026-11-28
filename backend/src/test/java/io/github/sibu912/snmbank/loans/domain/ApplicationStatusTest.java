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
            "SUBMITTED,    DECLINED,     true",
            "UNDER_REVIEW, SUBMITTED,    true",
            "APPROVED,     UNDER_REVIEW, true",
            "DECLINED,     UNDER_REVIEW, true",
            // not allowed
            "SUBMITTED,    APPROVED,     false",
            "SUBMITTED,    SUBMITTED,    false",
            "UNDER_REVIEW, UNDER_REVIEW, false",
            "APPROVED,     DECLINED,     false",
            "APPROVED,     SUBMITTED,    false",
            "DECLINED,     APPROVED,     false",
            "DECLINED,     SUBMITTED,    false"
    })
    void knowsWhichStatusChangesAreAllowed(ApplicationStatus from, ApplicationStatus to, boolean allowed) {
        assertThat(from.canMoveTo(to)).isEqualTo(allowed);
    }
}
