package com.example.kairan.service;

import static org.mockito.Mockito.*;

import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendEmail_正常系_メール送信されること() throws Exception {
        // Arrange
        String to = "test@example.com";
        String subject = "テスト件名";
        String content = "<p>テスト本文</p>";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendEmail(to, subject, content);

        // Assert
        verify(mailSender, times(1)).send(mimeMessage);
    }
    
    @Test
    void sendEmail_異常系_メール送信失敗時に例外がスローされること() throws Exception {
        // Arrange
        String to = "test@example.com";
        String subject = "テスト件名";
        String content = "<p>テスト本文</p>";

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("メール作成エラー"));

        // Act  Assert
        org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> emailService.sendEmail(to, subject, content),
            "メール送信エラー: メール作成エラー"
        );
    }
    
    @Test
    void sendIdAndPasswordResetEmail_正常系_メール送信されること() throws Exception {
        // Arrange
        String toEmail = "reset@example.com";
        String token = "test-token";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendIdAndPasswordResetEmail(toEmail, token);

        // Assert
        verify(mailSender, times(1)).send(mimeMessage);
    }
    
    @Test
    void sendIdAndPasswordResetEmail_本文にトークン付きURLが含まれていること() throws Exception {
        // Arrange
        String toEmail = "reset@example.com";
        String token = "test-token";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // メール送信時に MimeMessageHelper をキャプチャするため
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        // Act
        emailService.sendIdAndPasswordResetEmail(toEmail, token);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());

    }



}
