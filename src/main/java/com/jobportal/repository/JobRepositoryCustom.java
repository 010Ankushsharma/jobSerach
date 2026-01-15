package com.jobportal.repository;

import com.jobportal.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobRepositoryCustom {
    Page<Job> findJobsByFilters(String title, String location, List<String> skills, 
                               Integer experienceRequired, Pageable pageable);
}

