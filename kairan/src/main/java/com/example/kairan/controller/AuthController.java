package com.example.kairan.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.kairan.entity.EmailVerificationToken;
import com.example.kairan.entity.User;
import com.example.kairan.repository.EmailVerificationTokenRepository;
import com.example.kairan.service.UserService;

@Controller
public class AuthController {
    private final UserService userService;
    private final EmailVerificationTokenRepository tokenRepository;

    public AuthController(UserService userService, EmailVerificationTokenRepository tokenRepository) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
    }

    @GetMapping("/confirm")
    public String confirmUser(@RequestParam String token, Model model) {
        Optional<EmailVerificationToken> verificationToken = tokenRepository.findByToken(token);

        if (verificationToken.isEmpty()) {
            model.addAttribute("errorMessage", "無効な認証リンクです。");
            return "auth/confirm-failed";
        }

        EmailVerificationToken tokenEntity = verificationToken.get();

        // トークンの有効期限チェック
        if (tokenEntity.isExpired()) {
            model.addAttribute("errorMessage", "この認証リンクは有効期限が切れています。再度、依頼してください。");
            return "auth/confirm-failed";
        }

        // ユーザーのアカウントを有効化
        User user = tokenEntity.getUser();
        user.setEnabled(true);
        userService.saveUser(user);

        // 認証トークンを削除
        tokenRepository.delete(tokenEntity);

        model.addAttribute("successMessage", "アカウントが有効化されました。ログインしてください。");
        return "auth/confirm-success"; // ✅ スペル修正
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/logout-success")
    public String logoutPage() {
        return "redirect:/login?logout";
    }
}
