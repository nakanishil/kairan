package com.example.kairan.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.kairan.entity.Accounting;
import com.example.kairan.entity.AccountingCategory;
import com.example.kairan.entity.District;
import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.Payment;
import com.example.kairan.entity.PaymentMethod;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.form.DepositProcessingForm;
import com.example.kairan.repository.AccountingCategoryRepository;
import com.example.kairan.repository.AccountingRepository;
import com.example.kairan.repository.MembershipFeeRepository;
import com.example.kairan.repository.PaymentMethodRepository;
import com.example.kairan.repository.PaymentRepository;
import com.example.kairan.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class NonPaymentServiceTest {

	   @Mock
	    AccountingRepository accountingRepository;

	    @Mock
	    AccountingCategoryRepository accountingCategoryRepository;

	    @Mock
	    PaymentMethodRepository paymentMethodRepository;

	    @Mock
	    PaymentRepository paymentRepository;

	    @Mock
	    MembershipFeeService membershipFeeService;

	    @Mock
	    UserRepository userRepository;

	    @Mock
	    MembershipFeeRepository membershipFeeRepository;

	    @InjectMocks
	    NonPaymentService nonPaymentService;
	    
	    private User loginUser;
	    private District district;
	    
	    @BeforeEach
	    void setUp() {
	        district = new District();
	        district.setAssociation("中央町内会");
	        district.setArea("1区");

	        // ログインユーザーの基本設定（役職はテストごと変更）
	        loginUser = new User();
	        loginUser.setDistrict(district);

	        Role role = new Role(); 
	        loginUser.setRole(role);
	    }
	    
	    @Test
	    void nonPaymentPage_町内会長はAssociation単位で未納者を取得できる() {
	    	loginUser.getRole().setName("ROLE_町内会長");
	    	 int year = 2024;
	         Pageable pageable = PageRequest.of(0, 10);
	         Page<User> mockPage = new PageImpl<>(List.of());

	         when(accountingRepository.findNonPayersByAssociationIfMembershipFeeExists(
	                 eq("中央町内会"), eq(1), eq(Accounting.Type.収入),
	                 eq(year), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)
	         )).thenReturn(mockPage);

	         Page<User> result = nonPaymentService.nonPaymentPage(loginUser, year, pageable);

	         assertEquals(mockPage, result);
	         verify(accountingRepository).findNonPayersByAssociationIfMembershipFeeExists(
	                 eq("中央町内会"), eq(1), eq(Accounting.Type.収入),
	                 eq(year), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)
	         );
	     }
	    
	    @Test
	    void nonPaymentPage_区長はAssociationとArea単位で未納者を取得できる() {
	        // 役職を区長に設定
	        loginUser.getRole().setName("ROLE_区長");

	        int year = 2024;
	        Pageable pageable = PageRequest.of(0, 10);
	        Page<User> mockPage = new PageImpl<>(List.of());

	        when(accountingRepository.findNonPayersByAssociationAreaIfMembershipFeeExists(
	                eq("中央町内会"), eq(1), eq(Accounting.Type.収入),
	                eq("1区"), eq(year), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)
	        )).thenReturn(mockPage);

	        Page<User> result = nonPaymentService.nonPaymentPage(loginUser, year, pageable);

	        assertEquals(mockPage, result);
	        verify(accountingRepository).findNonPayersByAssociationAreaIfMembershipFeeExists(
	                eq("中央町内会"), eq(1), eq(Accounting.Type.収入),
	                eq("1区"), eq(year), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)
	        );
	    }
	    
	    @Test
	    void nonPaymentPage_未対応の役職は例外をスローする() {
	        // 役職を会員に設定（未対応）
	        loginUser.getRole().setName("ROLE_会員");

	        int year = 2024;
	        Pageable pageable = PageRequest.of(0, 10);

	        IllegalArgumentException exception = assertThrows(
	            IllegalArgumentException.class,
	            () -> nonPaymentService.nonPaymentPage(loginUser, year, pageable)
	        );

	        assertEquals("未対応の役職です", exception.getMessage());
	    }
	    
	    @Test
	    void nonPaymentRegiCheck_町内会長は同じ町内会のユーザを処理できる() {
	        loginUser.getRole().setName("ROLE_町内会長");

	        User payer = new User();
	        District payerDistrict = new District();
	        payerDistrict.setAssociation("中央町内会");
	        payer.setDistrict(payerDistrict);

	        assertDoesNotThrow(() -> {
	            nonPaymentService.nonPaymentRegiCheck(loginUser, payer);
	        });
	    }
	    
	    @Test
	    void nonPaymentRegiCheck_区長が異なる区のユーザを処理しようとすると例外がスローされる() {
	        loginUser.getRole().setName("ROLE_区長");

	        // 処理対象ユーザー（payer）：中央町内会・2区（areaが異なる）
	        User payer = new User();
	        District payerDistrict = new District();
	        payerDistrict.setAssociation("中央町内会");
	        payerDistrict.setArea("2区");
	        payer.setDistrict(payerDistrict);

	        IllegalArgumentException exception = assertThrows(
	            IllegalArgumentException.class,
	            () -> nonPaymentService.nonPaymentRegiCheck(loginUser, payer)
	        );

	        assertEquals("対象決済者は管轄外のIDです", exception.getMessage());
	    }
	    
	    @Test
	    void nonPaymentRegiCheck_町内会長が異なる町内会のユーザを処理しようとすると例外がスローされる() {
	        // ログインユーザー（町内会長）：中央町内会
	        loginUser.getRole().setName("ROLE_町内会長");

	        // 処理対象ユーザー（payer）：南町内会（associationが異なる）
	        User payer = new User();
	        District payerDistrict = new District();
	        payerDistrict.setAssociation("南町内会");
	        payer.setDistrict(payerDistrict);

	        IllegalArgumentException exception = assertThrows(
	            IllegalArgumentException.class,
	            () -> nonPaymentService.nonPaymentRegiCheck(loginUser, payer)
	        );

	        assertEquals("対象決済者は管轄外のIDです", exception.getMessage());
	    }
	    
	    @Test
	    void nonPaymentRegiCheck_未対応の役職は例外をスローする() {
	        loginUser.getRole().setName("ROLE_会員");

	        User payer = new User();
	        District payerDistrict = new District();
	        payerDistrict.setAssociation("中央町内会");
	        payerDistrict.setArea("1区");
	        payer.setDistrict(payerDistrict);

	        IllegalArgumentException exception = assertThrows(
	            IllegalArgumentException.class,
	            () -> nonPaymentService.nonPaymentRegiCheck(loginUser, payer)
	        );

	        assertEquals("未対応の役職です", exception.getMessage());
	    }
	    
	    @Test
	    void depositProcessing_正常に会計と支払情報が保存される() {
	        // payer（支払いユーザ）
	        User payer = new User();
	        District district = new District();
	        district.setId(1);
	        payer.setDistrict(district);

	        DepositProcessingForm form = new DepositProcessingForm();
	        form.setAmount(BigDecimal.valueOf(3000));
	        form.setDescription("現金での支払い");
	        form.setTransactionDate(LocalDateTime.now());
	        form.setPaymentMethodId(1);
	        form.setYear(2024);

	        AccountingCategory mockCategory = new AccountingCategory();
	        PaymentMethod mockMethod = new PaymentMethod();
	        MembershipFee mockFee = new MembershipFee();

	        when(accountingCategoryRepository.findById(1)).thenReturn(Optional.of(mockCategory));
	        when(paymentMethodRepository.findById(1)).thenReturn(Optional.of(mockMethod));
	        when(membershipFeeService.sameDistrictIdAndYear(1, 2024)).thenReturn(Optional.of(mockFee));

	        // act
	        assertDoesNotThrow(() -> {
	            nonPaymentService.depositProcessing(payer, form);
	        });

	        verify(accountingRepository, times(1)).save(any(Accounting.class));
	        verify(paymentRepository, times(1)).save(any(Payment.class));
	    }
	    
	    @Test
	    void depositProcessing_カテゴリが存在しない場合は例外をスローする() {
	        User payer = new User();
	        District district = new District();
	        district.setId(1);
	        payer.setDistrict(district);

	        DepositProcessingForm form = new DepositProcessingForm();
	        form.setAmount(BigDecimal.valueOf(3000));
	        form.setDescription("現金での支払い");
	        form.setTransactionDate(LocalDateTime.now());
	        form.setPaymentMethodId(1);
	        form.setYear(2024);

	        // カテゴリが存在しない
	        when(accountingCategoryRepository.findById(1)).thenReturn(Optional.empty());

	        IllegalArgumentException exception = assertThrows(
	            IllegalArgumentException.class,
	            () -> nonPaymentService.depositProcessing(payer, form)
	        );

	        assertEquals("指定したカテゴリが存在しません", exception.getMessage());
	    }
	    
	    @Test
	    void depositProcessing_支払方法が存在しない場合は例外をスローする() {
	        // payer（支払いユーザ）
	        User payer = new User();
	        District district = new District();
	        district.setId(1);
	        payer.setDistrict(district);

	        // フォームの入力
	        DepositProcessingForm form = new DepositProcessingForm();
	        form.setAmount(BigDecimal.valueOf(3000));
	        form.setDescription("現金での支払い");
	        form.setTransactionDate(LocalDateTime.now());
	        form.setPaymentMethodId(1); // 存在しないID
	        form.setYear(2024);

	        // モック：カテゴリは存在する
	        when(accountingCategoryRepository.findById(1)).thenReturn(Optional.of(new AccountingCategory()));
	        // モック：支払方法が存在しない
	        when(paymentMethodRepository.findById(1)).thenReturn(Optional.empty());

	        IllegalArgumentException exception = assertThrows(
	            IllegalArgumentException.class,
	            () -> nonPaymentService.depositProcessing(payer, form)
	        );

	        assertEquals("指定した支払方法が存在しません", exception.getMessage());
	    }
	    
	    @Test
	    void depositProcessing_年会費情報が存在しない場合は例外をスローする() {
	        // payer（支払いユーザ）
	        User payer = new User();
	        District district = new District();
	        district.setId(1);
	        payer.setDistrict(district);

	        // フォームの入力
	        DepositProcessingForm form = new DepositProcessingForm();
	        form.setAmount(BigDecimal.valueOf(3000));
	        form.setDescription("現金での支払い");
	        form.setTransactionDate(LocalDateTime.now());
	        form.setPaymentMethodId(1);
	        form.setYear(2024);

	        // モック設定
	        when(accountingCategoryRepository.findById(1)).thenReturn(Optional.of(new AccountingCategory()));
	        when(paymentMethodRepository.findById(1)).thenReturn(Optional.of(new PaymentMethod()));
	        when(membershipFeeService.sameDistrictIdAndYear(1, 2024)).thenReturn(Optional.empty());

	        IllegalArgumentException exception = assertThrows(
	            IllegalArgumentException.class,
	            () -> nonPaymentService.depositProcessing(payer, form)
	        );

	        assertEquals("年会費情報が見つかりません", exception.getMessage());
	    }

}
