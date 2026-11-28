package io.github.sibu912.snmbank.loans.domain;

/**
 * The normal flow is SUBMITTED -> UNDER_REVIEW -> APPROVED or DECLINED.
 * A reviewer can also:
 *  - decline a SUBMITTED application straight away,
 *  - send an UNDER_REVIEW application back to SUBMITTED,
 *  - reopen an APPROVED or DECLINED application (back to UNDER_REVIEW).
 * Approving always needs a review first.
 */
public enum ApplicationStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    DECLINED;

    /** The one place that defines which status changes are allowed. */
    public boolean canMoveTo(ApplicationStatus next) {
        return switch (this) {
            case SUBMITTED -> next == UNDER_REVIEW || next == DECLINED;
            case UNDER_REVIEW -> next == APPROVED || next == DECLINED || next == SUBMITTED;
            case APPROVED, DECLINED -> next == UNDER_REVIEW;
        };
    }
}
