package com.example.chatbotmc.service;

import com.example.chatbotmc.entity.User;
import com.example.chatbotmc.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    private static final int MAX_QUERY_LIMIT = 5;
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Check if user has reached their query limit
     * @param userId the user ID
     * @return true if user has reached limit, false otherwise
     */
    public boolean hasReachedQueryLimit(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getQueryCount() >= MAX_QUERY_LIMIT;
    }
    
    /**
     * Increment user's query count
     * @param userId the user ID
     * @throws RuntimeException if user has reached query limit
     */
    @Transactional
    public void incrementQueryCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getQueryCount() >= MAX_QUERY_LIMIT) {
            throw new RuntimeException("You have reached your query limit of " + MAX_QUERY_LIMIT + " questions");
        }
        
        user.setQueryCount(user.getQueryCount() + 1);
        userRepository.save(user);
    }
    
    /**
     * Get remaining queries for a user
     * @param userId the user ID
     * @return number of queries remaining
     */
    public int getRemainingQueries(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return Math.max(0, MAX_QUERY_LIMIT - user.getQueryCount());
    }
}
