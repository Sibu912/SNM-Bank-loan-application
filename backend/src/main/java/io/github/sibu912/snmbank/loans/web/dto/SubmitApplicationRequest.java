package io.github.sibu912.snmbank.loans.web.dto;

import io.github.sibu912.snmbank.loans.domain.Channel;
import io.github.sibu912.snmbank.loans.domain.Gender;
import io.github.sibu912.snmbank.loans.domain.IncomeBand;
import io.github.sibu912.snmbank.loans.domain.LoanAmountBand;
import io.github.sibu912.snmbank.loans.domain.LoanPurpose;
import io.github.sibu912.snmbank.loans.domain.Title;
import io.github.sibu912.snmbank.loans.service.NewApplication;
import io.github.sibu912.snmbank.loans.web.validation.AllowedLoanTerm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JSON body for POST /api/applications. The rules match docs/DESIGN.md section 6.1.
 */
public record SubmitApplicationRequest(
        @NotNull Title title,
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String surname,
        @NotNull Gender gender,
        @NotNull @Valid AddressDto address,
        @NotNull LoanPurpose loanPurpose,
        @NotNull LoanAmountBand loanAmountBand,
        @NotNull @AllowedLoanTerm Integer termYears,
        @NotNull IncomeBand incomeBand) {

    public NewApplication toNewApplication(Channel channel) {
        return new NewApplication(
                title, givenName.trim(), surname.trim(), gender,
                address.street().trim(), address.suburb().trim(), address.province().trim(), address.postalCode(),
                loanPurpose, loanAmountBand, termYears, incomeBand, channel);
    }
}
