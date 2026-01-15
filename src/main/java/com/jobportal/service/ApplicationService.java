package com.jobportal.service;

import com.jobportal.dto.request.ApplicationRequest;
import com.jobportal.dto.request.ApplicationStatusUpdateRequest;
import com.jobportal.dto.response.ApplicationResponse;
import com.jobportal.dto.response.PageResponse;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.exception.UnauthorizedException;
import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.model.enums.ApplicationStatus;
import com.jobportal.repository.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    
    private final ApplicationRepository applicationRepository;
    private final JobService jobService;
    private final UserService userService;

    public ApplicationService(ApplicationRepository applicationRepository, 
                             JobService jobService, 
                             UserService userService) {
        this.applicationRepository = applicationRepository;
        this.jobService = jobService;
        this.userService = userService;
    }

    @Transactional
    public ApplicationResponse applyForJob(ApplicationRequest request, String candidateId) {
        logger.info("Candidate {} applying for job {}", candidateId, request.getJobId());
        
        User candidate = userService.getUserEntity(candidateId);
        
        // Verify user is candidate
        if (candidate.getRole() != com.jobportal.model.enums.Role.CANDIDATE) {
            throw new UnauthorizedException("Only candidates can apply for jobs");
        }
        
        Job job = jobService.getJobEntity(request.getJobId());
        
        // Check if job is active
        if (!job.getIsActive()) {
            throw new IllegalArgumentException("Cannot apply to inactive job");
        }
        
        // Check for duplicate application
        if (applicationRepository.existsByCandidate_IdAndJob_Id(candidateId, request.getJobId())) {
            throw new IllegalArgumentException("You have already applied for this job");
        }
        
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setResume(request.getResume());
        application.setCoverLetter(request.getCoverLetter());
        application.setStatus(ApplicationStatus.APPLIED);
        
        application = applicationRepository.save(application);
        logger.info("Application created successfully with ID: {}", application.getId());
        
        return mapToResponse(application);
    }

    public ApplicationResponse getApplicationById(String id) {
        logger.debug("Fetching application by ID: {}", id);
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        return mapToResponse(application);
    }

    public PageResponse<ApplicationResponse> getApplicationsByCandidate(String candidateId, int page, int size) {
        logger.debug("Fetching applications for candidate: {}", candidateId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<Application> applicationPage = applicationRepository.findByCandidate_Id(candidateId, pageable);
        
        List<ApplicationResponse> content = applicationPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<>(
            content,
            applicationPage.getNumber(),
            applicationPage.getSize(),
            applicationPage.getTotalElements()
        );
    }

    public PageResponse<ApplicationResponse> getApplicationsByJob(String jobId, int page, int size) {
        logger.debug("Fetching applications for job: {}", jobId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<Application> applicationPage = applicationRepository.findByJob_Id(jobId, pageable);
        
        List<ApplicationResponse> content = applicationPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<>(
            content,
            applicationPage.getNumber(),
            applicationPage.getSize(),
            applicationPage.getTotalElements()
        );
    }

    public PageResponse<ApplicationResponse> getApplicationsByJobAndStatus(String jobId, 
                                                                          ApplicationStatus status, 
                                                                          int page, int size) {
        logger.debug("Fetching applications for job: {} with status: {}", jobId, status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());
        Page<Application> applicationPage = applicationRepository.findByJob_IdAndStatus(jobId, status, pageable);
        
        List<ApplicationResponse> content = applicationPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<>(
            content,
            applicationPage.getNumber(),
            applicationPage.getSize(),
            applicationPage.getTotalElements()
        );
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(String applicationId, 
                                                      ApplicationStatusUpdateRequest request, 
                                                      String userId) {
        logger.info("Updating application {} status to {} by user {}", 
                   applicationId, request.getStatus(), userId);
        
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        
        User user = userService.getUserEntity(userId);
        
        // Verify user is recruiter/admin and owns the job
        Job job = application.getJob();
        if (user.getRole() != com.jobportal.model.enums.Role.ADMIN && 
            !job.getPostedBy().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to update this application");
        }
        
        application.setStatus(request.getStatus());
        application.setNotes(request.getNotes());
        application.setReviewedAt(java.time.LocalDateTime.now());
        
        application = applicationRepository.save(application);
        logger.info("Application status updated successfully: {}", applicationId);
        
        return mapToResponse(application);
    }

    private ApplicationResponse mapToResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setCandidateId(application.getCandidate().getId());
        response.setCandidateName(application.getCandidate().getFirstName() + " " + 
                                 application.getCandidate().getLastName());
        response.setJobId(application.getJob().getId());
        response.setJobTitle(application.getJob().getTitle());
        response.setStatus(application.getStatus());
        response.setResume(application.getResume());
        response.setCoverLetter(application.getCoverLetter());
        response.setAppliedAt(application.getAppliedAt());
        response.setReviewedAt(application.getReviewedAt());
        response.setNotes(application.getNotes());
        return response;
    }
}

