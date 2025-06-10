package com.example.kairan.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.kairan.security.UserDetailsImpl;

@ControllerAdvice
public class GlobalModelAttributeAdvice {
	
	@ModelAttribute("loginUserName")
	public String addLoginUsername(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		if (userDetails != null) {
			return userDetails.getUser().getName();
		}
		return "";		
	}
	
	@ModelAttribute("loginUserRole")
	public String addLoginUserRole(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		if (userDetails != null) {
			return userDetails.getUser().getRole().getName().replace("ROLE_", "");
		}
		return "";
	}
}
