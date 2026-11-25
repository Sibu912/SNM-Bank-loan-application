package io.github.sibu912.snmbank.loans.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Who can call what.
 * <ul>
 *   <li>Public: logging in, submitting the loan form, and SOAP for partner systems</li>
 *   <li>Everything else (the admin API) needs a valid JWT: "Authorization: Bearer ..."</li>
 * </ul>
 * Tokens are signed with an HMAC-SHA256 key from snm.security.jwt-secret (the JWT_SECRET
 * environment variable). If none is set, a random key is made at start-up, so tokens stop
 * working when the app restarts - fine for local use.
 */
@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final int MIN_SECRET_BYTES = 32; // HS256 needs at least 256 bits

    private final SecretKey signingKey;

    public SecurityConfig(@Value("${snm.security.jwt-secret:}") String jwtSecret) {
        this.signingKey = createSigningKey(jwtSecret);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())                 // uses CorsConfig
                .csrf(AbstractHttpConfigurer::disable)           // no cookies, so no CSRF risk
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/applications").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())); // checks the Bearer token
        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return NimbusJwtEncoder.withSecretKey(signingKey).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(signingKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private static SecretKey createSigningKey(String secret) {
        byte[] keyBytes;
        if (secret == null || secret.isBlank()) {
            log.info("No JWT_SECRET set - using a random signing key. Logins end when the app restarts.");
            keyBytes = new byte[MIN_SECRET_BYTES];
            new SecureRandom().nextBytes(keyBytes);
        } else {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < MIN_SECRET_BYTES) {
                throw new IllegalStateException("JWT_SECRET must be at least " + MIN_SECRET_BYTES + " characters long");
            }
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
}
