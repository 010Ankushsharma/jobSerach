package com.jobportal.repository;

import com.jobportal.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<Job, String>, JobRepositoryCustom {
    
    Page<Job> findByIsActiveTrue(Pageable pageable);
    
    Page<Job> findByIsActiveTrueAndTitleContainingIgnoreCase(String title, Pageable pageable);
    
    Page<Job> findByIsActiveTrueAndLocationContainingIgnoreCase(String location, Pageable pageable);
    
    Page<Job> findByIsActiveTrueAndSkillsIn(List<String> skills, Pageable pageable);
    
    Page<Job> findByIsActiveTrueAndExperienceRequiredLessThanEqual(Integer experienceRequired, Pageable pageable);
    
    @Query("{'isActive': true, $or: [{'title': {$regex: ?0, $options: 'i'}}, " +
           "{'description': {$regex: ?0, $options: 'i'}}, " +
           "{'location': {$regex: ?0, $options: 'i'}}, " +
           "{'skills': {$in: [?0]}}]}")
    Page<Job> searchJobs(String searchTerm, Pageable pageable);
    
    // Note: Complex filtering is handled in service layer for better flexibility
    // This method uses a simpler query approach
    @Query("{'isActive': true}")
    Page<Job> findAllActiveJobs(Pageable pageable);
    
    Page<Job> findByPostedBy_IdAndIsActiveTrue(String postedById, Pageable pageable);
}

