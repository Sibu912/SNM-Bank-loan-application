package io.github.sibu912.snmbank.loans.web.dto;

import io.github.sibu912.snmbank.loans.domain.Gender;
import io.github.sibu912.snmbank.loans.domain.Title;

public record ApplicantDto(
        Title title,
        String givenName,
        String surname,
        Gender gender,
        AddressDto address) {
}
