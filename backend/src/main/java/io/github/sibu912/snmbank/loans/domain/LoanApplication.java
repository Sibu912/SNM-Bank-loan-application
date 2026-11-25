package io.github.sibu912.snmbank.loans.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A loan request (page 2 of the form) plus what the system works out:
 * the estimated repayment, the affordability result and the current status.
 */
@Entity
@Table(name = "loan_application")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Set straight after the insert, because it is built from the generated id. */
    @Column(name = "reference_number", unique = true, length = 20)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_purpose", nullable = false, length = 20)
    private LoanPurpose loanPurpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_amount_band", nullable = false, length = 30)
    private LoanAmountBand loanAmountBand;

    @Column(name = "term_years", nullable = false)
    private int termYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_band", nullable = false, length = 30)
    private IncomeBand incomeBand;

    @Column(name = "estimated_monthly_repayment", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedMonthlyRepayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "affordability_result", nullable = false, length = 10)
    private AffordabilityResult affordabilityResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 10)
    private Channel channel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Required by JPA. */
    protected LoanApplication() {
    }

    public LoanApplication(Applicant applicant, LoanPurpose loanPurpose, LoanAmountBand loanAmountBand,
                           int termYears, IncomeBand incomeBand, BigDecimal estimatedMonthlyRepayment,
                           AffordabilityResult affordabilityResult, Channel channel) {
        this.applicant = applicant;
        this.loanPurpose = loanPurpose;
        this.loanAmountBand = loanAmountBand;
        this.termYears = termYears;
        this.incomeBand = incomeBand;
        this.estimatedMonthlyRepayment = estimatedMonthlyRepayment;
        this.affordabilityResult = affordabilityResult;
        this.channel = channel;
        this.status = ApplicationStatus.SUBMITTED;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        createdAt = now;
        updatedAt = now;
    }

    public void assignReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    /** Changes the status. The service checks the rules before calling this. */
    public void moveTo(ApplicationStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public Long getId() {
        return id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public LoanPurpose getLoanPurpose() {
        return loanPurpose;
    }

    public LoanAmountBand getLoanAmountBand() {
        return loanAmountBand;
    }

    public int getTermYears() {
        return termYears;
    }

    public IncomeBand getIncomeBand() {
        return incomeBand;
    }

    public BigDecimal getEstimatedMonthlyRepayment() {
        return estimatedMonthlyRepayment;
    }

    public AffordabilityResult getAffordabilityResult() {
        return affordabilityResult;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public Channel getChannel() {
        return channel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
