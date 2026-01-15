package com.jobportal.service;

import com.jobportal.dto.response.PageResponse;
import com.jobportal.dto.response.UserResponse;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.model.User;
import com.jobportal.model.enums.Role;
import com.jobportal.repository.UserRepository;
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
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserById(String id) {
        logger.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToResponse(user);
    }

    public PageResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching all users - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);
        
        List<UserResponse> content = userPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<>(
            content,
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements()
        );
    }

    public PageResponse<UserResponse> getUsersByRole(Role role, int page, int size) {
        logger.debug("Fetching users by role: {}", role);
        
        Pageable pageable = PageRequest.of(page, size);
        // Note: This would require a custom query method in repository
        // For now, filtering in memory (not ideal for large datasets)
        Page<User> allUsers = userRepository.findAll(pageable);
        List<UserResponse> filtered = allUsers.getContent().stream()
            .filter(user -> user.getRole() == role)
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return new PageResponse<>(filtered, page, size, filtered.size());
    }

    @Transactional
    public void deactivateUser(String id) {
        logger.info("Deactivating user: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setIsActive(false);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
        logger.info("User deactivated successfully: {}", id);
    }

    @Transactional
    public void activateUser(String id) {
        logger.info("Activating user: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setIsActive(true);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
        logger.info("User activated successfully: {}", id);
    }

    public User getUserEntity(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}

