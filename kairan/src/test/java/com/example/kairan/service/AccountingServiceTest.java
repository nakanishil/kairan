package com.example.kairan.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kairan.entity.Accounting;
import com.example.kairan.entity.AccountingCategory;
import com.example.kairan.entity.District;
import com.example.kairan.entity.User;
import com.example.kairan.form.AccountingEditForm;
import com.example.kairan.form.AccountingRegiForm;
import com.example.kairan.repository.AccountingRepository;
import com.example.kairan.service.AccountingCategoryService;
import com.example.kairan.service.AccountingService;

@ExtendWith(MockitoExtension.class)
public class AccountingServiceTest {

	@Mock
	private AccountingRepository accountingRepository;
	
	@Mock
	private AccountingCategoryService accountingCategoryService;
	
	@InjectMocks
	private AccountingService accountingService;
	
	private District district;
	private User loginUser;
	private AccountingCategory category;
	
	@BeforeEach
	void setUp() {
	district = new District();
    district.setId(1);
    district.setAssociation("テスト町内会");
    
    loginUser = new User();
    loginUser.setId(1);
    loginUser.setDistrict(district);
    
    category = new AccountingCategory();
    category.setId(1);
    
	}
	
// CSV出力	
	@Test
	void findForCsvExport_年月あり_typeあり_categoryありの場合() {
		// Arrange
		Integer districtId = 1;
		YearMonth yearMonth = YearMonth.of(2024, 4);
		Accounting.Type type = Accounting.Type.収入;
		Integer categoryId = 10;
		
		List<Accounting> expectedList = Collections.singletonList(new Accounting());
		when(accountingRepository.
				findByDistrictIdAndTransactionDateBetweenAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(
						any(), any(), any(), any(), any())).thenReturn(expectedList);
		
		// Act
		List<Accounting> result = accountingService.
				findForCsvExport(districtId, yearMonth, type, categoryId);
		
		// Assert
		assertThat(result).isEqualTo(expectedList);
		
		// モックが呼ばれたか検証
		verify(accountingRepository, times(1)).
			findByDistrictIdAndTransactionDateBetweenAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(
					eq(districtId), any(),	any(), eq(type), eq(categoryId));
	}
	
	@Test
	void findForCsvExport_年月あり_typeあり_categoryなしの場合() {
		// arrange
		Integer districtId = 1;
		YearMonth yearMonth = YearMonth.of(2024, 4);
		Accounting.Type type = Accounting.Type.収入;
		Integer categoryId = null;
		
		List<Accounting> expectedList = Collections.singletonList(new Accounting());
		when(accountingRepository.
				findByDistrictIdAndTransactionDateBetweenAndTypeAndDeletedAtIsNull(
						any(), any(), any(), any())).thenReturn(expectedList);
		
		// Act
		List<Accounting> result = accountingService.
				findForCsvExport(districtId, yearMonth, type, categoryId);
		
		// Assert
		assertThat(result).isEqualTo(expectedList);
		
		// モックが呼ばれたか検証
		verify(accountingRepository, times(1)).
		findByDistrictIdAndTransactionDateBetweenAndTypeAndDeletedAtIsNull(
					eq(districtId), any(),	any(), eq(type));
	}
	
	@Test
	void findForCsvExport_年月あり_typeなし_categoryありの場合() {
		// arrange
		Integer districtId = 1;
		YearMonth yearMonth = YearMonth.of(2024, 4);
		Accounting.Type type = null;
		Integer categoryId = 10;
		
		List<Accounting> expectedList = Collections.singletonList(new Accounting());
		when(accountingRepository.
				findByDistrictIdAndTransactionDateBetweenAndAccountingCategoryIdAndDeletedAtIsNull(
						any(), any(), any(), any())).thenReturn(expectedList);
		
		// Act
		List<Accounting> result = accountingService.
				findForCsvExport(districtId, yearMonth, type, categoryId);
		
		// Assert
		assertThat(result).isEqualTo(expectedList);
		
		// モックが呼ばれたか検証
		verify(accountingRepository, times(1)).
		findByDistrictIdAndTransactionDateBetweenAndAccountingCategoryIdAndDeletedAtIsNull(
				eq(districtId), any(), any(), eq(categoryId));
	}
	
