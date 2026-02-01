package com.example.chatbotmc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "approved_by_admin")
    private boolean approvedByAdmin = false;

    @Column(name = "approval_token")
    private String approvalToken;

    @Column(name = "approval_requested_at")
    private LocalDateTime approvalRequestedAt;
    
    @Column(name = "approval_token_expires_at")
    private LocalDateTime approvalTokenExpiresAt;

    @Column(name = "query_count")
    private int queryCount = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
