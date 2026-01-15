package com.jobportal.repository;

import com.jobportal.model.Application;
import com.jobportal.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {
    
    Optional<Application> findByCandidate_IdAndJob_Id(String candidateId, String jobId);
    
    Page<Application> findByCandidate_Id(String candidateId, Pageable pageable);
    
    Page<Application> findByJob_Id(String jobId, Pageable pageable);
    
    Page<Application> findByJob_IdAndStatus(String jobId, ApplicationStatus status, Pageable pageable);
    
    boolean existsByCandidate_IdAndJob_Id(String candidateId, String jobId);
    
    long countByJob_Id(String jobId);
    
    long countByJob_IdAndStatus(String jobId, ApplicationStatus status);
}

