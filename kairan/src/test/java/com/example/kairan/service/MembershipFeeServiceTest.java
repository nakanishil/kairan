package com.example.kairan.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.kairan.entity.District;
import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.User;
import com.example.kairan.form.MembershipFeeEditForm;
import com.example.kairan.form.MembershipFeeForm;
import com.example.kairan.repository.MembershipFeeRepository;

@ExtendWith(MockitoExtension.class)
public class MembershipFeeServiceTest {
	
	@InjectMocks
	private MembershipFeeService membershipFeeService;
	
	@Mock
	private MembershipFeeRepository membershipFeeRepository;
	
	private District district;
	private User loginUser;
	private MembershipFee membershipFee;
	
	@BeforeEach
	void setUp() {
	district = new District();
    district.setId(1);
    district.setAssociation("テスト町内会");
    
    loginUser = new User();
    loginUser.setId(1);
    loginUser.setDistrict(district);
    
    
    
    membershipFee = new MembershipFee();
    membershipFee.setId(1);
    membershipFee.setYear(2025);
	membershipFee.setAmount(BigDecimal.valueOf(5000));
	membershipFee.setDistrict(loginUser.getDistrict());
	membershipFee.setRecordedBy(loginUser);
    
    
//    category = new AccountingCategory();
//    category.setId(1);
    
	}
	
// 登録
	@Test
	void regimebershipFee_正常に設定金額が登録できる(){
		MembershipFeeForm form = new MembershipFeeForm();
		form.setYear(2025);
		form.setAmount(5000);
		
		// act
		membershipFeeService.regiMembershipFee(form, loginUser);
		
		// assert
		verify(membershipFeeRepository, times(1)).save(argThat(membershipFee ->
			membershipFee.getYear().equals(form.getYear()) &&
			membershipFee.getAmount().equals(BigDecimal.valueOf(form.getAmount())) &&
			membershipFee.getDistrict().equals(loginUser.getDistrict()) &&
			membershipFee.getRecordedBy().equals(loginUser)
		));
	}
	
// 編集
	@Test
	void editMembershipFee_正常に設定金額が更新できる() {
		// arrange
		MembershipFeeEditForm form = new MembershipFeeEditForm();
		form.setId(1);
		form.setYear(9999);
		form.setAmount(9999);
		
		User loginUser2 = new User();
	    loginUser2.setId(2);
	    loginUser2.setDistrict(district);
		
	    when(membershipFeeRepository.findByIdAndDeletedAtIsNull(1))
	    	.thenReturn(Optional.of(membershipFee));
	    // act
		membershipFeeService.editMembershipFee(form, loginUser2);
		
		//assert
		verify(membershipFeeRepository, times(1)).save(argThat(membershipFee ->
			membershipFee.getYear().equals(form.getYear()) &&
			membershipFee.getAmount().equals(BigDecimal.valueOf(form.getAmount())) &&
			membershipFee.getDistrict().equals(loginUser2.getDistrict()) &&
			membershipFee.getRecordedBy().equals(loginUser2)
		));
	}
	
	@Test
	void editMembershipFee_指定したIDが存在しない場合例外がスローされる() {
		// arange
		MembershipFeeEditForm form = new MembershipFeeEditForm();
		form.setId(1);
		form.setYear(9999);
		form.setAmount(9999);
		
		when(membershipFeeRepository.findByIdAndDeletedAtIsNull(1))
			.thenReturn(Optional.empty());
		
		// act assert
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> membershipFeeService.editMembershipFee(form, loginUser)
		);
		
		assertThat(exception.getMessage()).isEqualTo("指定したIDが存在しないか既に削除されています");
	}
	
// 削除
	@Test
	void softDeletedMembershipFee_正常にソフトデリートが実行できる() {
		// arrange
		when(membershipFeeRepository.findByIdAndDeletedAtIsNull(membershipFee.getId()))
			.thenReturn(Optional.of(membershipFee));
		
		// act
		membershipFeeService.softDeletedMembershipFee(membershipFee.getId());
		
		// assert
		assertThat(membershipFee.getDeletedAt()).isNotNull();
	}
	
	@Test
	void softDeletedMembershipFee_指定したIDが見つからない場合は例外をスロー() {
		// arrange
		when(membershipFeeRepository.findByIdAndDeletedAtIsNull(1))
		.thenReturn(Optional.empty());
		
		// act assert
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> membershipFeeService.softDeletedMembershipFee(membershipFee.getId())
		);
		assertThat(exception.getMessage()).isEqualTo("指定したIDが存在しないか既に削除されています");
	}
	
// 金額設定履歴取得
	@Test
	void getMembershipFeeList_正常にソートされた一覧を取得できる() {
		// arrange
		Pageable inputPageable = PageRequest.of(0, 10); // ソート無しで渡す
		Page<MembershipFee> mockPage = Page.empty();
		
		when(membershipFeeRepository.findByDistrictAndDeletedAtIsNull(
				eq(loginUser.getDistrict()), any(Pageable.class)
				)).thenReturn(mockPage);
		
		// act
		Page<MembershipFee> result = membershipFeeService.
				getMembershipFeeList(loginUser, inputPageable);
		
		// assert
		assertThat(result).isNotNull();
		verify(membershipFeeRepository).findByDistrictAndDeletedAtIsNull(
				eq(loginUser.getDistrict()),
				argThat(pageable ->
					pageable.getSort().getOrderFor("year") != null &&
					pageable.getSort().getOrderFor("year").getDirection() == Sort.Direction.DESC
				)
		);
	}
}
