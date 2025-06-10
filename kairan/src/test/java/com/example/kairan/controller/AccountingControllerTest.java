package com.example.kairan.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.kairan.controller.AccountingController;
import com.example.kairan.entity.Accounting;
import com.example.kairan.entity.AccountingCategory;
import com.example.kairan.entity.District;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.form.AccountingEditForm;
import com.example.kairan.form.AccountingRegiForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.AccountingCategoryService;
import com.example.kairan.service.AccountingService;

@WebMvcTest(AccountingController.class)
@AutoConfigureMockMvc
public class AccountingControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private AccountingService accountingService;
	
	@MockBean
	private AccountingCategoryService accountingCategoryService;
	
	private Accounting accounting;
	private AccountingCategory category;
	
	
	private UserDetailsImpl createTestUser() {
		User loginUser = new User();
		loginUser.setId(1);
		loginUser.setName("テストユーザー");
		loginUser.setUserId("testuser");
		loginUser.setPassword("password");
		loginUser.setEnabled(true);
		
		District district = new District();
		district.setId(1);
		district.setRegionCode("RC001");
		district.setAssociation("A001");
		loginUser.setDistrict(district);
		
		Role role = new Role();
		role.setName("ROLE_町内会長");
		loginUser.setRole(role);
		
		List<GrantedAuthority>	authorities =
				List.of(new SimpleGrantedAuthority(role.getName()));
		return new UserDetailsImpl(loginUser, authorities);
	}
	
	private AccountingEditForm createEditForm() {
		AccountingEditForm form = new AccountingEditForm();
		form.setTransactionDate(LocalDate.of(2024, 4, 1));
		form.setType(Accounting.Type.収入);
		form.setAccountingCategoryId(1);
		form.setAmount(new BigDecimal(5000));
		form.setDescription("説明文");
		return form;
	}
	
	@BeforeEach
	void setUp() {
	    User loginUser = createTestUser().getUser();

	    accounting = new Accounting();
	    accounting.setId(1);
	    accounting.setDistrict(loginUser.getDistrict());
	    accounting.setTransactionDate(LocalDate.of(2024, 4, 1).atStartOfDay());
	    accounting.setAmount(new BigDecimal("5000"));
	    accounting.setDescription("説明文");
	    accounting.setType(Accounting.Type.収入);


	    category = new AccountingCategory();
	    category.setId(1);
	    category.setName("テストカテゴリ");
	    accounting.setAccountingCategory(category);

	    accounting.setRecordedBy(loginUser);
	}
	
	
// "/accounting"	
	@Test
	void list_正常に表示できる() throws Exception{
		UserDetailsImpl userDetails = createTestUser();
		
		when(accountingCategoryService.findAll()).thenReturn(List.of()); // 空リストでもOK
		when(accountingService.findByDistrict(anyInt(), any())).thenReturn(Page.empty());
		when(accountingService.getSumAmountByDistrictAndPeriod(anyInt(), any(), any())).thenReturn(BigDecimal.ZERO);
		when(accountingService.getSumIncomeByDistrictAndPeriod(anyInt(), any(), any())).thenReturn(BigDecimal.ZERO);
		when(accountingService.getSumExpenseByDistrictAndPeriod(anyInt(), any(), any())).thenReturn(BigDecimal.ZERO);

		
		mockMvc.perform(get("/accounting")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("accounting/list"))
			.andExpect(model().attributeExists("categories"))
			.andExpect(model().attributeExists("accountingPage"))
			.andExpect(model().attributeExists("districtId"))
			.andExpect(model().attributeExists("sumAmount"))
			.andExpect(model().attributeExists("sumIncome"))
			.andExpect(model().attributeExists("sumExpense"));
	}
	
// CSV	
	@Test
	void exportAccountingCsv_正常にCSV出力できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(accountingService.findForCsvExport(anyInt(), any(), any(), any()))
			.thenReturn(List.of());
		
		mockMvc.perform(get("/accounting/csv")
					.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(content().contentType("text/csv; charset=MS932"))
			.andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("accounting.csv")));
	}
	
// 登録ページ
	@Test
	void accountingRegiPage_正常に登録ページが表示できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(accountingCategoryService.findAll())
			.thenReturn(List.of());
		
		mockMvc.perform(get("/accounting/register-page")
					.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("accounting/register-page"))
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("association"))
			.andExpect(model().attributeExists("categories"));
	}
	
