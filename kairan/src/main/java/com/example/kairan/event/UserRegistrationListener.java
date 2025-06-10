package com.example.kairan.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.kairan.entity.EmailVerificationToken;
import com.example.kairan.entity.User;
import com.example.kairan.repository.EmailVerificationTokenRepository;
import com.example.kairan.service.EmailService;

@Component
public class UserRegistrationListener {
	private final EmailService emailService;
	private final EmailVerificationTokenRepository tokenRepository;
	
	public UserRegistrationListener(EmailService emailService,
			EmailVerificationTokenRepository tokenRepository) {
		this.emailService = emailService;
		this.tokenRepository = tokenRepository;
	}
	
	@EventListener
	public void handleUserRegistration(UserRegistrationEvent event) {
		User user = event.getUser();
		String token = UUID.randomUUID().toString();
		
		// トークンの有効期限を24時間後に設定
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);
		
        EmailVerificationToken verificationToken = new EmailVerificationToken(token, user, expiryDate);
        tokenRepository.save(verificationToken);
        
        
		// メール本文作成
		String confirmUrl = event.getAppUrl() + "/confirm?token=" + token;
		String message = "アカウントを有効化するには、以下のリンクをクリックしてください: "+ confirmUrl;
		
		// メール送信
		emailService.sendEmail(user.getEmail(), "【町内会管理アプリ：kairan】 メール認証", message);
	}
}
