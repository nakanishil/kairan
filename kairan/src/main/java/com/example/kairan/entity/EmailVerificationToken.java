package com.example.kairan.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

// 認証トークンを管理するverificationTOkenテーブルを作成
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, unique = true)
    private String token;
	
	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	
	
	@Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;  // expiry_date → expiresAt に修正
	
	public EmailVerificationToken() {}
	
	public EmailVerificationToken(String token, User user, LocalDateTime expiryDate) {
		this.token = token;
		this.user = user;
		this.expiresAt = expiryDate;
	}
    public String getToken() {
        return token;
    }

    public User getUser() { 
        return user;
    }

    public LocalDateTime getExpiryDate() {
        return expiresAt;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
