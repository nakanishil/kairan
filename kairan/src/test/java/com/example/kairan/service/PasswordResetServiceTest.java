package com.example.kairan.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.kairan.entity.PasswordResetToken;
import com.example.kairan.entity.User;
import com.example.kairan.repository.PasswordResetTokenRepository;
import com.example.kairan.repository.UserRepository;

class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPasswordResetToken_正常系_トークン作成と保存とメール送信が行われること() {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        passwordResetService.createPasswordResetToken(email);

        // Assert
        verify(userRepository, times(1)).findByEmail(email);
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendIdAndPasswordResetEmail(eq(email), anyString());
    }
    
    @Test
    void createPasswordResetToken_異常系_ユーザーが見つからない場合は例外をスローすること() {
        // Arrange
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act  Assert
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> passwordResetService.createPasswordResetToken(email),
            "該当するユーザーが見つかりません"
        );

        verify(userRepository, times(1)).findByEmail(email);
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendIdAndPasswordResetEmail(anyString(), anyString());
    }
    
    @Test
    void getByToken_正常系_トークンが取得できること() {
        // Arrange
        String token = "test-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // Act
        Optional<PasswordResetToken> result = passwordResetService.getByToken(token);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isPresent());
        org.junit.jupiter.api.Assertions.assertEquals(token, result.get().getToken());
    }
    
    @Test
    void resetPassword_正常系_パスワードがリセットされること() {
        // Arrange
        String token = "test-token";
        String newPassword = "newPassword123";

        User user = new User();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setUsed(false);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1)); // 有効期限内

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        // Act
        passwordResetService.resetPassword(token, newPassword);

        // Assert
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(user);
        verify(tokenRepository, times(1)).save(resetToken);

        // パスワードとトークンの使用済みフラグが更新されていることを確認
        org.junit.jupiter.api.Assertions.assertEquals("encodedPassword", user.getPassword());
        org.junit.jupiter.api.Assertions.assertTrue(resetToken.getUsed());
    }
    
    @Test
    void resetPassword_異常系_トークンが既に使用済みの場合は例外をスローすること() {
        // Arrange
        String token = "test-token";
        String newPassword = "newPassword123";

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsed(true); // すでに使用済み
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1)); // 有効期限内

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // Act  Assert
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalStateException.class,
            () -> passwordResetService.resetPassword(token, newPassword),
            "このトークンはすでに使用済みです"
        );

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }
    
    @Test
    void resetPassword_異常系_トークンが期限切れの場合は例外をスローすること() {
        // Arrange
        String token = "test-token";
        String newPassword = "newPassword123";

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsed(false);
        resetToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // すでに期限切れ

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // Act  Assert
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalStateException.class,
            () -> passwordResetService.resetPassword(token, newPassword),
            "このトークンは期限切れです"
        );

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }
    
    @Test
    void generateTemporaryCredentials_正常系_仮IDと仮パスワードが生成され保存されること() {
        // Arrange
        String token = "test-token";

        User user = new User();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setUsed(false);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTemporaryPassword");

        // Act
        List<String> credentials = passwordResetService.generateTemporaryCredentials(token);

        // Assert
        verify(userRepository, times(1)).save(user);
        verify(tokenRepository, times(1)).save(resetToken);

        // 仮IDと仮パスワードが生成されているかを簡易チェック
        org.junit.jupiter.api.Assertions.assertEquals(2, credentials.size());
        org.junit.jupiter.api.Assertions.assertTrue(credentials.get(0).startsWith("temp")); // 仮IDはtempで始まる
        org.junit.jupiter.api.Assertions.assertEquals("encodedTemporaryPassword", user.getPassword());
        org.junit.jupiter.api.Assertions.assertTrue(resetToken.getUsed());
    }







}
