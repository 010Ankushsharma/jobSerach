package com.jobportal.dto.request;

import com.jobportal.model.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class ApplicationStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private ApplicationStatus status;
    
    private String notes;

    // Constructors
    public ApplicationStatusUpdateRequest() {
    }

    // Getters and Setters
    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

