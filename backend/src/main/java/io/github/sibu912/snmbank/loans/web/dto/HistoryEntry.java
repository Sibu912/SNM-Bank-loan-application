package io.github.sibu912.snmbank.loans.web.dto;

import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;

import java.time.LocalDateTime;

public record HistoryEntry(
        ApplicationStatus fromStatus,
        ApplicationStatus toStatus,
        String note,
        String changedBy,
        LocalDateTime changedAt) {
}
