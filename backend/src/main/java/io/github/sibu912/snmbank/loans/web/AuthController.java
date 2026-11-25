package io.github.sibu912.snmbank.loans.web;

import io.github.sibu912.snmbank.loans.security.IssuedToken;
import io.github.sibu912.snmbank.loans.security.ReviewerPrincipal;
import io.github.sibu912.snmbank.loans.security.TokenService;
import io.github.sibu912.snmbank.loans.web.dto.CurrentUserResponse;
import io.github.sibu912.snmbank.loans.web.dto.LoginRequest;
import io.github.sibu912.snmbank.loans.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/**
 * Login for reviewers. A wrong username or password throws an AuthenticationException,
 * which GlobalExceptionHandler turns into a 401.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, request.password()));

        ReviewerPrincipal reviewer = (ReviewerPrincipal) authentication.getPrincipal();
        IssuedToken token = tokenService.issue(reviewer);
        return new LoginResponse(token.value(), reviewer.getUsername(), reviewer.getDisplayName(), token.expiresAt());
    }

    /** Who is logged in, read from the token itself. */
    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal Jwt token) {
        return new CurrentUserResponse(token.getSubject(), token.getClaimAsString("name"));
    }
}
