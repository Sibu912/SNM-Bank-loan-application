package io.github.sibu912.snmbank.loans.security;

import java.time.Instant;

/**
 * A signed JWT and the moment it stops being valid.
 */
public record IssuedToken(String value, Instant expiresAt) {
}
