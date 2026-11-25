package io.github.sibu912.snmbank.loans.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Creates the JWT a reviewer gets after logging in. The Angular admin sends it back in the
 * Authorization header ("Bearer ...") on every request, so the server doesn't keep sessions.
 */
@Service
public class TokenService {

    static final String ISSUER = "snm-loan-service";

    private final JwtEncoder jwtEncoder;
    private final Duration validity;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${snm.security.token-validity-minutes:480}") long validityMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.validity = Duration.ofMinutes(validityMinutes);
    }

    public IssuedToken issue(ReviewerPrincipal reviewer) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(validity);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(reviewer.getUsername())         // who: "thandi"
                .claim("name", reviewer.getDisplayName()) // shown in the admin screen
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expiresAt);
    }
}
