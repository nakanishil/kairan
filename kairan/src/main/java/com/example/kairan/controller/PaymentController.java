package com.example.kairan.controller;

import java.time.Year;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.kairan.entity.District;
import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.User;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.MembershipFeeService;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

	@Value("${stripe.api.key}")
	private String stripeApiKey;
	
	private final MembershipFeeService membershipFeeService;

	@PostMapping("/create-checkout-session")
	public String createCheckoutSession(
			@AuthenticationPrincipal UserDetailsImpl userDetails) throws Exception {
		Stripe.apiKey = stripeApiKey;
		
		User loginUser = userDetails.getUser();
		District loginUserDistrict = loginUser.getDistrict();
		// 年会費設定を年と町内から取得
		
		MembershipFee membershipFee = membershipFeeService.sameDistrictIdAndYear(
				loginUserDistrict.getId(), Year.now().getValue())
					.orElseThrow(() -> new IllegalArgumentException("年会費設定が存在しません"));
		
		System.out.println("💡 年会費金額 = " + membershipFee.getAmount());
		System.out.println("💡 longValue = " + membershipFee.getAmount().longValue());

		SessionCreateParams params = 
				SessionCreateParams.builder()
				.setMode(SessionCreateParams.Mode.PAYMENT) // 支払いモード
				.setSuccessUrl("http://localhost:8080/payment/success?session_id={CHECKOUT_SESSION_ID}") //成功後に遷移するURL
				.setCancelUrl("http://localhost:8080/payment/cancel")	// キャンセル時
				.addLineItem(
						SessionCreateParams.LineItem.builder()
						.setQuantity(1L)
						.setPriceData(
								SessionCreateParams.LineItem.PriceData.builder()
								.setCurrency("jpy")
								.setUnitAmount(membershipFee.getAmount().longValue()) // 5000円
								.setProductData(
										SessionCreateParams.LineItem.PriceData.ProductData.builder()
										.setName("町内会費")
										.build()
										)
								.build()
								)
						.build()
						)
				.build();
		
		Session session = Session.create(params);

		return "redirect:" + session.getUrl();
	}
}
