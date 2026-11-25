package io.github.sibu912.snmbank.loans.web.dto;

import java.time.Instant;

/**
 * Returned after a successful login. The client sends the token back as "Authorization: Bearer token".
 */
public record LoginResponse(
        String token,
        String username,
        String displayName,
        Instant expiresAt) {
}
