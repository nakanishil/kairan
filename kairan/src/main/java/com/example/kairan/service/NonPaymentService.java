package com.example.kairan.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.kairan.entity.Accounting;
import com.example.kairan.entity.AccountingCategory;
import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.Payment;
import com.example.kairan.entity.PaymentMethod;
import com.example.kairan.entity.User;
import com.example.kairan.form.DepositProcessingForm;
import com.example.kairan.repository.AccountingCategoryRepository;
import com.example.kairan.repository.AccountingRepository;
import com.example.kairan.repository.MembershipFeeRepository;
import com.example.kairan.repository.PaymentMethodRepository;
import com.example.kairan.repository.PaymentRepository;
import com.example.kairan.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NonPaymentService {
	private final AccountingRepository accountingRepository;
	private final AccountingCategoryRepository accountingCategoryRepository;
	private final PaymentMethodRepository paymentMethodRepository;
	private final PaymentRepository paymentRepository;
	private final MembershipFeeService membershipFeeService;
	private final UserRepository userRepository;
	private final MembershipFeeRepository membershipFeeRepository;
	
	// 未納者一覧取得
	public Page<User> nonPaymentPage(
			User loginUser,
			int year,
			Pageable pageable
			) 
	{
		String roleName = loginUser.getRole().getName();
		String association = loginUser.getDistrict().getAssociation();
		String area = loginUser.getDistrict().getArea();
		LocalDateTime start = LocalDate.of(year,  1, 1).atStartOfDay();
		LocalDateTime end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
		
		Page<User> UserPage = switch (roleName) {
		case "ROLE_町内会長" -> accountingRepository.findNonPayersByAssociationIfMembershipFeeExists(
				association, 1, Accounting.Type.収入, year, start, end, pageable);
		case "ROLE_区長" -> accountingRepository.findNonPayersByAssociationAreaIfMembershipFeeExists(
				association, 1, Accounting.Type.収入, area, year, start, end, pageable);
		default -> throw new IllegalArgumentException("未対応の役職です");
		};
		
		return UserPage;
	}
	
	// userIdからuserを取得
	public User findUserId(int userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("指定したユーザが見つかりません"));
		return user;
	}
	
	// IDが適正かチェック
	public void nonPaymentRegiCheck(
			User loginUser,
			User payer
			) 
	{
		// ログインユーザ情報の抽出
		String roleName = loginUser.getRole().getName();
		String loginUserAssociation = loginUser.getDistrict().getAssociation();
		String loginUserArea = loginUser.getDistrict().getArea();
		
		// 会費決済者のユーザ情報抽出
		String payerAssociation = payer.getDistrict().getAssociation();
		String payerArea = payer.getDistrict().getArea();
		
		 switch (roleName) {
	        case "ROLE_町内会長":
	            if (!loginUserAssociation.equals(payerAssociation)) {
	                throw new IllegalArgumentException("対象決済者は管轄外のIDです");
	            }
	            break;

	        case "ROLE_区長":
	            if (!loginUserAssociation.equals(payerAssociation) ||
	                !loginUserArea.equals(payerArea)) {
	                throw new IllegalArgumentException("対象決済者は管轄外のIDです");
	            }
	            break;

	        default:
	            throw new IllegalArgumentException("未対応の役職です");
	    }
	}
		
	
	// 現金決済者の入金処理
	public void depositProcessing(User payer, DepositProcessingForm form) {
		/*accountingでの収入処理、 
		 * paymentsでの支払処理 */
		AccountingCategory accountingCategory = accountingCategoryRepository.findById(1)
				.orElseThrow(() -> new IllegalArgumentException("指定したカテゴリが存在しません"));
		
		Accounting ac = new Accounting();
		ac.setDistrict(payer.getDistrict());
		ac.setRecordedBy(payer);
		ac.setType(Accounting.Type.収入);
		ac.setAccountingCategory(accountingCategory);
		ac.setAmount(form.getAmount());
		ac.setDescription(form.getDescription());
		ac.setTransactionDate(form.getTransactionDate()); // 取引日
		
		PaymentMethod method = paymentMethodRepository.findById(form.getPaymentMethodId())
				.orElseThrow(() -> new IllegalArgumentException("指定した支払方法が存在しません"));
		MembershipFee fee = membershipFeeService.sameDistrictIdAndYear(payer.getDistrict().getId(), form.getYear())
				.orElseThrow(() -> new IllegalArgumentException("年会費情報が見つかりません"));
		
		Payment pa = new Payment();
		pa.setUser(payer); // 支払いユーザ
		pa.setAmount(form.getAmount()); // 金額
		pa.setMembershipFee(fee); // 支払設定金額
		pa.setPaymentMethod(method); //支払方法
		pa.setStatus(null); // 支払いの状態
		pa.setPaymentDate(form.getTransactionDate()); // 支払い実行日 == 取引日
		
		// 保存
		accountingRepository.save(ac);
		paymentRepository.save(pa);
	}
	
	public List<Integer> findMembershipFeeYear(String association) {
	    return membershipFeeRepository.findByDistrictAssociationAndDeletedAtIsNull(association).stream()
	        .map(MembershipFee::getYear)
	        .distinct()
	        .sorted(Comparator.reverseOrder())
	        .toList();
	}
}
