package io.github.sibu912.snmbank.loans;

import io.github.sibu912.snmbank.loans.domain.AffordabilityResult;
import io.github.sibu912.snmbank.loans.domain.Applicant;
import io.github.sibu912.snmbank.loans.domain.Channel;
import io.github.sibu912.snmbank.loans.domain.Gender;
import io.github.sibu912.snmbank.loans.domain.IncomeBand;
import io.github.sibu912.snmbank.loans.domain.LoanAmountBand;
import io.github.sibu912.snmbank.loans.domain.LoanApplication;
import io.github.sibu912.snmbank.loans.domain.LoanPurpose;
import io.github.sibu912.snmbank.loans.domain.Title;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ready-made objects for tests that don't use a database.
 * Fields that the database would normally fill in (id, timestamps) are set by reflection.
 */
public final class TestData {

    public static final String REFERENCE = "SNM-2026-000001";
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 25, 18, 30);

    private TestData() {
    }

    public static LoanApplication loanApplication(AffordabilityResult affordabilityResult) {
        Applicant applicant = new Applicant(Title.MR, "Thabo", "Nkosi", Gender.MALE,
                "12 Jan Smuts Avenue", "Braamfontein", "Gauteng", "2001");
        LoanApplication application = new LoanApplication(applicant, LoanPurpose.VEHICLE,
                LoanAmountBand.FROM_250K_TO_499K, 5, IncomeBand.FROM_50K_TO_99K,
                new BigDecimal("10996.30"), affordabilityResult, Channel.WEB);
        ReflectionTestUtils.setField(application, "id", 1L);
        ReflectionTestUtils.setField(application, "createdAt", CREATED_AT);
        ReflectionTestUtils.setField(application, "updatedAt", CREATED_AT);
        application.assignReferenceNumber(REFERENCE);
        return application;
    }

    /** The JSON body from docs/API.md, endpoint 1. */
    public static String submitJson(String postalCode, int termYears) {
        return """
                {
                  "title": "MR",
                  "givenName": "Thabo",
                  "surname": "Nkosi",
                  "gender": "MALE",
                  "address": {
                    "street": "12 Jan Smuts Avenue",
                    "suburb": "Braamfontein",
                    "province": "Gauteng",
                    "postalCode": "%s"
                  },
                  "loanPurpose": "VEHICLE",
                  "loanAmountBand": "FROM_250K_TO_499K",
                  "termYears": %d,
                  "incomeBand": "FROM_50K_TO_99K"
                }
                """.formatted(postalCode, termYears);
    }
}