	@Test
	void findForCsvExport_年月あり_typeなし_categoryなしの場合() {
		// arrange
		Integer districtId = 1;
		YearMonth yearMonth = YearMonth.of(2024, 4);
		Accounting.Type type = null;
		Integer categoryId = null;
		
		List<Accounting> expectedList = Collections.singletonList(new Accounting());
		when(accountingRepository.
				findByDistrictIdAndTransactionDateBetweenAndDeletedAtIsNull(
						any(), any(), any())).thenReturn(expectedList);
		
		// Act
		List<Accounting> result = accountingService.
				findForCsvExport(districtId, yearMonth, type, categoryId);
		
		// Assert
		assertThat(result).isEqualTo(expectedList);
		
		// モックが呼ばれたか検証
		verify(accountingRepository, times(1)).
		findByDistrictIdAndTransactionDateBetweenAndDeletedAtIsNull(
				eq(districtId), any(), any());
	}
	
	@Test
	void findForCsvExport_年月なし_typeあり_categoryありの場合() {
		// arrange
		Integer districtId = 1;
		YearMonth yearMonth = null;
		Accounting.Type type = Accounting.Type.収入;
		Integer categoryId = 10;
		
		List<Accounting> expectedList = Collections.singletonList(new Accounting());
		when(accountingRepository.
				findByDistrictIdAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(
						any(), any(), any())).thenReturn(expectedList);
		
		// Act
		List<Accounting> result = accountingService.
				findForCsvExport(districtId, yearMonth, type, categoryId);
		
		// Assert
		assertThat(result).isEqualTo(expectedList);
		
		// モックが呼ばれたか検証
		verify(accountingRepository, times(1)).
		findByDistrictIdAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(
				eq(districtId), eq(type), eq(categoryId));
	}
	
	@Test
	void findForCsvExport_年月なし_typeあり_categoryなしの場合() {
		// arrange
		Integer districtId = 1;
		YearMonth yearMonth = null;
		Accounting.Type type = Accounting.Type.収入;
		Integer categoryId = null;
		
		List<Accounting> expectedList = Collections.singletonList(new Accounting());
		when(accountingRepository.
				findByDistrictIdAndTypeAndDeletedAtIsNull(
						any(), any())).thenReturn(expectedList);
		
		// Act
		List<Accounting> result = accountingService.
				findForCsvExport(districtId, yearMonth, type, categoryId);
		
		// Assert
		assertThat(result).isEqualTo(expectedList);
		
		// モックが呼ばれたか検証
		verify(accountingRepository, times(1)).
			findByDistrictIdAndTypeAndDeletedAtIsNull(
				eq(districtId), eq(type));
	}
	
	@Test
	void findForCsvExport_年月なし_typeなし_categoryありの場合() {
		// arrange
		Integer districtId = 1;
		YearMonth yearMonth = null;
		Accounting.Type type = null;
		Integer categoryId = 10;
		
		List<Accounting> expectedList = Collections.singletonList(new Accounting());
		when(accountingRepository.
				findByDistrictIdAndAccountingCategoryIdAndDeletedAtIsNull(
						any(), any())).thenReturn(expectedList);
		
		// Act
		List<Accounting> result = accountingService.
				findForCsvExport(districtId, yearMonth, type, categoryId);
		
		// Assert
		assertThat(result).isEqualTo(expectedList);
		
		// モックが呼ばれたか検証
		verify(accountingRepository, times(1)).
				findByDistrictIdAndAccountingCategoryIdAndDeletedAtIsNull(
						eq(districtId), eq(categoryId));
	}
	
