package com.jobportal.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    
    public String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

