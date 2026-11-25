package io.github.sibu912.snmbank.loans.repository;

import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;
import io.github.sibu912.snmbank.loans.domain.LoanApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * The @EntityGraph annotations load the applicant in the same query,
 * so it is available after the transaction has finished (open-in-view is off).
 */
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    @EntityGraph(attributePaths = "applicant")
    Optional<LoanApplication> findByReferenceNumber(String referenceNumber);

    @EntityGraph(attributePaths = "applicant")
    Page<LoanApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "applicant")
    Page<LoanApplication> findAll(Pageable pageable);
}
