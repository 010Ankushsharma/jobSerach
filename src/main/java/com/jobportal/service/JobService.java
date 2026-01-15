package com.jobportal.service;

import com.jobportal.dto.request.JobCreateRequest;
import com.jobportal.dto.response.JobResponse;
import com.jobportal.dto.response.PageResponse;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.exception.UnauthorizedException;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.JobRepository;
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
public class JobService {
    
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    
    private final JobRepository jobRepository;
    private final UserService userService;

    public JobService(JobRepository jobRepository, UserService userService) {
        this.jobRepository = jobRepository;
        this.userService = userService;
    }

    @Transactional
    public JobResponse createJob(JobCreateRequest request, String userId) {
        logger.info("Creating new job: {} by user: {}", request.getTitle(), userId);
        
        User recruiter = userService.getUserEntity(userId);
        
        // Verify user is recruiter or admin
        if (recruiter.getRole() != com.jobportal.model.enums.Role.RECRUITER && 
            recruiter.getRole() != com.jobportal.model.enums.Role.ADMIN) {
            throw new UnauthorizedException("Only recruiters and admins can create jobs");
        }
        
        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSkills(request.getSkills());
        job.setExperienceRequired(request.getExperienceRequired());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setEmploymentType(request.getEmploymentType());
        job.setPostedBy(recruiter);
        
        job = jobRepository.save(job);
        logger.info("Job created successfully with ID: {}", job.getId());
        
        return mapToResponse(job);
    }

    public JobResponse getJobById(String id) {
        logger.debug("Fetching job by ID: {}", id);
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        return mapToResponse(job);
    }

    public PageResponse<JobResponse> getAllActiveJobs(int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching all active jobs - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Job> jobPage = jobRepository.findByIsActiveTrue(pageable);
        
        List<JobResponse> content = jobPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<>(
            content,
            jobPage.getNumber(),
            jobPage.getSize(),
            jobPage.getTotalElements()
        );
    }

    public PageResponse<JobResponse> searchJobs(String searchTerm, int page, int size) {
        logger.debug("Searching jobs with term: {}", searchTerm);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobPage = jobRepository.searchJobs(searchTerm, pageable);
        
        List<JobResponse> content = jobPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<>(
            content,
            jobPage.getNumber(),
            jobPage.getSize(),
            jobPage.getTotalElements()
        );
    }

    public PageResponse<JobResponse> filterJobs(String title, String location, List<String> skills, 
                                               Integer experienceRequired, int page, int size) {
        logger.debug("Filtering jobs - title: {}, location: {}, skills: {}, experience: {}", 
                    title, location, skills, experienceRequired);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobPage = jobRepository.findJobsByFilters(title, location, skills, 
                                                             experienceRequired, pageable);
        
        List<JobResponse> content = jobPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<>(
            content,
            jobPage.getNumber(),
            jobPage.getSize(),
            jobPage.getTotalElements()
        );
    }

    public PageResponse<JobResponse> getJobsByRecruiter(String recruiterId, int page, int size) {
        logger.debug("Fetching jobs by recruiter: {}", recruiterId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Job> jobPage = jobRepository.findByPostedBy_IdAndIsActiveTrue(recruiterId, pageable);
        
        List<JobResponse> content = jobPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<>(
            content,
            jobPage.getNumber(),
            jobPage.getSize(),
            jobPage.getTotalElements()
        );
    }

    @Transactional
    public JobResponse updateJob(String id, JobCreateRequest request, String userId) {
        logger.info("Updating job: {} by user: {}", id, userId);
        
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        
        User user = userService.getUserEntity(userId);
        
        // Verify user is the owner or admin
        if (!job.getPostedBy().getId().equals(userId) && user.getRole() != com.jobportal.model.enums.Role.ADMIN) {
            throw new UnauthorizedException("You don't have permission to update this job");
        }
        
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSkills(request.getSkills());
        job.setExperienceRequired(request.getExperienceRequired());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setEmploymentType(request.getEmploymentType());
        job.setUpdatedAt(java.time.LocalDateTime.now());
        
        job = jobRepository.save(job);
        logger.info("Job updated successfully: {}", id);
        
        return mapToResponse(job);
    }

    @Transactional
    public void deleteJob(String id, String userId) {
        logger.info("Deleting job: {} by user: {}", id, userId);
        
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        
        User user = userService.getUserEntity(userId);
        
        // Verify user is the owner or admin
        if (!job.getPostedBy().getId().equals(userId) && user.getRole() != com.jobportal.model.enums.Role.ADMIN) {
            throw new UnauthorizedException("You don't have permission to delete this job");
        }
        
        // Soft delete
        job.setIsActive(false);
        job.setUpdatedAt(java.time.LocalDateTime.now());
        jobRepository.save(job);
        logger.info("Job deleted successfully: {}", id);
    }

    public Job getJobEntity(String id) {
        return jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    private JobResponse mapToResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setLocation(job.getLocation());
        response.setSkills(job.getSkills());
        response.setExperienceRequired(job.getExperienceRequired());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setEmploymentType(job.getEmploymentType());
        response.setPostedBy(job.getPostedBy().getId());
        response.setPostedByName(job.getPostedBy().getFirstName() + " " + job.getPostedBy().getLastName());
        response.setIsActive(job.getIsActive());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }
}

