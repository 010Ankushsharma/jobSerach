package com.jobportal.controller;

import com.jobportal.dto.request.ApplicationRequest;
import com.jobportal.dto.request.ApplicationStatusUpdateRequest;
import com.jobportal.dto.response.ApiResponse;
import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.dto.response.PageResponse;
import com.jobportal.model.enums.ApplicationStatus;
import com.jobportal.security.JwtTokenProvider;
import com.jobportal.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);
    
    private final ApplicationService applicationService;
    private final JwtTokenProvider jwtTokenProvider;

    public ApplicationController(ApplicationService applicationService, 
                                JwtTokenProvider jwtTokenProvider) {
        this.applicationService = applicationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyForJob(
            @Valid @RequestBody ApplicationRequest request,
            HttpServletRequest httpRequest) {
        logger.info("Application request for job: {}", request.getJobId());
        String token = extractToken(httpRequest);
        String candidateId = jwtTokenProvider.getUserIdFromToken(token);
        ApplicationResponse application = applicationService.applyForJob(request, candidateId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Application submitted successfully", application));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(@PathVariable String id) {
        logger.debug("Fetching application by ID: {}", id);
        ApplicationResponse application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.success(application));
    }

    @GetMapping("/my-applications")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationResponse>>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        logger.debug("Fetching applications for candidate");
        String token = extractToken(httpRequest);
        String candidateId = jwtTokenProvider.getUserIdFromToken(token);
        PageResponse<ApplicationResponse> applications = 
            applicationService.getApplicationsByCandidate(candidateId, page, size);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationResponse>>> getApplicationsByJob(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("Fetching applications for job: {}", jobId);
        PageResponse<ApplicationResponse> applications = 
            applicationService.getApplicationsByJob(jobId, page, size);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/job/{jobId}/status/{status}")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationResponse>>> getApplicationsByJobAndStatus(
            @PathVariable String jobId,
            @PathVariable ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("Fetching applications for job: {} with status: {}", jobId, status);
        PageResponse<ApplicationResponse> applications = 
            applicationService.getApplicationsByJobAndStatus(jobId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable String id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request,
            HttpServletRequest httpRequest) {
        logger.info("Updating application status: {}", id);
        String token = extractToken(httpRequest);
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        ApplicationResponse application = 
            applicationService.updateApplicationStatus(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Application status updated successfully", application));
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

