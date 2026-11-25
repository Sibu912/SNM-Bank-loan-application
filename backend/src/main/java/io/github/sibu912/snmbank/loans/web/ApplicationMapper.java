package io.github.sibu912.snmbank.loans.web;

import io.github.sibu912.snmbank.loans.domain.Applicant;
import io.github.sibu912.snmbank.loans.domain.ApplicationStatusHistory;
import io.github.sibu912.snmbank.loans.domain.LoanApplication;
import io.github.sibu912.snmbank.loans.web.dto.AddressDto;
import io.github.sibu912.snmbank.loans.web.dto.ApplicantDto;
import io.github.sibu912.snmbank.loans.web.dto.ApplicationDetail;
import io.github.sibu912.snmbank.loans.web.dto.ApplicationSummary;
import io.github.sibu912.snmbank.loans.web.dto.HistoryEntry;
import io.github.sibu912.snmbank.loans.web.dto.SubmitApplicationResponse;

/**
 * Converts entities into the DTOs the API returns. Entities never leave the backend.
 */
final class ApplicationMapper {

    private ApplicationMapper() {
    }

    static SubmitApplicationResponse toSubmitResponse(LoanApplication application) {
        return new SubmitApplicationResponse(
                application.getReferenceNumber(),
                application.getStatus(),
                application.getAffordabilityResult(),
                application.getEstimatedMonthlyRepayment(),
                application.getCreatedAt());
    }

    static ApplicationSummary toSummary(LoanApplication application) {
        return new ApplicationSummary(
                application.getReferenceNumber(),
                application.getApplicant().getFullName(),
                application.getLoanPurpose(),
                application.getLoanAmountBand(),
                application.getStatus(),
                application.getAffordabilityResult(),
                application.getCreatedAt());
    }

    static ApplicationDetail toDetail(LoanApplication application) {
        return new ApplicationDetail(
                application.getReferenceNumber(),
                toApplicantDto(application.getApplicant()),
                application.getLoanPurpose(),
                application.getLoanAmountBand(),
                application.getTermYears(),
                application.getIncomeBand(),
                application.getEstimatedMonthlyRepayment(),
                application.getAffordabilityResult(),
                application.getStatus(),
                application.getChannel(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }

    static HistoryEntry toHistoryEntry(ApplicationStatusHistory history) {
        return new HistoryEntry(
                history.getFromStatus(),
                history.getToStatus(),
                history.getNote(),
                history.getChangedBy(),
                history.getChangedAt());
    }

    private static ApplicantDto toApplicantDto(Applicant applicant) {
        AddressDto address = new AddressDto(
                applicant.getStreet(), applicant.getSuburb(), applicant.getProvince(), applicant.getPostalCode());
        return new ApplicantDto(
                applicant.getTitle(), applicant.getGivenName(), applicant.getSurname(), applicant.getGender(), address);
    }
}
