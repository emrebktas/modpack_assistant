package com.example.chatbotmc.service;

import com.example.chatbotmc.dto.AuthResponse;
import com.example.chatbotmc.dto.LoginRequest;
import com.example.chatbotmc.dto.RegisterRequest;
import com.example.chatbotmc.entity.User;
import com.example.chatbotmc.entity.Role;
import com.example.chatbotmc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    
    @Value("${admin.approval-token-expiration-hours:48}")
    private int tokenExpirationHours;
    
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username is already taken");
        }
        
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email is already registered");
        }
        
        // Generate approval token for admin with expiration
        String approvalToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(tokenExpirationHours);
        
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setApprovedByAdmin(false);
        user.setApprovalToken(approvalToken);
        user.setApprovalRequestedAt(now);
        user.setApprovalTokenExpiresAt(expiresAt);
        
        userRepository.save(user);
        
        // Send approval email to admin
        emailService.sendAdminApprovalEmail(user.getUsername(), user.getEmail(), approvalToken);
        
        // Return response without token (user needs admin approval first)
        return new AuthResponse(
            null, 
            user.getUsername(), 
            user.getEmail(),
            user.getRole().name()
        );
    }
    
    public AuthResponse login(LoginRequest request) {
        // Use generic error message to prevent username enumeration
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        // Check if user is approved by admin
        if (!user.isApprovedByAdmin()) {
            throw new RuntimeException("Your account is pending administrator approval. An email has been sent to the administrator for review. You will receive an email notification once your account is approved. Please check your email inbox for confirmation.");
        }
        
        String token = jwtService.generateToken(user);
        
        return new AuthResponse(
            token,
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()
        );
    }
    
    public String approveUser(String token, String action) {
        User user = userRepository.findByApprovalToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid approval token"));
        
        // Check if token has expired
        if (user.getApprovalTokenExpiresAt() != null && 
            LocalDateTime.now().isAfter(user.getApprovalTokenExpiresAt())) {
            throw new RuntimeException("Approval token has expired. Please request a new registration.");
        }
        
        if ("approve".equals(action)) {
            user.setApprovedByAdmin(true);
            user.setApprovalToken(null);
            userRepository.save(user);
            
            // Send notification to user
            emailService.sendUserApprovalNotification(user.getEmail(), user.getUsername(), true);
            
            return "User " + user.getUsername() + " has been successfully approved!";
        } else if ("reject".equals(action)) {
            // Optionally delete the user or mark as rejected
            user.setApprovalToken(null);
            userRepository.save(user);
            
            // Send notification to user
            emailService.sendUserApprovalNotification(user.getEmail(), user.getUsername(), false);
            
            return "User " + user.getUsername() + " has been rejected.";
        } else {
            throw new RuntimeException("Invalid action");
        }
    }
}