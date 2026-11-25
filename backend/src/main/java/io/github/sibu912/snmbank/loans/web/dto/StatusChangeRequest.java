package io.github.sibu912.snmbank.loans.web.dto;

import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JSON body for PATCH /api/applications/{referenceNumber}/status.
 * Who made the change is taken from the login token, not from the body.
 */
public record StatusChangeRequest(
        @NotNull ApplicationStatus status,
        @Size(max = 255) String note) {
}
