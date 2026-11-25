package io.github.sibu912.snmbank.loans.web;

import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;
import io.github.sibu912.snmbank.loans.domain.Channel;
import io.github.sibu912.snmbank.loans.domain.LoanApplication;
import io.github.sibu912.snmbank.loans.service.LoanApplicationService;
import io.github.sibu912.snmbank.loans.web.dto.ApplicationDetail;
import io.github.sibu912.snmbank.loans.web.dto.ApplicationSummary;
import io.github.sibu912.snmbank.loans.web.dto.HistoryEntry;
import io.github.sibu912.snmbank.loans.web.dto.PageResponse;
import io.github.sibu912.snmbank.loans.web.dto.StatusChangeRequest;
import io.github.sibu912.snmbank.loans.web.dto.SubmitApplicationRequest;
import io.github.sibu912.snmbank.loans.web.dto.SubmitApplicationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * REST endpoints for loan applications (see docs/API.md).
 * Kept thin: it validates input, calls the service and maps the result to DTOs.
 * Submitting is public (the loan form); everything else needs a reviewer login (SecurityConfig).
 */
@RestController
@RequestMapping("/api/applications")
public class LoanApplicationController {

    static final int MAX_PAGE_SIZE = 100;

    private final LoanApplicationService service;

    public LoanApplicationController(LoanApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SubmitApplicationResponse> submit(@Valid @RequestBody SubmitApplicationRequest request) {
        LoanApplication application = service.submit(request.toNewApplication(Channel.WEB));
        URI location = URI.create("/api/applications/" + application.getReferenceNumber());
        return ResponseEntity.created(location).body(ApplicationMapper.toSubmitResponse(application));
    }

    @GetMapping
    public PageResponse<ApplicationSummary> list(@RequestParam(required = false) ApplicationStatus status,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        return PageResponse.from(service.list(status, safePage, safeSize).map(ApplicationMapper::toSummary));
    }

    @GetMapping("/{referenceNumber}")
    public ApplicationDetail get(@PathVariable String referenceNumber) {
        return ApplicationMapper.toDetail(service.findByReference(referenceNumber));
    }

    /** The reviewer making the change is the logged-in user (the "sub" of their JWT). */
    @PatchMapping("/{referenceNumber}/status")
    public ApplicationDetail changeStatus(@PathVariable String referenceNumber,
                                          @Valid @RequestBody StatusChangeRequest request,
                                          @AuthenticationPrincipal Jwt reviewer) {
        LoanApplication application = service.changeStatus(
                referenceNumber, request.status(), request.note(), reviewer.getSubject());
        return ApplicationMapper.toDetail(application);
    }

    @GetMapping("/{referenceNumber}/history")
    public List<HistoryEntry> history(@PathVariable String referenceNumber) {
        return service.history(referenceNumber).stream()
                .map(ApplicationMapper::toHistoryEntry)
                .toList();
    }
}
