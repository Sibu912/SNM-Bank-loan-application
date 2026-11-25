package io.github.sibu912.snmbank.loans.security;

import io.github.sibu912.snmbank.loans.repository.ReviewerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Tells Spring Security how to find a reviewer: from the reviewer table in PostgreSQL.
 */
@Service
public class ReviewerUserDetailsService implements UserDetailsService {

    private final ReviewerRepository reviewerRepository;

    public ReviewerUserDetailsService(ReviewerRepository reviewerRepository) {
        this.reviewerRepository = reviewerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return reviewerRepository.findByUsername(username)
                .map(ReviewerPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("No reviewer called " + username));
    }
}
