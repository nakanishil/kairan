package com.example.kairan.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kairan.entity.PasswordResetToken;
import com.example.kairan.entity.User;
import com.example.kairan.repository.PasswordResetTokenRepository;
import com.example.kairan.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    // メールアドレスからトークンを発行して保存
    @Transactional
    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("該当するユーザーが見つかりません"));

        // トークン作成
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24時間有効
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        emailService.sendIdAndPasswordResetEmail(user.getEmail(), token);
    }

    // トークンからパスワードリセット情報を取得
    public Optional<PasswordResetToken> getByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    
     // パスワードをリセットする
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無効なトークンです"));

        if (resetToken.getUsed()) {
            throw new IllegalStateException("このトークンはすでに使用済みです");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("このトークンは期限切れです");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // トークンを使用済みに
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
    
    // トークンを使って仮IDと仮パスワードを発行
    @Transactional
    public List<String> generateTemporaryCredentials(String token) {
    	PasswordResetToken resetToken = tokenRepository.findByToken(token)
    			.orElseThrow(() -> new IllegalArgumentException("無効なトークンです"));
    	
    	if (resetToken.getUsed()) {
    		throw new IllegalStateException("このトークンは既に使用済みです");
    	}
    	
    	User user = resetToken.getUser();
    	
    	// 仮IDと仮パスワードを生成
    	String temporaryUserId = "temp" + UUID.randomUUID().toString().substring(0, 8);
    	String temporaryPassword = UUID.randomUUID().toString().substring(0, 10);
    	
    	user.setUserId(temporaryUserId);
    	user.setPassword(passwordEncoder.encode(temporaryPassword));
    	
    	userRepository.save(user);
    	
    	resetToken.setUsed(true);
    	tokenRepository.save(resetToken);
    	
    	return List.of(temporaryUserId,temporaryPassword);
    	
    }
    
    
}