// 登録機能
	@Test
	void accountingRegi_正常にデータが登録できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/accounting/register")
					.with(user(userDetails))
					.param("transactionDate", "2024-04-01")
					.param("type", "収入")
					.param("accountingCategoryId", "1")
					.param("amount", "1000")
					.param("description", "テスト説明")
					.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/accounting"))
			.andExpect(flash().attributeExists("successMessage"));
	}
	
	@Test
	void accountingRegi_バリデーションエラーで再表示() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/accounting/register")
				.with(user(userDetails))
				.with(csrf())
				.param("transactionDate", "2024-04-01")
				.param("type", "")
				.param("accountingCategoryId", "1")
				.param("amount", "1000")
				.param("description", "テスト説明")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().isOk())
			.andExpect(view().name("accounting/register-page"))
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("categories"))
			.andExpect(model().attributeExists("errorMessage"));
	}
	
	@Test
	void accountingRegi_カテゴリが見つからない場合例外をスロー() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		Mockito.doThrow(new IllegalArgumentException("指定されたカテゴリが存在しません"))
			.when(accountingService)
			.accountingRegi(any(User.class), any(AccountingRegiForm.class));
		
		mockMvc.perform(post("/accounting/register")
				.with(user(userDetails))
				.with(csrf())
				.param("transactionDate", "2024-04-01")
				.param("type", "収入")
				.param("accountingCategoryId", "1")
				.param("amount", "1000")
				.param("description", "テスト説明")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().isOk())
			.andExpect(view().name("accounting/register-page"))
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("errorMessage"));
	}
// 詳細ページ
	@Test
	void showDetailPage_正常に詳細ページが表示できる() throws Exception{
		UserDetailsImpl userDetails = createTestUser();
		accounting.setId(1);
		
		when(accountingService.findByAccountId(1))
			.thenReturn(Optional.of(accounting));
		
		mockMvc.perform(get("/accounting/detail/1")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("accounting/detail"))
			.andExpect(model().attributeExists("accounting"));
	}
	
	@Test
	void showDetailPage_IDが存在しない場合例外をスロー() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(accountingService.findByAccountId(any()))
			.thenReturn(Optional.empty());
		
		assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/accounting/detail/1")
					.with(user(userDetails)));
		});
	}

// 編集
	// 編集ページ
	@Test
	void showEditPage_正常に表示される() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(accountingService.findByAccountId(1))
			.thenReturn(Optional.of(accounting));
		
		mockMvc.perform(get("/accounting/edit-page/1")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("accounting/edit-page"))
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("categories"));
		
	}
	
	@Test
	void showEditPage_指定されたIDが見つからなければエラーページを返す() throws Exception {
	    UserDetailsImpl userDetails = createTestUser();
	    
	    when(accountingService.findByAccountId(any()))
	        .thenReturn(Optional.empty());

	    assertThrows(ServletException.class, () -> {
	        mockMvc.perform(get("/accounting/edit-page/1")
	                .with(user(userDetails)));
	    });
	}
	
// 編集機能
	@Test
	void accountingEdit_正常に編集が行える() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/accounting/edit/1")
					.with(user(userDetails))
					.param("transactionDate", "2024-04-01")
					.param("type", "収入")
					.param("accountingCategoryId", "1")
					.param("amount", "1000")
					.param("description", "テスト編集")
					.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
					.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/accounting"))
			.andExpect(flash().attributeExists("successMessage"));
	}
	
	@Test
	void accountingEdit_バリデーションエラーで再表示() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		
		mockMvc.perform(post("/accounting/edit/1")
				.with(user(userDetails))
				.with(csrf())
				.param("transactionDate", "2024-04-01")
				.param("type", "")
				.param("accountingCategoryId", "1")
				.param("amount", "1000")
				.param("description", "テスト説明")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().isOk())
			.andExpect(view().name("accounting/edit-page"))
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("categories"));
	}
	
	@Test
	void accountingEdit_指定したIDが見つからない場合例外をスロー() throws Exception {
	    UserDetailsImpl userDetails = createTestUser();
	    
 		Mockito.doThrow(new IllegalArgumentException("指定された項目が存在しないか、既に削除されています"))
 			.when(accountingService)
 			.accountingEdit(any(User.class),any(AccountingEditForm.class));
	    
 		mockMvc.perform(post("/accounting/edit/1")
				.with(user(userDetails))
				.with(csrf())
				.param("transactionDate", "2024-04-01")
				.param("type", "収入")
				.param("accountingCategoryId", "1")
				.param("amount", "1000")
				.param("description", "テスト説明")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
 		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/accounting"))
		.andExpect(flash().attributeExists("errorMessage"));
	}
	
	@Test
	void softDeleteAccounting_正常に会計項目が削除できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/accounting/delete/1")
				.with(user(userDetails))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/accounting"))
			.andExpect(flash().attributeExists("successMessage"));
			
			verify(accountingService)
				.softDeletedAccounting(any(User.class),eq(1));
		
	}
	
	@Test
	void softDeletedAccounting_指定したIDが存在しない場合は例外をスロー() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		
		doThrow(new IllegalArgumentException("指定された項目が存在しないか、既に削除されています"))
			.when(accountingService)
			.softDeletedAccounting(any(User.class),eq(1));
		
		mockMvc.perform(post("/accounting/delete/1")
				.with(user(userDetails))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/accounting"))
			.andExpect(flash().attributeExists("errorMessage"));
	}

}
