package io.github.sibu912.snmbank.loans.repository;

import io.github.sibu912.snmbank.loans.domain.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
}
