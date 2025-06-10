package com.example.kairan.controller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.example.kairan.entity.Accounting;
import com.example.kairan.entity.Accounting.Type;
import com.example.kairan.entity.AccountingCategory;
import com.example.kairan.entity.User;
import com.example.kairan.form.AccountingEditForm;
import com.example.kairan.form.AccountingRegiForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.AccountingCategoryService;
import com.example.kairan.service.AccountingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor	
public class AccountingController {
	private final AccountingService accountingService;
	private final AccountingCategoryService accountingCategoryService;
	
	// 会計データ一覧表示
	@GetMapping("/accounting")
	public String list(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@RequestParam(value = "yearMonth", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
			@RequestParam(value = "type", required = false) Type type,
			@RequestParam(value = "categoryId", required = false) Integer categoryId,
			@PageableDefault(size = 10)Pageable pageable,
			Model model
			)
	{
		Integer districtId = userDetails.getUser().getDistrict().getId();
		Page<Accounting> accountingPage;
		BigDecimal sumAmount;
		BigDecimal sumIncome;
	    BigDecimal sumExpense;
		
		if (yearMonth != null) {
			// 指定年月で絞込
			LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
			LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
			
			if(type != null && categoryId != null) {
				// 年月 + 収支区分 + カテゴリ指定
				accountingPage = accountingService.
						findByDistrictAndTransactionDateBetweenAndTypeAndAccountingCategory
						(districtId, startDate, endDate, type, categoryId, pageable);
			} else if(type != null){
				// 年月 + 収支区分だけ指定
				accountingPage = accountingService.findByDistrictAndTransactionDateBetweenAndType(
						districtId, startDate, endDate, type, pageable);
			} else if(categoryId != null) {
				// 年月 + カテゴリだけ指定
	            accountingPage = accountingService.findByDistrictAndTransactionDateBetweenAndAccountingCategory(
	            		districtId, startDate, endDate, categoryId, pageable);
			} else {
				// 年月のみ指定
	            accountingPage = accountingService.findByDistrictAndTransactionDateBetween(
	            		districtId, startDate, endDate, pageable);
			}
			
			sumAmount = accountingService.getSumAmountByDistrictAndPeriod(districtId, startDate, endDate);
			sumIncome = accountingService.getSumIncomeByDistrictAndPeriod(districtId, startDate, endDate);
	        sumExpense = accountingService.getSumExpenseByDistrictAndPeriod(districtId, startDate, endDate);
		} else {
			// 年月無し
			if(type != null && categoryId != null){
				// 収支区分 + カテゴリ指定
				accountingPage = accountingService.findByDistrictAndTypeAndAccountingCategory(
						districtId, type, categoryId, pageable);
			} else if(type != null) {
				// 収支区分だけ指定
	            accountingPage = accountingService.findByDistrictAndType(districtId, type, pageable);
			} else if(categoryId != null) {
				// カテゴリだけ指定
				accountingPage = accountingService.findByDistrictAndAccountingCategory(districtId, categoryId, pageable);
	        } else {
	        	// 全件
	            accountingPage = accountingService.findByDistrict(districtId, pageable);
			}
			
			sumAmount = accountingService.getSumAmountByDistrictAndPeriod(
					districtId, LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now());
			sumIncome = accountingService.getSumIncomeByDistrictAndPeriod(
	                districtId, LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now());
	        sumExpense = accountingService.getSumExpenseByDistrictAndPeriod(
	                districtId, LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now());
					
		}
		
		model.addAttribute("categories", accountingCategoryService.findAll());
		model.addAttribute("accountingPage", accountingPage);
		model.addAttribute("districtId", districtId);
		model.addAttribute("sumAmount", sumAmount);
		model.addAttribute("sumIncome", sumIncome);
	    model.addAttribute("sumExpense", sumExpense);
	    
	    // 検索フォームの検索履歴
	    model.addAttribute("yearMonth", yearMonth);
	    model.addAttribute("type", type);
	    model.addAttribute("categoryId", categoryId);
	    
		
		return "accounting/list";
	}
	
	@GetMapping("/accounting/csv")
	public void exportAccountingCsv(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@RequestParam(value = "yearMonth", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
			@RequestParam(value = "type", required = false) Type type,
			@RequestParam(value = "categoryId", required = false) Integer categoryId,
			HttpServletResponse response
	) throws IOException {
		
		Integer districtId = userDetails.getUser().getDistrict().getId();
		
		// データを取得（今の一覧と同じ絞り込み)
		List<Accounting> accountingList = accountingService.
				findForCsvExport(districtId, yearMonth, type, categoryId);
		
		// CSV出力
		String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String fileName = today + "_accounting.csv";
		response.setContentType("text/csv; charset=MS932");
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
		
		// 書き込み処理
		try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "MS932"))) {
		    // ヘッダー行
		    writer.println("ID,記録者,収支区分,カテゴリ,金額,説明,取引日,作成日,更新日");

		    // データ行
		    for (Accounting accounting : accountingList) {
		        String recordedBy = accounting.getRecordedBy() != null ? accounting.getRecordedBy().getName() : "";
		        String typeStr = accounting.getType() != null ? accounting.getType().name() : "";
		        String categoryStr = accounting.getAccountingCategory() != null ? accounting.getAccountingCategory().getName() : "";
		        String amount = accounting.getAmount() != null ? accounting.getAmount().toString() : "";
		        String description = accounting.getDescription() != null ? accounting.getDescription() : "";
		        String transactionDate = accounting.getTransactionDate() != null ? accounting.getTransactionDate().toString() : "";
		        String createdAt = accounting.getCreatedAt() != null ? accounting.getCreatedAt().toString() : "";
		        String updatedAt = accounting.getUpdatedAt() != null ? accounting.getUpdatedAt().toString() : "";

		        writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s%n",
		            accounting.getId(),
		            recordedBy,
		            typeStr,
		            categoryStr,
		            amount,
		            description,
		            transactionDate,
		            createdAt,
		            updatedAt
		        );
		    }
		}

	}
	
	// 会計項目登録フォームページ
	@GetMapping("/accounting/register-page")
	public String accountingRegiPage(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			Model model)
	{
		AccountingRegiForm form = new AccountingRegiForm();
		User loginUser = userDetails.getUser();
		String association = loginUser.getDistrict().getAssociation();
		List<AccountingCategory> categories = accountingCategoryService.findAll();
		
		model.addAttribute("form", form);
		model.addAttribute("association", association);
		model.addAttribute("categories", categories);
		
		return "accounting/register-page";
	}
	
	// 会計項目登録
	@PostMapping("/accounting/register")
	public String accountingRegi(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes,
			@Valid @ModelAttribute("form") AccountingRegiForm form, BindingResult result,
			Model model) 
	{
		User loginUser = userDetails.getUser();
		
		// バリデーションエラー
		if (result.hasErrors()) {
			System.out.println("バリデーションエラー" + result.getAllErrors());
			List<AccountingCategory> categories = accountingCategoryService.findAll();
			model.addAttribute("categories", categories);
			model.addAttribute("errorMessage", "エラーが発生しました。");
			return "accounting/register-page";
		}
		
		try {
			accountingService.accountingRegi(loginUser, form);
			redirectAttributes.addFlashAttribute("successMessage", "項目を追加しました。");
			return "redirect:/accounting";
		} catch (IllegalArgumentException e) {
			// カテゴリが見つからない場合
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("form", form);
			return "accounting/register-page";
		}
		
	}
	
	// 会計項目詳細ページ
	@GetMapping("/accounting/detail/{id}")
	public String showDetailPage(
			@PathVariable("id") int accountingId,
			Model model)
	{
		Accounting accounting = accountingService.findByAccountId(accountingId)
				.orElseThrow(() -> new IllegalArgumentException("指定したIdが存在しないかデリートされた可能性があります"));
		
		model.addAttribute("accounting", accounting);
		
		return "accounting/detail";
	}
	
	
	// 会計項目を編集ページ
	@GetMapping("/accounting/edit-page/{id}")
	public String showEditPage(
			@PathVariable("id") int accountingId,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			Model model)
	{
		// accountidから取得
		Accounting accounting = new Accounting();
		accounting = accountingService.findByAccountId(accountingId)
						.orElseThrow(() -> new IllegalArgumentException("指定された会計項目が存在しません"));
		//formに入れる
		AccountingEditForm form = new AccountingEditForm();
		form.setId(accounting.getId());
		form.setTransactionDate(accounting.getTransactionDate().toLocalDate());
		form.setType(accounting.getType());
		form.setAccountingCategoryId(accounting.getAccountingCategory().getId());
		form.setAmount(accounting.getAmount());
		form.setDescription(accounting.getDescription());
		
		List<AccountingCategory> categories = accountingCategoryService.findAll();
		
		//formを渡す
		model.addAttribute("form", form);
		model.addAttribute("categories", categories);
		return "accounting/edit-page";
	}
	
	// 会計項目を編集
	@PostMapping("/accounting/edit/{id}")
	public String accountingEdit(
			@PathVariable("id") int accountingId,
			@Valid @ModelAttribute("form") AccountingEditForm form, BindingResult result,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes,
			Model model
			)
	{
		// バリデーションチェック
		if (result.hasErrors()) {
			List<AccountingCategory> categories = accountingCategoryService.findAll();
			model.addAttribute("form", form);
			model.addAttribute("categories", categories);
			return "accounting/edit-page";
		}
		
		try {
			accountingService.accountingEdit(userDetails.getUser(), form);
			redirectAttributes.addFlashAttribute("successMessage", "会計項目を更新しました。");
			return "redirect:/accounting";
			
		} catch(IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/accounting";
		}
	}
	
	// 会計項目を削除
	@PostMapping("/accounting/delete/{id}")
	public String softDeleteAccounting(
			@PathVariable("id") int accountingId,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes)
	{
		try {
			accountingService.softDeletedAccounting(userDetails.getUser(), accountingId);
			redirectAttributes.addFlashAttribute("successMessage", "項目を削除しました。");
		} catch(IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		
		return "redirect:/accounting";
	}
	
	
}