	@Test
	void findForCsvExport_年月なし_typeなし_categoryなしの場合() {
		// arrange
		Integer districtId = 1;
		YearMonth yearMonth = null;
		Accounting.Type type = null;
		Integer categoryId = null;
		
		List<Accounting> expectedList = Collections.singletonList(new Accounting());
		when(accountingRepository.
				findByDistrictIdAndDeletedAtIsNull(
						any())).thenReturn(expectedList);
		
		// Act
		List<Accounting> result = accountingService.
				findForCsvExport(districtId, yearMonth, type, categoryId);
		
		// Assert
		assertThat(result).isEqualTo(expectedList);
		
		// モックが呼ばれたか検証
		verify(accountingRepository, times(1)).
				findByDistrictIdAndDeletedAtIsNull(
						eq(districtId));
	}
	
// 登録メソッド
	@Test
	void accountingRegi_正常に会計項目が登録できる() {
		
		// arrange
		AccountingRegiForm form = new AccountingRegiForm();
		form.setTransactionDate(LocalDate.of(2024, 4, 1));
		form.setType(Accounting.Type.収入);
		form.setAccountingCategoryId(1);
		form.setAmount(new BigDecimal("1000"));
		form.setDescription("テスト説明");
		
		when(accountingCategoryService.findById(form.getAccountingCategoryId()))
			.thenReturn(Optional.of(category));
		// act
		accountingService.accountingRegi(loginUser,  form);
		
		// assert
		verify(accountingRepository, times(1)).save(argThat(accounting -> 
			accounting.getDistrict().equals(district) &&
			accounting.getRecordedBy().equals(loginUser) &&
			accounting.getType().equals(form.getType()) &&
			accounting.getAccountingCategory().equals(category) &&
			accounting.getAmount().equals(form.getAmount()) &&
			accounting.getDescription().equals(form.getDescription())
				));
	}
	
	@Test
	void accountingRegi_カテゴリが存在しない場合例外をスロー() {
		// arrange
		AccountingRegiForm form = new AccountingRegiForm();
		form.setTransactionDate(LocalDate.of(2024, 4, 1));
		form.setType(Accounting.Type.収入);
		form.setAccountingCategoryId(999);
		form.setAmount(new BigDecimal("1000"));
		form.setDescription("テスト説明");
		
		when(accountingCategoryService.findById(form.getAccountingCategoryId()))
			.thenReturn(Optional.empty());
		
		// assert
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> accountingService.accountingRegi(loginUser, form)
		);
		
		assertThat(exception.getMessage()).isEqualTo("指定されたカテゴリが存在しません");
	}
	
