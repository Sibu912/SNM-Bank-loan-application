package io.github.sibu912.snmbank.loans.service;

import io.github.sibu912.snmbank.loans.domain.Channel;
import io.github.sibu912.snmbank.loans.domain.Gender;
import io.github.sibu912.snmbank.loans.domain.IncomeBand;
import io.github.sibu912.snmbank.loans.domain.LoanAmountBand;
import io.github.sibu912.snmbank.loans.domain.LoanPurpose;
import io.github.sibu912.snmbank.loans.domain.Title;

/**
 * Everything needed to submit an application. The REST and SOAP layers both
 * convert their own request objects into this, so the service doesn't depend on either.
 */
public record NewApplication(
        Title title,
        String givenName,
        String surname,
        Gender gender,
        String street,
        String suburb,
        String province,
        String postalCode,
        LoanPurpose loanPurpose,
        LoanAmountBand loanAmountBand,
        int termYears,
        IncomeBand incomeBand,
        Channel channel) {
}
