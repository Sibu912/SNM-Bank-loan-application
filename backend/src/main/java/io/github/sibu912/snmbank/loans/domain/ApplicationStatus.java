package io.github.sibu912.snmbank.loans.domain;

/**
 * SUBMITTED -> UNDER_REVIEW -> APPROVED or DECLINED.
 * APPROVED and DECLINED are final.
 */
public enum ApplicationStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    DECLINED;

    /** The one place that defines which status changes are allowed. */
    public boolean canMoveTo(ApplicationStatus next) {
        return switch (this) {
            case SUBMITTED -> next == UNDER_REVIEW;
            case UNDER_REVIEW -> next == APPROVED || next == DECLINED;
            case APPROVED, DECLINED -> false;
        };
    }
}
