package com.example.kairan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payment")
public class PaymentCancelController {

    @GetMapping("/cancel")
    public String paymentCancelPage() {
        return "payment/payment-cancel"; 
    }
}
