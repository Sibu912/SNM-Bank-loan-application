package io.github.sibu912.snmbank.loans.repository;

import io.github.sibu912.snmbank.loans.domain.AffordabilityResult;
import io.github.sibu912.snmbank.loans.domain.Applicant;
import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;
import io.github.sibu912.snmbank.loans.domain.ApplicationStatusHistory;
import io.github.sibu912.snmbank.loans.domain.Channel;
import io.github.sibu912.snmbank.loans.domain.Gender;
import io.github.sibu912.snmbank.loans.domain.IncomeBand;
import io.github.sibu912.snmbank.loans.domain.LoanAmountBand;
import io.github.sibu912.snmbank.loans.domain.LoanApplication;
import io.github.sibu912.snmbank.loans.domain.LoanPurpose;
import io.github.sibu912.snmbank.loans.domain.Title;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against the PostgreSQL test database (profile "test"). Flyway creates the tables
 * from the migration scripts, and Hibernate checks the entities match them.
 * Each test runs in a transaction that is rolled back afterwards.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("/clean-loan-tables.sql")
class LoanApplicationRepositoryTest {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private ApplicationStatusHistoryRepository historyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAnApplicationAndFindsItByReferenceNumber() {
        LoanApplication saved = saveApplication("SNM-2026-000001");
        entityManager.flush();
        entityManager.clear(); // forces the next query to read from the database

        Optional<LoanApplication> found = loanApplicationRepository.findByReferenceNumber("SNM-2026-000001");

        assertThat(found).isPresent();
        LoanApplication application = found.get();
        assertThat(application.getId()).isEqualTo(saved.getId());
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(application.getEstimatedMonthlyRepayment()).isEqualByComparingTo("10996.30");
        assertThat(application.getApplicant().getFullName()).isEqualTo("Mr Thabo Nkosi");
        assertThat(application.getCreatedAt()).isNotNull();
    }

    @Test
    void returnsEmptyForAnUnknownReferenceNumber() {
        assertThat(loanApplicationRepository.findByReferenceNumber("SNM-2026-999999")).isEmpty();
    }

    @Test
    void findsApplicationsByStatus() {
        saveApplication("SNM-2026-000001");
        saveApplication("SNM-2026-000002");

        Page<LoanApplication> submitted =
                loanApplicationRepository.findByStatus(ApplicationStatus.SUBMITTED, PageRequest.of(0, 10));
        Page<LoanApplication> approved =
                loanApplicationRepository.findByStatus(ApplicationStatus.APPROVED, PageRequest.of(0, 10));

        assertThat(submitted.getTotalElements()).isEqualTo(2);
        assertThat(approved.getTotalElements()).isZero();
    }

    @Test
    void returnsHistoryOldestFirst() {
        LoanApplication application = saveApplication("SNM-2026-000001");
        historyRepository.save(new ApplicationStatusHistory(
                application, null, ApplicationStatus.SUBMITTED, null, "system"));
        historyRepository.save(new ApplicationStatusHistory(
                application, ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW,
                "Payslips received", "reviewer.thandi"));

        List<ApplicationStatusHistory> history =
                historyRepository.findByLoanApplicationIdOrderByIdAsc(application.getId());

        assertThat(history).extracting(ApplicationStatusHistory::getToStatus)
                .containsExactly(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW);
        assertThat(history.get(1).getChangedBy()).isEqualTo("reviewer.thandi");
    }

    private LoanApplication saveApplication(String referenceNumber) {
        Applicant applicant = applicantRepository.save(new Applicant(
                Title.MR, "Thabo", "Nkosi", Gender.MALE,
                "12 Jan Smuts Avenue", "Braamfontein", "Gauteng", "2001"));

        LoanApplication application = new LoanApplication(
                applicant, LoanPurpose.VEHICLE, LoanAmountBand.FROM_250K_TO_499K, 5,
                IncomeBand.FROM_50K_TO_99K, new BigDecimal("10996.30"), AffordabilityResult.PASS, Channel.WEB);
        application = loanApplicationRepository.save(application);
        application.assignReferenceNumber(referenceNumber);
        return application;
    }
}
