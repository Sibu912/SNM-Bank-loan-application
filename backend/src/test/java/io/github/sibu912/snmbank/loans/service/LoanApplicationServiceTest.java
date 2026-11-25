package io.github.sibu912.snmbank.loans.service;

import io.github.sibu912.snmbank.loans.config.LoanProperties;
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
import io.github.sibu912.snmbank.loans.repository.ApplicantRepository;
import io.github.sibu912.snmbank.loans.repository.ApplicationStatusHistoryRepository;
import io.github.sibu912.snmbank.loans.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for the service. The repositories are Mockito mocks, so no database is used.
 * The affordability calculator is real, because it is plain Java with no dependencies.
 */
@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private ApplicationStatusHistoryRepository historyRepository;

    private LoanApplicationService service;

    @BeforeEach
    void setUp() {
        AffordabilityCalculator calculator = new AffordabilityCalculator(
                new LoanProperties(new BigDecimal("0.115"), new BigDecimal("0.30")));
        service = new LoanApplicationService(
                applicantRepository, loanApplicationRepository, historyRepository, calculator);
    }

    @Test
    void submitSavesTheApplicationWithAReferenceNumberAndAFirstHistoryRow() {
        // Arrange: save() returns what it was given; the loan application gets id 1, like the database would
        when(applicantRepository.save(any(Applicant.class))).thenAnswer(call -> call.getArgument(0));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(call -> {
            LoanApplication application = call.getArgument(0);
            ReflectionTestUtils.setField(application, "id", 1L);
            return application;
        });

        // Act
        LoanApplication result = service.submit(newApplication(LoanAmountBand.FROM_250K_TO_499K, 5));

        // Assert
        assertThat(result.getReferenceNumber()).matches("SNM-\\d{4}-000001");
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(result.getEstimatedMonthlyRepayment()).isEqualByComparingTo("10996.30");
        assertThat(result.getAffordabilityResult()).isEqualTo(AffordabilityResult.PASS);
        assertThat(result.getChannel()).isEqualTo(Channel.WEB);

        ArgumentCaptor<ApplicationStatusHistory> history = ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(historyRepository).save(history.capture());
        assertThat(history.getValue().getFromStatus()).isNull();
        assertThat(history.getValue().getToStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(history.getValue().getChangedBy()).isEqualTo("system");
    }

    @Test
    void submitStoresFailWhenTheLoanIsNotAffordable() {
        when(applicantRepository.save(any(Applicant.class))).thenAnswer(call -> call.getArgument(0));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(call -> {
            LoanApplication application = call.getArgument(0);
            ReflectionTestUtils.setField(application, "id", 2L);
            return application;
        });

        LoanApplication result = service.submit(newApplication(LoanAmountBand.FROM_500K_TO_999K, 5));

        // A failed check is saved, not rejected: the reviewer decides, but can't approve it.
        assertThat(result.getAffordabilityResult()).isEqualTo(AffordabilityResult.FAIL);
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(result.getEstimatedMonthlyRepayment()).isEqualByComparingTo("21992.61");
    }

    @Test
    void changeStatusMovesSubmittedToUnderReviewAndRecordsHistory() {
        LoanApplication application = existingApplication(AffordabilityResult.PASS);
        when(loanApplicationRepository.findByReferenceNumber("SNM-2026-000001")).thenReturn(Optional.of(application));

        LoanApplication result = service.changeStatus(
                "SNM-2026-000001", ApplicationStatus.UNDER_REVIEW, "Payslips received", "reviewer.thandi");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);

        ArgumentCaptor<ApplicationStatusHistory> history = ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(historyRepository).save(history.capture());
        assertThat(history.getValue().getFromStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(history.getValue().getToStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        assertThat(history.getValue().getNote()).isEqualTo("Payslips received");
        assertThat(history.getValue().getChangedBy()).isEqualTo("reviewer.thandi");
    }

    @Test
    void changeStatusRejectsSkippingTheReview() {
        LoanApplication application = existingApplication(AffordabilityResult.PASS);
        when(loanApplicationRepository.findByReferenceNumber("SNM-2026-000001")).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> service.changeStatus(
                "SNM-2026-000001", ApplicationStatus.APPROVED, null, "reviewer.thandi"))
                .isInstanceOf(InvalidStatusChangeException.class)
                .hasMessage("Cannot change status from SUBMITTED to APPROVED");

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        verify(historyRepository, never()).save(any());
    }

    @Test
    void changeStatusRejectsApprovingAFailedAffordabilityCheck() {
        LoanApplication application = existingApplication(AffordabilityResult.FAIL);
        application.moveTo(ApplicationStatus.UNDER_REVIEW);
        when(loanApplicationRepository.findByReferenceNumber("SNM-2026-000001")).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> service.changeStatus(
                "SNM-2026-000001", ApplicationStatus.APPROVED, null, "reviewer.thandi"))
                .isInstanceOf(InvalidStatusChangeException.class)
                .hasMessageContaining("failed the affordability check");

        verify(historyRepository, never()).save(any());
    }

    @Test
    void changeStatusAllowsDecliningAFailedAffordabilityCheck() {
        LoanApplication application = existingApplication(AffordabilityResult.FAIL);
        application.moveTo(ApplicationStatus.UNDER_REVIEW);
        when(loanApplicationRepository.findByReferenceNumber("SNM-2026-000001")).thenReturn(Optional.of(application));

        LoanApplication result = service.changeStatus(
                "SNM-2026-000001", ApplicationStatus.DECLINED, "Repayment too high", "reviewer.thandi");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.DECLINED);
    }

    @Test
    void unknownReferenceNumberThrowsNotFound() {
        when(loanApplicationRepository.findByReferenceNumber("SNM-2026-999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByReference("SNM-2026-999999"))
                .isInstanceOf(ApplicationNotFoundException.class)
                .hasMessage("Application SNM-2026-999999 not found");
    }

    @Test
    void listFiltersByStatusOnlyWhenOneIsGiven() {
        when(loanApplicationRepository.findByStatus(eq(ApplicationStatus.SUBMITTED), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(loanApplicationRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        service.list(ApplicationStatus.SUBMITTED, 0, 20);
        service.list(null, 0, 20);

        verify(loanApplicationRepository).findByStatus(eq(ApplicationStatus.SUBMITTED), any(Pageable.class));
        verify(loanApplicationRepository).findAll(any(Pageable.class));
    }

    private NewApplication newApplication(LoanAmountBand amountBand, int termYears) {
        return new NewApplication(
                Title.MR, "Thabo", "Nkosi", Gender.MALE,
                "12 Jan Smuts Avenue", "Braamfontein", "Gauteng", "2001",
                LoanPurpose.VEHICLE, amountBand, termYears, IncomeBand.FROM_50K_TO_99K, Channel.WEB);
    }

    private LoanApplication existingApplication(AffordabilityResult affordabilityResult) {
        Applicant applicant = new Applicant(Title.MR, "Thabo", "Nkosi", Gender.MALE,
                "12 Jan Smuts Avenue", "Braamfontein", "Gauteng", "2001");
        LoanApplication application = new LoanApplication(applicant, LoanPurpose.VEHICLE,
                LoanAmountBand.FROM_250K_TO_499K, 5, IncomeBand.FROM_50K_TO_99K,
                new BigDecimal("10996.30"), affordabilityResult, Channel.WEB);
        ReflectionTestUtils.setField(application, "id", 1L);
        application.assignReferenceNumber("SNM-2026-000001");
        return application;
    }
}
