package io.github.sibu912.snmbank.loans.service;

import io.github.sibu912.snmbank.loans.domain.AffordabilityResult;
import io.github.sibu912.snmbank.loans.domain.Applicant;
import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;
import io.github.sibu912.snmbank.loans.domain.ApplicationStatusHistory;
import io.github.sibu912.snmbank.loans.domain.LoanApplication;
import io.github.sibu912.snmbank.loans.repository.ApplicantRepository;
import io.github.sibu912.snmbank.loans.repository.ApplicationStatusHistoryRepository;
import io.github.sibu912.snmbank.loans.repository.LoanApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * The business logic. Both the REST controller and the SOAP endpoint call this class,
 * so the rules live in one place.
 */
@Service
@Transactional
public class LoanApplicationService {

    static final String SYSTEM_USER = "system";

    private final ApplicantRepository applicantRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final AffordabilityCalculator affordabilityCalculator;

    public LoanApplicationService(ApplicantRepository applicantRepository,
                                  LoanApplicationRepository loanApplicationRepository,
                                  ApplicationStatusHistoryRepository historyRepository,
                                  AffordabilityCalculator affordabilityCalculator) {
        this.applicantRepository = applicantRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.historyRepository = historyRepository;
        this.affordabilityCalculator = affordabilityCalculator;
    }

    public LoanApplication submit(NewApplication request) {
        Applicant applicant = applicantRepository.save(new Applicant(
                request.title(), request.givenName(), request.surname(), request.gender(),
                request.street(), request.suburb(), request.province(), request.postalCode()));

        AffordabilityAssessment assessment = affordabilityCalculator.assess(
                request.loanAmountBand(), request.termYears(), request.incomeBand());

        LoanApplication application = loanApplicationRepository.save(new LoanApplication(
                applicant, request.loanPurpose(), request.loanAmountBand(), request.termYears(),
                request.incomeBand(), assessment.monthlyRepayment(), assessment.result(), request.channel()));

        // The reference number is built from the generated id, so it can only be set after the insert.
        // Hibernate saves this change when the transaction commits.
        application.assignReferenceNumber(referenceNumberFor(application));

        historyRepository.save(new ApplicationStatusHistory(
                application, null, ApplicationStatus.SUBMITTED, null, SYSTEM_USER));
        return application;
    }

    @Transactional(readOnly = true)
    public LoanApplication findByReference(String referenceNumber) {
        return loanApplicationRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ApplicationNotFoundException(referenceNumber));
    }

    /** Newest first. When status is null, all applications are returned. */
    @Transactional(readOnly = true)
    public Page<LoanApplication> list(ApplicationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        if (status == null) {
            return loanApplicationRepository.findAll(pageable);
        }
        return loanApplicationRepository.findByStatus(status, pageable);
    }

    public LoanApplication changeStatus(String referenceNumber, ApplicationStatus newStatus,
                                        String note, String changedBy) {
        LoanApplication application = findByReference(referenceNumber);
        ApplicationStatus currentStatus = application.getStatus();

        if (!currentStatus.canMoveTo(newStatus)) {
            throw new InvalidStatusChangeException(
                    "Cannot change status from " + currentStatus + " to " + newStatus);
        }
        if (newStatus == ApplicationStatus.APPROVED
                && application.getAffordabilityResult() == AffordabilityResult.FAIL) {
            throw new InvalidStatusChangeException(
                    "Cannot approve an application that failed the affordability check");
        }

        application.moveTo(newStatus);
        historyRepository.save(new ApplicationStatusHistory(
                application, currentStatus, newStatus, note, changedBy));
        return application;
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusHistory> history(String referenceNumber) {
        LoanApplication application = findByReference(referenceNumber);
        return historyRepository.findByLoanApplicationIdOrderByIdAsc(application.getId());
    }

    /** For example SNM-2026-000001. */
    private String referenceNumberFor(LoanApplication application) {
        return String.format("SNM-%d-%06d", LocalDate.now().getYear(), application.getId());
    }
}
