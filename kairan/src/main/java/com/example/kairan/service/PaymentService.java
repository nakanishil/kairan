package com.example.kairan.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kairan.entity.Accounting;
import com.example.kairan.entity.AccountingCategory;
import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.Payment;
import com.example.kairan.entity.PaymentMethod;
import com.example.kairan.entity.User;
import com.example.kairan.repository.AccountingCategoryRepository;
import com.example.kairan.repository.AccountingRepository;
import com.example.kairan.repository.PaymentMethodRepository;
import com.example.kairan.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
	
	private final PaymentRepository paymentRepository;
	private final AccountingRepository accountingRepository;
	private final AccountingCategoryRepository accountingCategoryRepository;
	private final MembershipFeeService membershipFeeService;
	private final PaymentMethodRepository paymentMethodRepository;
	
	@Transactional
	public void regiPaymentAndAccounting(User loginUser, MembershipFee membershipFee, String sessionId) {
		
		PaymentMethod method = paymentMethodRepository.findById(1)
				.orElseThrow(() -> new IllegalArgumentException("支払方法が存在しません"));
		// paymentに支払い履歴を登録
		Payment payment = new Payment();
		payment.setUser(loginUser);
		payment.setAmount(membershipFee.getAmount()); // 年会費から取得
		payment.setMembershipFee(membershipFee); //membershipFeeとの関連付け
		payment.setPaymentMethod(method); //クレカ（仮）
		payment.setStatus("PAID");
		payment.setTransactionId(sessionId);
		payment.setPaymentDate(LocalDateTime.now());
		payment.setDueDate(LocalDateTime.now());
		
		//accountingにも会計収支として登録
		paymentRepository.save(payment);
		AccountingCategory category = accountingCategoryRepository.findById(1)
				.orElseThrow(() -> new RuntimeException("カテゴリが見つかりません"));
		Accounting accounting = new Accounting();
		
		accounting.setDistrict(loginUser.getDistrict()); //ユーザの所属町
		accounting.setRecordedBy(loginUser); // 登録者
		accounting.setType(Accounting.Type.収入);
		accounting.setAccountingCategory(category);
		accounting.setAmount(membershipFee.getAmount());
		accounting.setDescription("町内会費支払い(stripe)");
		accounting.setTransactionDate(payment.getPaymentDate());
		accountingRepository.save(accounting);
		
	}
	
	public Optional<BigDecimal> paymentCheck(User loginUser) {
		
		   // 年度範囲の取得
	    int currentYear = Year.now().getValue();
	    LocalDateTime startOfYear = LocalDateTime.of(currentYear, 1, 1, 0, 0);
	    LocalDateTime endOfYear = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);

	    // 会費支払い済みチェック（accountingから検索）
	    Optional<Accounting> accountingOpt = accountingRepository.findOneByAllConditions(
	            loginUser.getId(),
	            1, // 1は「町内会費収入カテゴリID」
	            Accounting.Type.収入,
	            startOfYear,
	            endOfYear
	    );

	    // すでに支払済みなら Optional.empty() を返す
	    if (accountingOpt.isPresent()) {
	        return Optional.empty();
	    }

	    // 支払設定金額を取得
	    MembershipFee fee = membershipFeeService.sameDistrictIdAndYear(
	            loginUser.getDistrict().getId(), currentYear
	    ).orElseThrow(() -> new IllegalArgumentException("今年の町内会費支払金額が設定されていません"));

	    // 支払うべき金額を返す
	    return Optional.of(fee.getAmount());
	}
}
