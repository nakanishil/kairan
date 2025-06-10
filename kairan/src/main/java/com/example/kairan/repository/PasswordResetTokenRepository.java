package com.example.kairan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kairan.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

	// トークン文字列から検索（トークン認証用）
    Optional<PasswordResetToken> findByToken(String token);
}
