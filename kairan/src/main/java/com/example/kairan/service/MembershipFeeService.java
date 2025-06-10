package com.example.kairan.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.User;
import com.example.kairan.form.MembershipFeeEditForm;
import com.example.kairan.form.MembershipFeeForm;
import com.example.kairan.repository.MembershipFeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipFeeService {
	
	private final MembershipFeeRepository membershipFeeRepository;
	
	// 指定したIDを取得（ソフトデリート除く）
	public Optional<MembershipFee> findByDistrictAndDeletedAtIsNull(int id) {
		return membershipFeeRepository.findByIdAndDeletedAtIsNull(id);
	}
	
	// 年と町内の指定で取得
	public Optional<MembershipFee> sameDistrictIdAndYear(int districtId, int year) {
		return membershipFeeRepository.findByDistrictIdAndYearAndDeletedAtIsNull(districtId, year);
	}
	
	

	
	// 登録
	public void regiMembershipFee(MembershipFeeForm form, User loginUser) {
		MembershipFee membershipFee = new MembershipFee();
		membershipFee.setYear(form.getYear());
		membershipFee.setAmount(BigDecimal.valueOf(form.getAmount()));
		membershipFee.setDistrict(loginUser.getDistrict());
		membershipFee.setRecordedBy(loginUser);
		
		membershipFeeRepository.save(membershipFee);
	}
	
	// 金額設定履歴取得
	public Page<MembershipFee> getMembershipFeeList(User loginUser,Pageable pageable) {
		Pageable sortedPageable = PageRequest.of(
		        pageable.getPageNumber(),
		        pageable.getPageSize(),
		        Sort.by(Sort.Direction.DESC, "year")
		    );
		
		return membershipFeeRepository.
				findByDistrictAndDeletedAtIsNull(loginUser.getDistrict(), sortedPageable);
	}
	
	// 編集
	public void editMembershipFee(MembershipFeeEditForm form, User loginUser) {
		
		MembershipFee membershipFee = 
				findByDistrictAndDeletedAtIsNull(form.getId())
						.orElseThrow(() -> new IllegalArgumentException
								("指定したIDが存在しないか既に削除されています"));
		
		membershipFee.setYear(form.getYear());
		membershipFee.setAmount(BigDecimal.valueOf(form.getAmount()));
		membershipFee.setDistrict(loginUser.getDistrict());
		membershipFee.setRecordedBy(loginUser);
		
		// 明示的に記載
		membershipFeeRepository.save(membershipFee);
		
	}
	
	// 削除
	public void softDeletedMembershipFee(int membershipFeeId) {
		MembershipFee membershipFee = 
				findByDistrictAndDeletedAtIsNull(membershipFeeId)
						.orElseThrow(() -> new IllegalArgumentException
								("指定したIDが存在しないか既に削除されています"));
		
		membershipFee.softDelete();
		// 明示的に記載
		membershipFeeRepository.save(membershipFee);
	}
}
