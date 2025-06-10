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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import com.example.kairan.controller.DistrictController;
import com.example.kairan.entity.District;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.form.DistrictEditForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.DistrictService;

@WebMvcTest(DistrictController.class)
@AutoConfigureMockMvc
public class DistrictControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private DistrictService districtService;
	
	@MockBean
	private UserDetailsService userDetailsService;
	
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
	
	@Test
	void showAreaList_正常に表示できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		Mockito.when(districtService.permissions(Mockito.any(User.class)))
			.thenReturn(true);
		
		Mockito.when(districtService.sameRegionCodeAssociationArea(Mockito.any(User.class)))
			.thenReturn(Collections.emptyList());
		
		mockMvc.perform(get("/district/area-list")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("district/area-list"))
			.andExpect(model().attributeExists("districtList"))
			.andExpect(model().attributeExists("association"))
			.andExpect(model().attributeExists("form"));
		
	}
	
	@Test
	void districtRegiPage_登録ページを正常に表示できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(get("/district/register-page")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("district/register-page"))
			.andExpect(model().attributeExists("association"))
			.andExpect(model().attributeExists("form"));
	}
	
	@Test
	void districtRegi_正常に登録できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		Mockito.when(districtService.permissions(Mockito.any(User.class)))
			.thenReturn(true);
		
		mockMvc.perform(post("/district/register")
				.with(user(userDetails))
				.with(csrf())
				.param("name", "新しい町名")
				.param("area", "新しい区名")
				.param("description", "新しい説明文")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/district/area-list"));
	}
	
	@Test
	void districtRegi_バリデーションエラーで再表示() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		Mockito.when(districtService.permissions(Mockito.any(User.class)))
			.thenReturn(true);
		
		mockMvc.perform(post("/district/register")
				.with(user(userDetails))
				.with(csrf())
				.param("name", "")
				.param("area", "新しい区名")
				.param("description", "新しい説明文")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().isOk())
			.andExpect(view().name("district/register-page"))
			.andExpect(model().attributeHasFieldErrors("form", "name"));
	}
	
	@Test
	void softDeleteDistrict_正常に削除できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/district/delete/1")
				.with(user(userDetails))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/district/area-list"));
		
		// 削除処理が行われたか
		Mockito.verify(districtService)
			.softDeletedDistrict(Mockito.any(User.class), Mockito.eq(1));
	}
	
	@Test
	void showEditPage_正常に編集ページが表示されるか() throws Exception{
		UserDetailsImpl userDetails = createTestUser();
		
		District districtTest = new District();
		districtTest.setId(1);
		districtTest.setName("町名");
		districtTest.setArea("区名");
		districtTest.setDescription("説明文");
		districtTest.setAssociation("町内会名");
		
		Mockito.when(districtService.findByIdAndDeletedAtIsNull(1))
			.thenReturn(districtTest);
		
		Mockito.when(districtService.permissions(Mockito.any(User.class)))
			.thenReturn(true);
		
		DistrictEditForm form = new DistrictEditForm();
		form.setId(districtTest.getId());
		form.setName(districtTest.getName());
		form.setArea(districtTest.getArea());
		form.setDescription(districtTest.getDescription());
		
		mockMvc.perform(get("/district/edit-page/1")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("district/edit-page"))
			.andExpect(model().attributeExists("association"))
			.andExpect(model().attributeExists("form"));
	}
	
	
	@Test
	void editDistrict_正常に編集できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/district/edit/1")
				.with(user(userDetails))
				.with(csrf())
				.param("name", "新しい町名")
				.param("area", "新しい区名")
				.param("description", "新しい説明文")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/district/area-list"));
			
		// サービスのeditCommittee()が呼ばれているか確認
			Mockito.verify(districtService)
				.editDistrict(Mockito.any(User.class), Mockito.eq(1), Mockito.any(DistrictEditForm.class));
			
	}
	
	@Test
	void editDistrict_編集失敗時はエラーメッセージをセットしてリダイレクト() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		// サービス呼び出し時に例外を投げさせる
		Mockito.doThrow(new IllegalArgumentException("編集できませんでした"))
			.when(districtService)
			.editDistrict(Mockito.any(User.class), Mockito.eq(1), Mockito.any(DistrictEditForm.class));
		
		mockMvc.perform(post("/district/edit/1")
				.with(user(userDetails))
				.with(csrf())
				.param("name", "新しい町名")
				.param("area", "新しい区名")
				.param("description", "新しい説明文")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/district/area-list"));
	}
	
	@Test
	void editDistrict_バリデーションエラー() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(post("/district/edit/1")
				.with(user(userDetails))
				.with(csrf())
				.param("name", "")
				.param("area", "新しい区名")
				.param("description", "新しい説明文")
				.contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().isOk())
			.andExpect(view().name("district/edit-page"))
			.andExpect(model().attributeHasFieldErrors("form", "name")); //nameフィールドにエラー
	}

}
