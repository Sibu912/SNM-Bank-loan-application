package io.github.sibu912.snmbank.loans.repository;

import io.github.sibu912.snmbank.loans.domain.Reviewer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewerRepository extends JpaRepository<Reviewer, Long> {

    Optional<Reviewer> findByUsername(String username);
}
