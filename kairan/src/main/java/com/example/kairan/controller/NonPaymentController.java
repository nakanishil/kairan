package com.example.kairan.controller;

import java.time.Year;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kairan.entity.PaymentMethod;
import com.example.kairan.entity.User;
import com.example.kairan.form.DepositProcessingForm;
import com.example.kairan.repository.PaymentMethodRepository;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.NonPaymentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NonPaymentController {
	
	private final NonPaymentService nonPaymentService;
	private final PaymentMethodRepository paymentMethodRepository;
	
	
	@GetMapping({"/nonpayment/nonpayment-list", "/nonpayment/nonpayment-list/{year}"})
	public String nonPaymentList(
			@PageableDefault(size = 10) Pageable pageable,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@RequestParam(name = "year", required = false) Integer inputYear,
			Model model) 
	{
		User loginUser = userDetails.getUser();
		int nowYear = Year.now().getValue();
		int year = (inputYear != null) ? inputYear : Year.now().getValue();
		
		model.addAttribute("page", Page.empty());
	    model.addAttribute("yearList", List.of());
	    model.addAttribute("year", year);
		
		try {
			Page<User> page = nonPaymentService
					.nonPaymentPage(loginUser, year, pageable);
			
			List<Integer> yearList = nonPaymentService
					.findMembershipFeeYear(loginUser.getDistrict().getAssociation());
			
			model.addAttribute("page", page);
			model.addAttribute("year", year);
			model.addAttribute("yearList", yearList);
		} catch(IllegalArgumentException e) {
			model.addAttribute("errorMessage", e.getMessage());
		}
		
		return "nonpayment/nonpayment-list";
	}
	
	// 支払いページ 
	@GetMapping("/nonpayment/depositprocessing-page/{id}")
	public String depositprocessingPage(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable(name = "id") Integer userId,
			DepositProcessingForm form,
			Model model
			) 
	{
		User loginUser = userDetails.getUser();
		List<PaymentMethod> methods = paymentMethodRepository.findAll();
		
		List<Integer> yearList = nonPaymentService
				.findMembershipFeeYear(loginUser.getDistrict().getAssociation());
		form.setPayerId(userId);

		model.addAttribute("yearList", yearList);
		model.addAttribute("methods", methods);
		model.addAttribute("form", form);
		return "nonpayment/depositprocessing-page";
	}
	
	// 支払処理
	@PostMapping("/nonpayment/depositprocessing/{id}")
	public String depositprocessing(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable(name = "id") Integer payerId,
			@Valid @ModelAttribute("form") DepositProcessingForm form, BindingResult result,
			RedirectAttributes redirectAttributes,
			Model model)
	{
		User loginUser = userDetails.getUser();
		
		if (result.hasErrors()) {
			List<PaymentMethod> methods = paymentMethodRepository.findAll();
		    model.addAttribute("methods", methods);
			System.out.println("バリデーションエラー" + result.getAllErrors());
		    model.addAttribute("form", form);
		    List<Integer> yearList = nonPaymentService
					.findMembershipFeeYear(loginUser.getDistrict().getAssociation());

			model.addAttribute("yearList", yearList);

		    return "nonpayment/depositprocessing-page";
		}
		
		try {
			User payer = nonPaymentService.findUserId(payerId);
			nonPaymentService.nonPaymentRegiCheck(loginUser, payer);
			nonPaymentService.depositProcessing(payer, form);
		} catch(IllegalArgumentException e) {
			model.addAttribute("errorMessage", e.getMessage());
			
			List<PaymentMethod> methods = paymentMethodRepository.findAll();
		    List<Integer> yearList = nonPaymentService.findMembershipFeeYear(loginUser.getDistrict().getAssociation());
		    model.addAttribute("methods", methods);
		    model.addAttribute("yearList", yearList);
		    model.addAttribute("form", form);
		    
		    return "nonpayment/depositprocessing-page";
		}
		System.out.println("処理完了");
		return "redirect:/nonpayment/nonpayment-list";

	}
	
}
