package com.example.kairan.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import com.example.kairan.entity.District;
import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.repository.MembershipFeeRepository;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.MembershipFeeService;

@WebMvcTest(MembershipFeeController.class)
@AutoConfigureMockMvc
public class MembershipFeeControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private MembershipFeeService membershipFeeService;
	
	@MockBean
	private UserDetailsService userDetailsService;
	
	@MockBean
	private MembershipFeeRepository membershipFeeRepository;
	
	private Page<MembershipFee> membershipFeePage;
	private MembershipFee fee;
	
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
	
	@BeforeEach
	void setUp() {
		fee = new MembershipFee();
		fee.setAmount(new BigDecimal("5000"));
		fee.setYear(2025);
		
		List<MembershipFee> dummyList = List.of(fee);
		membershipFeePage = new PageImpl<>(dummyList);
	}
	
// 登録
	@Test
	void registerPage_正常に表示ができる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(membershipFeeService.getMembershipFeeList(any(User.class), any(Pageable.class)))
			.thenReturn(membershipFeePage);
		
		mockMvc.perform(get("/membership-fee/register-page")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("membership-fee/register-page"))
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("currentYear"))
			.andExpect(model().attributeExists("membershipFeePage"));
	}
	
	@Test
	void registerMembershipFee_正常に登録できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/membership-fee/register")
				.with(user(userDetails))
				.with(csrf())
				.param("year", "2025")
				.param("amount", "5000"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/membership-fee/register-page"))
		    .andExpect(flash().attributeExists("successMessage"));
		
		// サービスが呼びだされているか検証
		verify(membershipFeeService, times(1)).regiMembershipFee(any(), eq(userDetails.getUser()));
	}
	
	@Test
	void registerMembershipFee_yearが空の時に例外をスロー() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(membershipFeeService.getMembershipFeeList(any(User.class), any(Pageable.class)))
		.thenReturn(membershipFeePage);
		
		mockMvc.perform(post("/membership-fee/register")
				.with(user(userDetails))
				.with(csrf())
				.param("year", "")
				.param("amount", "5000"))
			.andExpect(status().isOk())
			.andExpect(view().name("membership-fee/register-page"))
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("membershipFeePage"));
		
		// サービスが読みだされていないか検証
		verify(membershipFeeService, never()).regiMembershipFee(any(), any());
	}
	
	@Test
	void registerMembershipFee_重複する年の会費を設定した場合例外をスロー() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(membershipFeeService.sameDistrictIdAndYear(anyInt(),anyInt()))
			.thenReturn(Optional.of(new MembershipFee()));
		
		mockMvc.perform(post("/membership-fee/register")
				.with(user(userDetails))
				.with(csrf())
				.param("year", "2025")
				.param("amount", "5000"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/membership-fee/register-page"))
			    .andExpect(flash().attributeExists("errorMessage"))
				.andExpect(flash().attributeExists("form"));
		
		// サービスが呼びだされていないか検証
		verify(membershipFeeService, never()).regiMembershipFee(any(), any());
	}
	
// 編集ページ
	@Test
	void editMembershipFeePage_正常に表示される() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(membershipFeeService.findByDistrictAndDeletedAtIsNull(1))
			.thenReturn(Optional.of(fee));
		
		mockMvc.perform(get("/membership-fee/edit-page/1")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("membership-fee/edit-page"))
			.andExpect(model().attributeExists("form"));
	}
	
	@Test
	void editMembershipFeePage_指定したIDが見つからない場合例外をスロー() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(membershipFeeService.findByDistrictAndDeletedAtIsNull(1))
			.thenReturn(Optional.empty());
		
		mockMvc.perform(get("/membershi-fee/edit-page/1")
				.with(user(userDetails)))
			.andExpect(status().is4xxClientError());
	}
	
// 編集機能
	@Test
	void editMembershipFee_正常に更新できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/membership-fee/edit")
				.with(user(userDetails))
				.with(csrf())
				.param("year", "2025")
				.param("amount", "5000"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/membership-fee/register-page"))
		    .andExpect(flash().attributeExists("successMessage"));
		
		// サービスが呼びだされているか検証
		verify(membershipFeeService, times(1)).editMembershipFee(any(), eq(userDetails.getUser()));
	}
	
	@Test
	void editMembershipFee_バリデーションエラーで再表示() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/membership-fee/edit")
				.with(user(userDetails))
				.with(csrf())
				.param("year", "")
				.param("amount", "5000"))
			.andExpect(status().isOk())
			.andExpect(view().name("membership-fee/edit-page"))
			.andExpect(model().attributeExists("errorMessage"))
			.andExpect(model().attributeExists("form"));
		
		// サービスが呼び出されていないことを確認
		verify(membershipFeeService, never()).editMembershipFee(any(), any());
	}
	
	@Test
	void editMembershipFee_編集対象以外のデータを上書きしようとする場合は例外をスロー() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		// 重複対象の年
		MembershipFee existingFee = new MembershipFee();
		existingFee.setId(999);
		
		when(membershipFeeService.sameDistrictIdAndYear(
				eq(userDetails.getUser().getDistrict().getId()), eq(2025)))
			.thenReturn(Optional.of(existingFee));
		
		mockMvc.perform(post("/membership-fee/edit")
				.with(user(userDetails))
				.with(csrf())
				.param("id", "1")
				.param("year", "2025")
				.param("amount", "5000"))
			.andExpect(status().isOk())
			.andExpect(view().name("membership-fee/edit-page"))
			.andExpect(model().attributeExists("errorMessage"))
			.andExpect(model().attributeExists("form"));
		
		// サービスが呼び出されていないことを確認
		verify(membershipFeeService, never()).editMembershipFee(any(), any());
	}
	
	@Test
	void editMembershipFee_サービスが例外をスローした場合はエラーメッセ時をリダイレクトで表示() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(membershipFeeService.sameDistrictIdAndYear(
				eq(userDetails.getUser().getDistrict().getId()), eq(2025)))
			.thenReturn(Optional.empty());
		
		// サービス側が例外をスロー
		doThrow(new IllegalArgumentException("指定されたIDが存在しないか既に削除されています"))
			.when(membershipFeeService).editMembershipFee(any(), eq(userDetails.getUser()));
		
		mockMvc.perform(post("/membership-fee/edit")
				.with(user(userDetails))
				.with(csrf())
				.param("id", "1")
				.param("year", "2025")
				.param("amount", "5000"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/membership-fee/register-page"))
			.andExpect(flash().attributeExists("errorMessage"));
	}
// 削除機能
	@Test
	void softDeletedMembershipFee_正常に削除できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/membership-fee/delete/1")
				.with(user(userDetails))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/membership-fee/register-page"))
			.andExpect(flash().attributeExists("successMessage"));
		
		verify(membershipFeeService, times(1)).softDeletedMembershipFee(1);
	}
	
	@Test
	void softDeletedMembershipFee_サービスが例外をスローした場合はエラーメッセージをリダイレクトで表示
		() throws Exception 
	{
		UserDetailsImpl userDetails = createTestUser();
		
		// サービス側が例外をスロー
		doThrow(new IllegalArgumentException("指定されたIDが存在しないか既に削除されています"))
			.when(membershipFeeService).softDeletedMembershipFee(1);
		
		
		mockMvc.perform(post("/membership-fee/delete/1")
				.with(user(userDetails))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/membership-fee/register-page"))
			.andExpect(flash().attributeExists("errorMessage"));

		verify(membershipFeeService, times(1)).softDeletedMembershipFee(1);
	}
}
