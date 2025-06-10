package com.example.kairan.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.kairan.entity.Circular;
import com.example.kairan.entity.CircularFile;
import com.example.kairan.entity.District;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.form.CircularEditForm;
import com.example.kairan.form.CircularForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.CircularFileService;
import com.example.kairan.service.CircularReadService;
import com.example.kairan.service.CircularService;

@WebMvcTest(CircularController.class)
public class CircularControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CircularService circularService;

	@MockBean
	private CircularReadService circularReadService;
	
	@MockBean
	private CircularFileService circularFileService;


	private UserDetailsImpl testUser;


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

// 一般閲覧
	// 一覧
	@Test
	void circular_正常に表示される() throws Exception {
		UserDetailsImpl userDetails = createTestUser();

		when(circularService.getLatestCirculars(any(), any()))
			.thenReturn(Page.empty());

		when(circularReadService.readStatusCheck(any(), any()))
			.thenReturn(true);

		mockMvc.perform(get("/circular").with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("circular/index"))
			.andExpect(model().attributeExists("circularPage"))
			.andExpect(model().attributeExists("readMap"));
	}
	
	// 詳細
	@Test
	void circularDetail_正常に表示される() throws Exception {
	    UserDetailsImpl userDetails = createTestUser();

	    // ダミー Circular
	    Circular dummyCircular = new Circular();
	    dummyCircular.setId(1);
	    dummyCircular.setName("回覧板テスト");

	    // CircularFileのダミーリスト
	    CircularFile dummyFile = new CircularFile();
	    dummyFile.setId(1);
	    dummyFile.setFileName("添付資料");
	    dummyFile.setFilePath("dummy/path");
	    List<CircularFile> fileList = List.of(dummyFile);
	    
	    User author = new User();
	    author.setName("投稿者ユーザー");
	    dummyCircular.setAuthor(author);

	    when(circularService.getCircularById(1)).thenReturn(Optional.of(dummyCircular));
	    when(circularFileService.getfindByCircularId(dummyCircular)).thenReturn(fileList);
	    when(circularReadService.readStatusCheck(any(), eq(dummyCircular))).thenReturn(true);

	    mockMvc.perform(get("/circular/detail/1")
	            .with(user(userDetails)))
	        .andExpect(status().isOk())
	        .andExpect(view().name("circular/detail"))
	        .andExpect(model().attributeExists("circular"))
	        .andExpect(model().attributeExists("circularFiles"));
	}
	
	@Test
	void circularDetail_指定したIDが存在しない場合はエラーメッセージを表示() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		when(circularService.getCircularById(anyInt())).thenReturn(Optional.empty());
		mockMvc.perform(get("/circular/detail/1")
	            .with(user(userDetails)))
	        .andExpect(status().isOk())
	        .andExpect(view().name("circular/detail"))
	        .andExpect(model().attributeExists("errorMessage"))
	        .andExpect(model().attributeDoesNotExist("circular","circularFiles"));
	}
	
	@Test
	void circularDetail_未読の場合は既読処理が行われる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		Circular circular = new Circular();
	    circular.setId(1);
	    circular.setAuthor(userDetails.getUser());
	    
	    when(circularService.getCircularById(1)).thenReturn(Optional.of(circular));
	    when(circularFileService.getfindByCircularId(circular)).thenReturn(List.of());
	    when(circularReadService
	    		.readStatusCheck(userDetails.getUser(), circular)).thenReturn(true);
	    
	    mockMvc.perform(get("/circular/detail/1")
	    		.with(user(userDetails)))
	    	.andExpect(status().isOk())
	    	.andExpect(view().name("circular/detail"))
	    	.andExpect(model().attributeExists("circular", "circularFiles"))
	        .andExpect(model().attributeDoesNotExist("readStatus"));
	    
	    verify(circularReadService, times(1)).markAsRead(userDetails.getUser(),circular);
	}
	
