package com.example.kairan.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.kairan.entity.Accounting;
import com.example.kairan.entity.Accounting.Type;
import com.example.kairan.entity.AccountingCategory;
import com.example.kairan.entity.District;
import com.example.kairan.entity.User;
import com.example.kairan.form.AccountingEditForm;
import com.example.kairan.form.AccountingRegiForm;
import com.example.kairan.repository.AccountingRepository;
import com.example.kairan.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountingService {
	
	private final AccountingRepository accountingRepository;
	private final AccountingCategoryService accountingCategoryService;
	private final UserRepository userRepository;
	
	// accountingIdで取得
	public Optional<Accounting> findByAccountId(Integer accountingId){
		return accountingRepository.findByIdAndDeletedAtIsNull(accountingId);
	}
	
	// districtIdで未削除の会計データをページング取得
	public Page<Accounting> findByDistrict(Integer districtId, Pageable pageable){
		return accountingRepository.findByDistrictIdAndDeletedAtIsNull(districtId, pageable);
	}
	
	// districtId + 取引日の期間で未削除の会計データをページング取得
	public Page<Accounting> findByDistrictAndTransactionDateBetween(
			Integer districtId, LocalDateTime startDate, 
			LocalDateTime endDate, Pageable pageable){
		return accountingRepository.findByDistrictIdAndTransactionDateBetweenAndDeletedAtIsNull(
			    districtId, startDate, endDate, pageable);
	}
	
	// districtIdと期間を指定して合計金額を取得
	public BigDecimal getSumAmountByDistrictAndPeriod(
			Integer districtId, LocalDateTime startDate, LocalDateTime endDate) {
	    BigDecimal sum = accountingRepository.sumAmountByDistrictIdAndTransactionDateBetween(
	    		districtId, startDate, endDate);
	    return sum != null ? sum : BigDecimal.ZERO; // nullの場合は0を返す
	}
	
	// districtIdと期間を指定して収入の合計金額を取得
	public BigDecimal getSumIncomeByDistrictAndPeriod(Integer districtId, LocalDateTime startDate, LocalDateTime endDate) {
	    BigDecimal incomeSum = accountingRepository.sumIncomeByDistrictIdAndTransactionDateBetween(districtId, startDate, endDate);
	    return incomeSum != null ? incomeSum : BigDecimal.ZERO; // nullなら0を返す
	}

	// districtIdと期間を指定して支出の合計金額を取得
	public BigDecimal getSumExpenseByDistrictAndPeriod(Integer districtId, LocalDateTime startDate, LocalDateTime endDate) {
	    BigDecimal expenseSum = accountingRepository.sumExpenseByDistrictIdAndTransactionDateBetween(districtId, startDate, endDate);
	    return expenseSum != null ? expenseSum : BigDecimal.ZERO; // nullなら0を返す
	}
	
	// districtId,期間、収支タイプで未削除のデータをページング取得
	public Page<Accounting> findByDistrictAndTransactionDateBetweenAndType(
			Integer districtId, LocalDateTime startDate, LocalDateTime endDate, Type type, Pageable pageable){
		return accountingRepository.findByDistrictIdAndTransactionDateBetweenAndDeletedAtIsNullAndType(
				districtId, startDate, endDate, type,pageable);
	}
	
	// districtId,収支タイプで未削除のデータをページング取得
	public Page<Accounting> findByDistrictAndType(
			Integer districtId, Type type, Pageable pageable) {
		return accountingRepository.findByDistrictIdAndDeletedAtIsNullAndType(
				districtId, type, pageable);
	}
	
	// districtId と カテゴリで絞り込み
	public Page<Accounting> findByDistrictAndAccountingCategory(
	    Integer districtId, Integer accountingCategoryId, Pageable pageable) {
	    return accountingRepository.findByDistrictIdAndAccountingCategoryIdAndDeletedAtIsNull(districtId, accountingCategoryId, pageable);
	}

	// districtId と 期間 と カテゴリで絞り込み
	public Page<Accounting> findByDistrictAndTransactionDateBetweenAndAccountingCategory(
	    Integer districtId, LocalDateTime startDate, LocalDateTime endDate, Integer accountingCategoryId, Pageable pageable) {
	    return accountingRepository.findByDistrictIdAndTransactionDateBetweenAndAccountingCategoryIdAndDeletedAtIsNull(districtId, startDate, endDate, accountingCategoryId, pageable);
	}

	// districtId と 収支区分 と カテゴリで絞り込み
	public Page<Accounting> findByDistrictAndTypeAndAccountingCategory(
	    Integer districtId, com.example.kairan.entity.Accounting.Type type, Integer accountingCategoryId, Pageable pageable) {
	    return accountingRepository.findByDistrictIdAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(districtId, type, accountingCategoryId, pageable);
	}

	// districtId と 期間 と 収支区分 と カテゴリで絞り込み
	public Page<Accounting> findByDistrictAndTransactionDateBetweenAndTypeAndAccountingCategory(
	    Integer districtId, LocalDateTime startDate, LocalDateTime endDate, com.example.kairan.entity.Accounting.Type type, Integer accountingCategoryId, Pageable pageable) {
	    return accountingRepository.findByDistrictIdAndTransactionDateBetweenAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(districtId, startDate, endDate, type, accountingCategoryId, pageable);
	}
	
	// districtId、yearMonth、type、categoryIdに応じて会計データ一覧を取得（CSV用）
	public List<Accounting> findForCsvExport(Integer districtId, YearMonth yearMonth, Accounting.Type type, Integer categoryId) {
	    if (yearMonth != null) {
	        // 年月ありの場合
	        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
	        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

	        if (type != null && categoryId != null) {
	            return accountingRepository.findByDistrictIdAndTransactionDateBetweenAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(
	                    districtId, startDate, endDate, type, categoryId);
	        } else if (type != null) {
	            return accountingRepository.findByDistrictIdAndTransactionDateBetweenAndTypeAndDeletedAtIsNull(
	                    districtId, startDate, endDate, type);
	        } else if (categoryId != null) {
	            return accountingRepository.findByDistrictIdAndTransactionDateBetweenAndAccountingCategoryIdAndDeletedAtIsNull(
	                    districtId, startDate, endDate, categoryId);
	        } else {
	            return accountingRepository.findByDistrictIdAndTransactionDateBetweenAndDeletedAtIsNull(
	                    districtId, startDate, endDate);
	        }
	    } else {
	        // 年月なしの場合
	        if (type != null && categoryId != null) {
	            return accountingRepository.findByDistrictIdAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(
	                    districtId, type, categoryId);
	        } else if (type != null) {
	            return accountingRepository.findByDistrictIdAndTypeAndDeletedAtIsNull(
	                    districtId, type);
	        } else if (categoryId != null) {
	            return accountingRepository.findByDistrictIdAndAccountingCategoryIdAndDeletedAtIsNull(
	                    districtId, categoryId);
	        } else {
	            return accountingRepository.findByDistrictIdAndDeletedAtIsNull(districtId);
	        }
	    }
	}
	
	// 登録
	@Transactional
	public void accountingRegi(User loginUser, AccountingRegiForm form) {
		// ログイン者から抽出
		District district = loginUser.getDistrict();
		// フォームから抽出
		AccountingCategory category = accountingCategoryService.findById(form.getAccountingCategoryId())
			    .orElseThrow(() -> new IllegalArgumentException("指定されたカテゴリが存在しません"));
		// accounting 登録
		Accounting accounting = new Accounting();
		accounting.setTransactionDate(form.getTransactionDate().atStartOfDay());
		accounting.setDistrict(district);
		accounting.setRecordedBy(loginUser);
		accounting.setType(form.getType());
		accounting.setAccountingCategory(category);
		accounting.setAmount(form.getAmount());
		accounting.setDescription(form.getDescription());
		
		accountingRepository.save(accounting);
	}
	
	// 編集
	@Transactional
	public void accountingEdit(User loginUser, AccountingEditForm form) {
		
		// フォームカテゴリチェック
		AccountingCategory category = accountingCategoryService.findById(form.getAccountingCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("指定されたカテゴリが存在しません"));
		
		// フォームのaccountingIdからテーブルデータを抽出
		Accounting accounting = findByAccountId(form.getId())
				.orElseThrow(() -> new IllegalArgumentException(
						"指定された項目が存在しないか、既に削除されています"));
		
		// accounting 更新
		
		accounting.setTransactionDate(form.getTransactionDate().atStartOfDay());
		accounting.setType(form.getType());
		accounting.setAccountingCategory(category);
		accounting.setAmount(form.getAmount());
		accounting.setDescription(form.getDescription());
		accounting.setRecordedBy(loginUser);
		
		accountingRepository.save(accounting);
	}
	
	// ソフトデリート 削除
	public void softDeletedAccounting(User loginUser, int accountingId) {
		
		Accounting accounting = findByAccountId(accountingId)
				.orElseThrow(() -> new IllegalArgumentException("指定したIDが存在しないか既に削除されています"));
		accounting.setRecordedBy(loginUser);
		accounting.softDelete();
		accountingRepository.save(accounting);
	}





}
