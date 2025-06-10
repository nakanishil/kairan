package com.example.kairan.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.kairan.entity.EmailVerificationToken;
import com.example.kairan.entity.User;
import com.example.kairan.repository.EmailVerificationTokenRepository;
import com.example.kairan.service.EmailService;

public class UserRegistrationListenerTest {
	
	private EmailService emailService;
	private EmailVerificationTokenRepository tokenRepository;
	private UserRegistrationListener listener;
	
	@BeforeEach
	void setUp() {
		emailService = mock(EmailService.class);
		tokenRepository = mock(EmailVerificationTokenRepository.class);
		listener = new UserRegistrationListener(emailService, tokenRepository);
	}
	
	
	@Test
	void handleUserRegistration_正常にトークン保存とメール送信が行われる(){
		
		// arrange
		User user = new User();
		user.setEmail("test@example.com");
		
		String appUrl = "http://localhost:8080";
		
		UserRegistrationEvent event = new UserRegistrationEvent(this, user, appUrl);
		
		// act
		listener.handleUserRegistration(event);
		
		// Assert
		ArgumentCaptor<EmailVerificationToken> tokenCaptor 
			= ArgumentCaptor.forClass(EmailVerificationToken.class);
		verify(tokenRepository, times(1)).save(tokenCaptor.capture());
		
		EmailVerificationToken savedToken = tokenCaptor.getValue();
		assertNotNull(savedToken.getToken());
		assertEquals(user, savedToken.getUser());
		assertTrue(savedToken.getExpiryDate().isAfter(LocalDateTime.now()));

		
		verify(emailService, times(1)).sendEmail(eq(user.getEmail()), anyString(), contains("/confirm?token="));
		
		
	}
	
	
	
}