// 管理
	// 管理ページ
	@Test
	void circularControlPage_正常に表示される() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		Page<Circular> mockPage = new PageImpl<>(List.of());

		when(circularService.getLatestCirculars(any(), any()))
			.thenReturn(mockPage);

		mockMvc.perform(get("/circular/control-page")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("circular/control-page"))
			.andExpect(model().attributeExists("circularPage"))
			.andExpect(model().attributeExists("association"));
	}

	// 登録ページ
	@Test
	void circularRegiPage_正常に表示される() throws Exception {
		UserDetailsImpl userDetails = createTestUser();

		mockMvc.perform(get("/circular/control/register-page")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("circular/register-page"))
			.andExpect(model().attributeExists("association"))
			.andExpect(model().attributeExists("form"));
	}

	// 登録機能
	@Test
	void circularRegi_正常に回覧板が登録できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		Page<Circular> mockPage = new PageImpl<>(List.of());
		
		Circular dummyCircular = new Circular();
	    dummyCircular.setId(1);
	    
	    when(circularService.createCircular(any(), any()))
	    						.thenReturn(dummyCircular);
	    when(circularService.getLatestCirculars(any(), any()))
								.thenReturn(mockPage);
	    
	    mockMvc.perform(multipart("/circular/control/register")
				.with(user(userDetails))
	    		.with(csrf())
	    		.param("name", "テスト回覧板")
	    		.param("description", "説明文です")
	    		.param("isUrgent", "false"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/circular/control-page"))
			.andExpect(flash().attributeExists("successMessage"))
			.andExpect(flash().attributeExists("circularPage"));
	    
	    verify(circularService, times(1)).createCircular(any(), eq(userDetails.getUser()));
	}
	
	@Test
	void circularRegi_バリデーションエラーが発生した場合は再表示() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		mockMvc.perform(multipart("/circular/control/register")
				.with(user(userDetails))
	    		.with(csrf())
	    		.param("name", "")
	    		.param("description", "説明文です")
	    		.param("isUrgent", "false"))
			.andExpect(status().isOk())
			.andExpect(view().name("circular/register-page"))
			.andExpect(model().attributeExists("form"));
	}
	
	@Test
	void circularRegi_アップロード失敗の場合はエラー表示() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		
		when(circularService.createCircular(any(CircularForm.class), any(User.class)))
	    .thenThrow(new IOException("ファイルのアップロードに不具合が発生しました"));

		
		mockMvc.perform(multipart("/circular/control/register")
				.with(user(userDetails))
	    		.with(csrf())
	    		.param("name", "テスト回覧板")
	    		.param("description", "説明文です")
	    		.param("isUrgent", "false"))
			.andExpect(status().isOk())
			.andExpect(view().name("circular/register-page"))
			.andExpect(model().attributeExists("errorMessage"))
			.andExpect(model().attributeExists("form"));
	}
	
	// 編集
	@Test
	void circularEditPage_正常に編集ページが表示される() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		Circular circular = new Circular();
	    circular.setId(1);
	    circular.setName("タイトル");
	    circular.setDescription("説明");
	    circular.setIsUrgent(false);
	    circular.setAuthor(userDetails.getUser());
	    
	    when(circularService.getCircularById(1)).thenReturn(Optional.of(circular));

		mockMvc.perform(get("/circular/control/edit-page/1")
				.with(user(userDetails)))
			.andExpect(status().isOk())
			.andExpect(view().name("circular/edit-page"))
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attribute("circularId", 1));
	}
	
	@Test
	void circularEditPage_指定したIDが存在しない場合はエラーメッセージを表示() throws Exception {
	    UserDetailsImpl userDetails = createTestUser();

	    when(circularService.getCircularById(1)).thenReturn(Optional.empty());

	    mockMvc.perform(get("/circular/control/edit-page/1")
	            .with(user(userDetails)))
	        .andExpect(status().isOk())
	        .andExpect(view().name("circular/edit-page"))
	        .andExpect(model().attributeExists("errorMessage"));
	}
	
	// 編集機能
	@Test
	void circularEdit_正常に回覧板が更新できる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		Page<Circular> mockPage = new PageImpl<>(List.of());
		
		Circular dummyCircular = new Circular();
	    dummyCircular.setId(1);
	    
	    doNothing().when(circularService)
	    	.editCircularById(eq(1), any(CircularEditForm.class),
	    			eq(userDetails.getUser()));
	    when(circularService.getLatestCirculars(any(), any()))
								.thenReturn(mockPage);
	    
	    mockMvc.perform(multipart("/circular/control/edit/1")
				.with(user(userDetails))
	    		.with(csrf())
	    		.param("name", "テスト回覧板")
	    		.param("description", "説明文です")
	    		.param("isUrgent", "true"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/circular/control-page"))
			.andExpect(flash().attributeExists("successMessage"))
			.andExpect(flash().attributeExists("circularPage"));
	    
	    verify(circularService,
	    		times(1)).editCircularById(eq(1),
	    		any(CircularEditForm.class),
	    		eq(userDetails.getUser())
	    );
	}
	
	@Test
	void circularEdit_バリデーションエラーが発生した場合は再表示() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		
		
		mockMvc.perform(multipart("/circular/control/edit/1")
				.with(user(userDetails))
	    		.with(csrf())
	    		.param("name", "")
	    		.param("description", "説明文です")
	    		.param("isUrgent", "true"))
			.andExpect(status().isOk())
			.andExpect(view().name("circular/edit-page"))
			.andExpect(model().attributeExists("form"));
		
		verify(circularService, never()).editCircularById(anyInt(), any(CircularEditForm.class), any(User.class));
	}
	
	@Test
	void circularEdit_指定したIDが存在しない場合はエラーメッセージを表示() throws Exception {
	    UserDetailsImpl userDetails = createTestUser();
	    Page<Circular> mockPage = new PageImpl<>(List.of());

	    doThrow(new IllegalArgumentException("指定したIDが存在しないか削除されています。"))
	    .when(circularService)
	    .editCircularById(eq(1), any(CircularEditForm.class), eq(userDetails.getUser()));

	    when(circularService.getLatestCirculars(any(), any()))
			.thenReturn(mockPage);

	    mockMvc.perform(multipart("/circular/control/edit/1")
	            .with(user(userDetails))
			    .with(csrf())
				.param("name", "テスト回覧板")
				.param("description", "説明文です")
				.param("isUrgent", "true"))
		    .andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/circular/control-page"))
			.andExpect(flash().attributeExists("errorMessage"))
			.andExpect(flash().attributeExists("circularPage"));
	    
	    verify(circularService, times(1))
       		.editCircularById(eq(1), any(CircularEditForm.class), eq(userDetails.getUser()));

	}
	
	@Test
	void circularEdit_ファイルの保存に失敗した場合エラーメッセージを表示() throws Exception {
	    UserDetailsImpl userDetails = createTestUser();
	    Page<Circular> mockPage = new PageImpl<>(List.of());

	    doThrow(new RuntimeException("ファイル保存に失敗しました"))
	    	.when(circularService)
	    	.editCircularById(eq(1), any(CircularEditForm.class), eq(userDetails.getUser()));

	    when(circularService.getLatestCirculars(any(), any()))
			.thenReturn(mockPage);

	    mockMvc.perform(multipart("/circular/control/edit/1")
	            .with(user(userDetails))
			    .with(csrf())
				.param("name", "テスト回覧板")
				.param("description", "説明文です")
				.param("isUrgent", "true"))
		    .andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/circular/control-page"))
			.andExpect(flash().attributeExists("errorMessage"))
			.andExpect(flash().attributeExists("circularPage"));
	    
	    verify(circularService, times(1))
       		.editCircularById(eq(1), any(CircularEditForm.class), eq(userDetails.getUser()));

	}
	
	// 削除機能
	@Test
	void circularDelete_正常に回覧板がソフトデリートできる() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		Page<Circular> mockPage = new PageImpl<>(List.of());
		
		Circular dummyCircular = new Circular();
	    dummyCircular.setId(1);
	    dummyCircular.setAuthor(userDetails.getUser());
	    
	    when(circularService.getCircularById(1))
	    	.thenReturn(Optional.of(dummyCircular));
		when(circularService
			.getLatestCirculars(any(), any())).thenReturn(mockPage);
	    
	    mockMvc.perform(post("/circular/control/delete/1")
				.with(user(userDetails))
	    		.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/circular/control-page"))
			.andExpect(flash().attributeExists("successMessage"))
			.andExpect(flash().attributeExists("circularPage"));
	    
	    verify(circularFileService,times(1)).softDeletedCircularFile(dummyCircular);
	    verify(circularService,times(1)).softDeletedCircular(dummyCircular);
	}
	
	@Test
	void circularDelete_指定したIDが存在しない場合はエラーメッセージを表示() throws Exception {
		UserDetailsImpl userDetails = createTestUser();
		Page<Circular> mockPage = new PageImpl<>(List.of());
		
		when(circularService.getCircularById(1))
        	.thenReturn(Optional.empty()); 

		when(circularService.getLatestCirculars(any(), any()))
        	.thenReturn(mockPage); 
	    
	    mockMvc.perform(post("/circular/control/delete/1")
				.with(user(userDetails))
	    		.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/circular/control-page"))
			.andExpect(flash().attributeExists("errorMessage"));
	}

}
