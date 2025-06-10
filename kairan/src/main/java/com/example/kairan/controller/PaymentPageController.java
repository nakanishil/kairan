package com.example.kairan.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.kairan.entity.User;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.MembershipFeeService;
import com.example.kairan.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PaymentPageController {
	
	private final MembershipFeeService membershipFeeService;
	private final PaymentService  paymentService;

    @GetMapping("/payment/start")
    public String paymentStartPage(
    		@AuthenticationPrincipal UserDetailsImpl userDetails,
    		Model model) 
    {
        User loginUser = userDetails.getUser();
        DecimalFormat formatter = new DecimalFormat("#,###");

        try {
            Optional<BigDecimal> fee = paymentService.paymentCheck(loginUser);

            // Optionalの中身があれば表示用金額と元のfeeをモデルに格納
            fee.ifPresent(value -> {
                String formattedAmount = formatter.format(value);
                model.addAttribute("feeDisplay", formattedAmount);
                model.addAttribute("fee", value);
            });

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "payment/payment-start";
    }
}
