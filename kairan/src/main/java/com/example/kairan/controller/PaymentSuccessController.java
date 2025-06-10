package com.example.kairan.controller;

import java.time.Year;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.User;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.MembershipFeeService;
import com.example.kairan.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PaymentSuccessController {
	
	private final PaymentService paymentService;
	private final MembershipFeeService membershipFeeService;
	
	@GetMapping("/payment/success")
	public String paymentSuccess(
			@RequestParam("session_id") String sessionId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Model model
			)
	{
		
		// nullチェック（テスト用）
	    if (sessionId != null) {
	        model.addAttribute("sessionId", sessionId);
	        // Stripeから決済情報取得する処理など
	    }
	    
		User loginUser = userDetails.getUser();
		MembershipFee membershipFee = membershipFeeService
				.sameDistrictIdAndYear(loginUser.getDistrict().getId(), Year.now().getValue())
					.orElseThrow(() -> new IllegalArgumentException("金額設定が存在しません"));
		paymentService.regiPaymentAndAccounting(loginUser, membershipFee, sessionId);
		System.out.println("支払い成功：ユーザーID = " + loginUser.getId() + ", セッションID = " + sessionId);

		return "payment/payment-success";
	}
}
