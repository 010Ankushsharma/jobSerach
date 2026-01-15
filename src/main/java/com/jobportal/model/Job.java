package com.jobportal.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Job Document Model
 * 
 * Design Decisions:
 * - Text indexes on title, description, location, skills for full-text search
 * - Reference to User (recruiter) for relationship tracking
 * - Embedded salary range for better query performance
 * - Index on isActive for filtering active jobs
 * - Index on createdAt for sorting
 */
@Document(collection = "jobs")
@org.springframework.data.mongodb.core.index.CompoundIndex(
    def = "{'isActive': 1, 'createdAt': -1}"
)
public class Job {
    
    @Id
    private String id;
    
    @TextIndexed
    @Field("title")
    private String title;
    
    @TextIndexed
    @Field("description")
    private String description;
    
    @TextIndexed
    @Field("location")
    private String location;
    
    @TextIndexed
    @Field("skills")
    private List<String> skills;
    
    @Field("experienceRequired")
    private Integer experienceRequired; // in years
    
    @Field("salaryMin")
    private BigDecimal salaryMin;
    
    @Field("salaryMax")
    private BigDecimal salaryMax;
    
    @Field("employmentType")
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, REMOTE
    
    @DBRef
    @Indexed
    @Field("postedBy")
    private User postedBy; // Recruiter reference
    
    @Field("isActive")
    @Indexed
    private Boolean isActive;
    
    @Field("createdAt")
    @Indexed
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;

    // Constructors
    public Job() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public Integer getExperienceRequired() {
        return experienceRequired;
    }

    public void setExperienceRequired(Integer experienceRequired) {
        this.experienceRequired = experienceRequired;
    }

    public BigDecimal getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(BigDecimal salaryMin) {
        this.salaryMin = salaryMin;
    }

    public BigDecimal getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(BigDecimal salaryMax) {
        this.salaryMax = salaryMax;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public User getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(User postedBy) {
        this.postedBy = postedBy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

