package com.example.kairan.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import com.example.kairan.controller.CommitteeClassificationController;
import com.example.kairan.entity.District;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.form.CommitteeRegiForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.CommitteeClassificationService;

@WebMvcTest(CommitteeClassificationController.class)
@AutoConfigureMockMvc
public class CommitteeClassificationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommitteeClassificationService committeeClassificationService;

	@MockBean
	private UserDetailsService userDetailsService;

	private UserDetailsImpl createTestUser() {
		User user = new User();
		user.setId(1);
		user.setName("テストユーザー");
		user.setUserId("testuser");
		user.setPassword("password");
		user.setEnabled(true);

		District district = new District();
		district.setId(1);
		district.setRegionCode("RC001");
		district.setAssociation("A001");
		user.setDistrict(district);

		Role role = new Role();
		role.setName("ROLE_委員長");
		user.setRole(role);

		List<GrantedAuthority> authorities = 
				List.of(new SimpleGrantedAuthority(role.getName()));

		return new UserDetailsImpl(user, authorities);
	}

	@Test
	void showCommitteeList_正常に表示できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();

		Mockito.when(committeeClassificationService.permissions(Mockito.any(User.class)))
		.thenReturn(true);

		Mockito.when(committeeClassificationService.getCommitteeSameDistrictIdAssosiationAsk(Mockito.anyInt(), Mockito.anyString()))
		.thenReturn(Collections.emptyList());

		mockMvc.perform(get("/committee/list")
				.with(user(userDetails)))
		.andExpect(status().isOk())
		.andExpect(view().name("committee/list"))
		.andExpect(model().attributeExists("committeeList"))
		.andExpect(model().attributeExists("committeeRegiForm"))
		.andExpect(model().attributeExists("association"));

	}

	@Test
	void committeeRegister_正常に登録できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();

		Mockito.when(committeeClassificationService.permissions(Mockito.any(User.class)))
		.thenReturn(true);

		mockMvc.perform(post("/committee/register")
				.with(user(userDetails))
				.with(csrf()) 
				.param("name", "新しい委員カテゴリ")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
		.andExpect(status().is3xxRedirection()) // リダイレクトステータス
		.andExpect(redirectedUrl("/committee/list")); // リダイレクト先

	}

	@Test
	void committeeRegister_バリデーションエラーで再表示される() throws Exception {
		UserDetailsImpl userDetails = createTestUser();

		Mockito.when(committeeClassificationService.permissions(Mockito.any(User.class)))
		.thenReturn(true);

		Mockito.when(committeeClassificationService.getCommitteeSameDistrictIdAssosiationAsk(Mockito. anyInt(), Mockito.anyString()))
		.thenReturn(Collections.emptyList()); // 再表示時にリスト渡すため必要

		mockMvc.perform(post("/committee/register")
				.with(user(userDetails))
				.with(csrf())
				.param("name", "") // 空欄送信
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
		.andExpect(status().isOk()) // エラー時は再表示なので 200 OK
		.andExpect(view().name("committee/list")) // 再びlist画面
		.andExpect(model().attributeHasFieldErrors("committeeRegiForm", "name")); // nameフィールドにエラーがあるか

	}

	@Test
	void softDeleteCommittee_正常に削除できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();

		mockMvc.perform(post("/committee/delete/1")
				.with(user(userDetails))
				.with(csrf()))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/committee/list"));

		// 削除処理(service呼び出し）が行われたか
		Mockito.verify(committeeClassificationService)
		.softDeletedCommittee(Mockito.any(User.class), Mockito.eq(1));
	}

	@Test
	void softDeleteCommittee_削除失敗時はエラーメッセージをセットしてリダイレクト() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		// サービス呼び出し時に例外を投げさせる
		Mockito.doThrow(new IllegalArgumentException("削除できませんでした"))
			.when(committeeClassificationService)
			.softDeletedCommittee(Mockito.any(User.class), Mockito.eq(1));
		
		mockMvc.perform(post("/committee/delete/1")
				.with(user(userDetails))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/committee/list"));
	}
	
	@Test
	void editCommittee_正常に編集できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/committee/edit/1")
				.with(user(userDetails))
				.with(csrf())
				.param("name", "新しい委員名") // 編集後の名前をセット
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/committee/list"));
		
		// サービスのeditCommittee()が呼ばれているか確認
		Mockito.verify(committeeClassificationService)
			.editCommittee(Mockito.any(User.class), Mockito.eq(1), Mockito.any(CommitteeRegiForm.class));
	}
	
	@Test
	void editCommittee_編集失敗時はエラーメッセージをセットしてリダイレクトされる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		// サービス呼び出し時に例外を投げさせる
		Mockito.doThrow(new IllegalArgumentException("編集できませんでした"))
			.when(committeeClassificationService)
			.editCommittee(Mockito.any(User.class), Mockito.eq(1), Mockito.any(CommitteeRegiForm.class));
		
		mockMvc.perform(post("/committee/edit/1")
				.with(user(userDetails))
				.with(csrf())
				.param("name", "新しい委員名")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/committee/list"));
	}
}
