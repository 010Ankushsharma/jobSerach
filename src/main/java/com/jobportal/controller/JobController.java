package com.jobportal.controller;

import com.jobportal.dto.request.JobCreateRequest;
import com.jobportal.dto.response.ApiResponse;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.PageResponse;
import com.jobportal.security.CurrentUser;
import com.jobportal.security.JwtTokenProvider;
import com.jobportal.service.JobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {
    
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    
    private final JobService jobService;
    private final JwtTokenProvider jwtTokenProvider;

    public JobController(JobService jobService, JwtTokenProvider jwtTokenProvider) {
        this.jobService = jobService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobResponse>>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        logger.debug("Fetching all jobs - page: {}, size: {}", page, size);
        PageResponse<JobResponse> jobs = jobService.getAllActiveJobs(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable String id) {
        logger.debug("Fetching job by ID: {}", id);
        JobResponse job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<JobResponse>>> searchJobs(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("Searching jobs with query: {}", q);
        PageResponse<JobResponse> jobs = jobService.searchJobs(q, page, size);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PageResponse<JobResponse>>> filterJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) Integer experienceRequired,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("Filtering jobs");
        PageResponse<JobResponse> jobs = jobService.filterJobs(title, location, skills, 
                                                               experienceRequired, page, size);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody JobCreateRequest request,
            HttpServletRequest httpRequest) {
        logger.info("Creating new job: {}", request.getTitle());
        String token = extractToken(httpRequest);
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        JobResponse job = jobService.createJob(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Job created successfully", job));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable String id,
            @Valid @RequestBody JobCreateRequest request,
            HttpServletRequest httpRequest) {
        logger.info("Updating job: {}", id);
        String token = extractToken(httpRequest);
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        JobResponse job = jobService.updateJob(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", job));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteJob(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        logger.info("Deleting job: {}", id);
        String token = extractToken(httpRequest);
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        jobService.deleteJob(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }

    @GetMapping("/recruiter/my-jobs")
    public ResponseEntity<ApiResponse<PageResponse<JobResponse>>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        logger.debug("Fetching jobs by recruiter");
        String token = extractToken(httpRequest);
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        PageResponse<JobResponse> jobs = jobService.getJobsByRecruiter(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

