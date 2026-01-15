package com.jobportal.repository;

import com.jobportal.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JobRepositoryImpl implements JobRepositoryCustom {
    
    private final MongoTemplate mongoTemplate;

    public JobRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Job> findJobsByFilters(String title, String location, List<String> skills, 
                                       Integer experienceRequired, Pageable pageable) {
        Query query = new Query();
        
        // Always filter active jobs
        query.addCriteria(Criteria.where("isActive").is(true));
        
        // Add filters if provided
        if (title != null && !title.trim().isEmpty()) {
            query.addCriteria(Criteria.where("title").regex(title, "i"));
        }
        
        if (location != null && !location.trim().isEmpty()) {
            query.addCriteria(Criteria.where("location").regex(location, "i"));
        }
        
        if (skills != null && !skills.isEmpty()) {
            query.addCriteria(Criteria.where("skills").in(skills));
        }
        
        if (experienceRequired != null) {
            query.addCriteria(Criteria.where("experienceRequired").lte(experienceRequired));
        }
        
        // Apply pagination and sorting
        query.with(pageable);
        
        // Execute query
        List<Job> jobs = mongoTemplate.find(query, Job.class);
        long total = mongoTemplate.count(query, Job.class);
        
        return new PageImpl<>(jobs, pageable, total);
    }
}

