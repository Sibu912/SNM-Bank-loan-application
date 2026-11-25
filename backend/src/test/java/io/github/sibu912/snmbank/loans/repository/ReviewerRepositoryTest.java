package io.github.sibu912.snmbank.loans.repository;

import io.github.sibu912.snmbank.loans.domain.Reviewer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the reviewer table created by V2__create_reviewer_table.sql.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewerRepositoryTest {

    @Autowired
    private ReviewerRepository reviewerRepository;

    @Test
    void theDemoReviewersExistWithHashedPasswords() {
        Reviewer thandi = reviewerRepository.findByUsername("thandi").orElseThrow();

        assertThat(thandi.getDisplayName()).isEqualTo("Thandi Mokoena");
        assertThat(thandi.isEnabled()).isTrue();
        assertThat(thandi.getPasswordHash()).startsWith("$2a$").isNotEqualTo("Reviewer#2026");
        assertThat(new BCryptPasswordEncoder().matches("Reviewer#2026", thandi.getPasswordHash())).isTrue();
        assertThat(reviewerRepository.findByUsername("sipho")).isPresent();
    }

    @Test
    void returnsEmptyForAnUnknownUsername() {
        assertThat(reviewerRepository.findByUsername("nobody")).isEmpty();
    }
}
