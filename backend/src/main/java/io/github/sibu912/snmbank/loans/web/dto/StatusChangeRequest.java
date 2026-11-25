package io.github.sibu912.snmbank.loans.web.dto;

import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JSON body for PATCH /api/applications/{referenceNumber}/status.
 */
public record StatusChangeRequest(
        @NotNull ApplicationStatus status,
        @Size(max = 255) String note,
        @NotBlank @Size(max = 50) String changedBy) {
}
