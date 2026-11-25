package io.github.sibu912.snmbank.loans.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressDto(
        @NotBlank @Size(max = 150) String street,
        @NotBlank @Size(max = 100) String suburb,
        @NotBlank @Size(max = 50) String province,
        @NotNull @Pattern(regexp = "\\d{4}", message = "must be exactly 4 digits") String postalCode) {
}
