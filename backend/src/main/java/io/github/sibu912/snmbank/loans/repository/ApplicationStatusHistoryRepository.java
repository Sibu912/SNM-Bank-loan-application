package io.github.sibu912.snmbank.loans.repository;

import io.github.sibu912.snmbank.loans.domain.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {

    /** Oldest change first. Ordering by id follows the order the rows were inserted. */
    List<ApplicationStatusHistory> findByLoanApplicationIdOrderByIdAsc(Long loanApplicationId);
}
