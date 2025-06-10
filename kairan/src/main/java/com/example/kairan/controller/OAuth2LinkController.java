package com.example.kairan.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuth2LinkController {

	@GetMapping("/link/google")
	public String linkGoogleAccount(HttpSession session) {
		// セッションに連携目的であることを保存
		session.setAttribute("oauth_link_mode", true);
		
		// Google認証へリダイレクト(Spring SecurityのAuth2ルート)
		return "redirect:/oauth2/authorization/google";
	}
}