// 編集機能
	// 編集
	@Test
	void accountingEdit_正常に会計項目が編集できる() {
		// arrange
		AccountingEditForm form = new AccountingEditForm();
		form.setId(1);
		form.setTransactionDate(LocalDate.of(2024, 4, 1));
		form.setType(Accounting.Type.収入);
		form.setAccountingCategoryId(1);
		form.setAmount(new BigDecimal("1000"));
		form.setDescription("新しいテスト説明");
		
		Accounting accounting = new Accounting();
		accounting.setId(1);
		accounting.setTransactionDate(LocalDate.of(2024, 5, 1).atStartOfDay());
		accounting.setType(Accounting.Type.収入);
		accounting.setDistrict(district);
		accounting.setAccountingCategory(category);
		accounting.setAmount(new BigDecimal("500"));
		accounting.setDescription("古いテスト説明");
		
		when(accountingCategoryService.findById(form.getAccountingCategoryId()))
			.thenReturn(Optional.of(category));
		when(accountingRepository.findByIdAndDeletedAtIsNull(form.getId()))
				.thenReturn(Optional.of(accounting));
		
		// act
		accountingService.accountingEdit(loginUser, form);
		// assert
		verify(accountingRepository, times(1)).save(argThat(updated -> 
		updated.getDistrict().equals(district) &&
		updated.getRecordedBy().equals(loginUser) &&
		updated.getType().equals(form.getType()) &&
		updated.getAccountingCategory().equals(category) &&
		updated.getAmount().equals(form.getAmount()) &&
		updated.getDescription().equals(form.getDescription())
		));
	}
	
	@Test
	void accountingEdit_カテゴリが存在しない場合例外をスロー() {
		// arrange
		AccountingEditForm form = new AccountingEditForm();
		form.setId(1);
		form.setTransactionDate(LocalDate.of(2024, 4, 1));
		form.setType(Accounting.Type.収入);
		form.setAccountingCategoryId(1);
		form.setAmount(new BigDecimal("1000"));
		form.setDescription("新しいテスト説明");
		
		Accounting accounting = new Accounting();
		accounting.setId(1);
		accounting.setTransactionDate(LocalDate.of(2024, 5, 1).atStartOfDay());
		accounting.setType(Accounting.Type.収入);
		accounting.setDistrict(district);
		accounting.setAccountingCategory(category);
		accounting.setAmount(new BigDecimal("500"));
		accounting.setDescription("古いテスト説明");
		
		when(accountingCategoryService.findById(form.getAccountingCategoryId()))
			.thenReturn(Optional.empty());
		
		// assert
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> accountingService.accountingEdit(loginUser, form)
		);
		
		assertThat(exception.getMessage()).isEqualTo("指定されたカテゴリが存在しません");
	}
	
	@Test
	void accountingEdit_accountingIdでデータを抽出できない場合例外をスロー() {
		// arrange
		AccountingEditForm form = new AccountingEditForm();
		form.setId(1);
		form.setTransactionDate(LocalDate.of(2024, 4, 1));
		form.setType(Accounting.Type.収入);
		form.setAccountingCategoryId(1);
		form.setAmount(new BigDecimal("1000"));
		form.setDescription("新しいテスト説明");
		
		Accounting accounting = new Accounting();
		accounting.setId(1);
		accounting.setTransactionDate(LocalDate.of(2024, 5, 1).atStartOfDay());
		accounting.setType(Accounting.Type.収入);
		accounting.setDistrict(district);
		accounting.setAccountingCategory(category);
		accounting.setAmount(new BigDecimal("500"));
		accounting.setDescription("古いテスト説明");
		
		when(accountingCategoryService.findById(form.getAccountingCategoryId()))
		.thenReturn(Optional.of(category));
		when(accountingRepository.findByIdAndDeletedAtIsNull(form.getId()))
		.thenReturn(Optional.empty());
		
		// assert
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> accountingService.accountingEdit(loginUser, form)
		);
		
		assertThat(exception.getMessage()).isEqualTo("指定された項目が存在しないか、既に削除されています");
	}

// 削除（ソフトデリート）
	@Test
	void softDeletedAccounting_正常に削除できる() {
		// arrange
		Accounting accounting = new Accounting();
		accounting.setId(1);
		accounting.setTransactionDate(LocalDate.of(2024, 5, 1).atStartOfDay());
		accounting.setType(Accounting.Type.収入);
		accounting.setDistrict(district);
		accounting.setAccountingCategory(category);
		accounting.setAmount(new BigDecimal("500"));
		accounting.setDescription("古いテスト説明");
		accounting.setRecordedBy(loginUser);
		
		User deleteMan = new User();
		deleteMan.setId(2);
		deleteMan.setDistrict(district);
		
		when(accountingRepository.findByIdAndDeletedAtIsNull(accounting.getId()))
			.thenReturn(Optional.of(accounting));
		
		// act
		accountingService.softDeletedAccounting(deleteMan, accounting.getId());
		
		// assert
		verify(accountingRepository, times(1)).save(argThat(updated ->
				updated.getDeletedAt() != null &&
				updated.getRecordedBy().equals(deleteMan)
		));
	}
	
	@Test
	void softDeletedAccounting_指定したIDが存在しない場合は例外をスロー() {
		// arrange
		when(accountingRepository.findByIdAndDeletedAtIsNull(1))
				.thenReturn(Optional.empty());
		
		// act
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> accountingService.softDeletedAccounting(loginUser, 1)
		);
		// assert
		assertThat(exception.getMessage()).isEqualTo("指定したIDが存在しないか既に削除されています");
	}
}
