package com.example.kairan.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.kairan.service.PasswordResetService;

import lombok.Data;

@Controller
@RequestMapping("/reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    // 再発行申請画面
    @GetMapping("/request")
    public String showRequestForm(Model model) {
        model.addAttribute("emailForm", new EmailForm());
        return "reset/request-form"; // ビュー名（後で作成する）
    }

    // 再発行
    @PostMapping("/request")
    public String requestReset(@Valid @ModelAttribute("emailForm") EmailForm form,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            return "reset/request-form";
        }

        try {
            passwordResetService.createPasswordResetToken(form.getEmail());
            model.addAttribute("message", "パスワードリセット用のメールを送信しました！");
            return "reset/request-success"; // 確認ページ（あとで作る）
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "notfound", "登録されているメールアドレスが見つかりません");
            return "reset/request-form";
        }
    }

    
    @GetMapping("/form")
    public String showTemporaryCredentials(
    		@RequestParam("token") String token,
    		Model model
    		)
    {
    	try {
	    	List<String> temporaryCredentials = passwordResetService.generateTemporaryCredentials(token);
	    	
	    	String temporaryUserId = temporaryCredentials.get(0);
	    	String temporaryPassword = temporaryCredentials.get(1);
	    	
	        model.addAttribute("temporaryUserId", temporaryUserId);
	        model.addAttribute("temporaryPassword", temporaryPassword);
	        model.addAttribute("message", "仮IDと仮パスワードを発行しました。 ログイン後、直ちにマイページからIDとパスワードの変更をしてください");
	        return "reset/temporary-credentials";
    	} catch(Exception e) {
    		model.addAttribute("errorMessage", e.getMessage());
    		return "reset/error";
    	}
    }

    // フォームクラスたち
    @Data
    public static class EmailForm {
        @NotBlank
        @Email
        private String email;
    }

}
