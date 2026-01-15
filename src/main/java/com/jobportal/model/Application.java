package com.jobportal.model;

import com.jobportal.model.enums.ApplicationStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

/**
 * Application Document Model
 * 
 * Design Decisions:
 * - Compound unique index on (candidate, job) to prevent duplicate applications
 * - DBRef to User and Job for referential integrity
 * - Index on status for filtering applications by status
 * - Index on appliedAt for sorting
 * - Embedded resume data (could be moved to separate collection if very large)
 */
@Document(collection = "applications")
@CompoundIndex(def = "{'candidate': 1, 'job': 1}", unique = true, name = "unique_application")
public class Application {
    
    @Id
    private String id;
    
    @DBRef
    @Field("candidate")
    private User candidate;
    
    @DBRef
    @Field("job")
    private Job job;
    
    @Field("status")
    @org.springframework.data.mongodb.core.index.Indexed
    private ApplicationStatus status;
    
    @Field("resume")
    private String resume; // Resume text or file path
    
    @Field("coverLetter")
    private String coverLetter;
    
    @Field("appliedAt")
    @org.springframework.data.mongodb.core.index.Indexed
    private LocalDateTime appliedAt;
    
    @Field("reviewedAt")
    private LocalDateTime reviewedAt;
    
    @Field("notes")
    private String notes; // Recruiter notes

    // Constructors
    public Application() {
        this.status = ApplicationStatus.APPLIED;
        this.appliedAt = LocalDateTime.now();
    }

    public Application(User candidate, Job job, String resume, String coverLetter) {
        this();
        this.candidate = candidate;
        this.job = job;
        this.resume = resume;
        this.coverLetter = coverLetter;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

