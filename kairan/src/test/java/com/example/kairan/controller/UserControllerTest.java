package com.example.kairan.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.kairan.entity.CommitteeClassification;
import com.example.kairan.entity.District;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.form.UserEditForm;
import com.example.kairan.form.UserRegiForm;
import com.example.kairan.repository.CommitteeClassificationRepository;
import com.example.kairan.repository.DistrictRepository;
import com.example.kairan.repository.EmailVerificationTokenRepository;
import com.example.kairan.repository.RoleRepository;
import com.example.kairan.repository.UserRepository;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.UserService;

@AutoConfigureMockMvc
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @MockBean
    private CommitteeClassificationRepository committeeClassificationRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DistrictRepository districtRepository;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setup() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserId("testuser"); 
        District district = new District();
        district.setId(1);
        loginUser.setDistrict(district);
        
        Role role = new Role();
        role.setName("ROLE_区長");
        loginUser.setRole(role); 

        userDetails = new UserDetailsImpl(loginUser, List.of(new SimpleGrantedAuthority("ROLE_区長")));
    }


    @Test
    void testShowRegisterForm_正常に登録画面を表示() throws Exception {
        mockMvc.perform(get("/auth/register")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/register"))
            .andExpect(model().attributeExists("userRegiForm"));
    }

    @Test
    void testRegisterUser_正常登録でリダイレクト() throws Exception {
        UserRegiForm form = new UserRegiForm();
        form.setEmail("test@example.com");
        form.setUserId("testuser"); // 新規登録する会員
        form.setPassword("password");
        form.setConfirmPassword("password");
        form.setName("テスト太郎");
        form.setFurigana("テストタロウ");
        form.setPhoneNumber("08012345678");
        form.setPostalCode("1234567");
        form.setAddress("名古屋市中区");

        User loginUser = new User();
        loginUser.setId(1);
        District district = new District();
        district.setId(1);
        loginUser.setDistrict(district);

        when(userService.passwordDoubleCheck(anyString(), anyString())).thenReturn(true);
        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userService.findByUserId(eq("testuser")))
        .thenReturn(null) // 1回目 (重複チェック)
        .thenReturn(loginUser); // 2回目 (ログインユーザ取得)
        when(roleRepository.findByName("ROLE_会員")).thenReturn(Optional.of(new Role()));
        doNothing().when(userService).saveUser(any(User.class), anyInt());

        mockMvc.perform(post("/register")
                .param("email", "test@example.com")
                .param("userId", "testuser")
                .param("password", "password")
                .param("confirmPassword", "password")
                .param("name", "テスト太郎")
                .param("furigana", "テストタロウ")
                .param("phoneNumber", "08012345678")
                .param("postalCode", "1234567")
                .param("address", "名古屋市中区")
                .with(csrf())
                .with(user(userDetails)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attributeExists("successMessage"));
    }
    
    @Test
    void testGetMemberList_正常に町内会員全一覧を表示() throws Exception{
    	// ログインユーザ準備
    	District district = new District();
    	district.setId(1);
    	district.setAssociation("中央町");
    	district.setName("中央町");
    	
    	Role role = new Role();
    	role.setId(1);
    	role.setName("ROLE_町内会長");
    	
    	User loginUser = new User();
    	loginUser.setId(1);
    	loginUser.setName("会長太郎");
    	loginUser.setDistrict(district);
    	loginUser.setRole(role);
    	
    	UserDetailsImpl userDetails = new UserDetailsImpl(
    			loginUser,
    			List.of(new SimpleGrantedAuthority("ROLE_町内会長"))
    	);
    	
    	// 一覧に表示するユーザ
    	User member = new User();
    	member.setId(2);
    	member.setName("会員花子");
    	member.setRole(role);
    	member.setDistrict(district);
    	
    	// モック設定
    	Page<User> mockUserPage = new PageImpl<>(List.of(member));
    	when(userService.searchUsersByConditions(anyInt(), any(), any(), any(), any())).thenReturn(mockUserPage);
    	when(roleRepository.findAll()).thenReturn(List.of(role));
    	when(committeeClassificationRepository.findAll()).thenReturn(List.of());
    	
    	// 実行・検証
    	mockMvc.perform(get("/user/member-list")
    				.with(user(userDetails)))
    			.andExpect(status().isOk())
    			.andExpect(view().name("user/member-list"))
    			.andExpect(model().attributeExists("userAssociation"))
    			.andExpect(model().attributeExists("userPage"))
    			.andExpect(model().attributeExists("roles"))
    			.andExpect(model().attributeExists("committees"));
    			
    }
    
    @Test
    void testGetAreaMembers_正常に区会員一覧を表示() throws Exception{
    	// ログインユーザ準備
    	District district = new District();
    	district.setId(1);
    	district.setAssociation("中央町");
    	district.setName("中央町");
    	district.setArea("中央町");
    	
    	Role role = new Role();
    	role.setId(1);
    	role.setName("ROLE_区長");
    	
    	User loginUser = new User();
    	loginUser.setId(1);
    	loginUser.setName("区長太郎");
    	loginUser.setDistrict(district);
    	loginUser.setRole(role);
    	
    	// ログイン中のユーザ情報と権限を持ったUserDetailsを作る
    	UserDetailsImpl userDetails = new UserDetailsImpl(
    			loginUser,
    			List.of(new SimpleGrantedAuthority("ROLE_区長"))
    	);
    	
    	// 一覧に表示するユーザ
    	User member = new User();
    	member.setId(2);
    	member.setName("会員花子");
    	member.setRole(role);
    	member.setDistrict(district);
    	
    	// モック設定
    	Page<User> mockUserPage = new PageImpl<>(List.of(member));
    	when(userService.findUsersInSameArea(eq("中央町"), eq("中央町"), any(Pageable.class))).thenReturn(mockUserPage);
    	when(roleRepository.findAll()).thenReturn(List.of(role));
    	
    	when(committeeClassificationRepository.findAll()).thenReturn(List.of());
    	
    	// 実行・検証
    	mockMvc.perform(get("/user/area-members")
    					.param("page", "0")
    					.with(user(userDetails)))
    			.andExpect(status().isOk())
    			.andExpect(view().name("user/area-members"))
    	.andExpect(model().attributeExists("association"))
    		.andExpect(model().attributeExists("userPage"));
    	
    }
    
    @Test
    void testGetAreaMembers_未ログイン時はGoogle認証ページにリダイレクト() throws Exception {
        mockMvc.perform(get("/user/area-members"))
            .andExpect(status().is3xxRedirection()) // 302
            .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"));

    }


    @Test
    void testGetCommiteeMembers_正常に委員所属会員一覧を表示() throws Exception{
    	// ログインユーザ準備
    	District district = new District();
    	district.setId(1);
    	district.setAssociation("中央町");
    	district.setName("中央町");
    	district.setArea("中央町");
    	
    	Role role = new Role();
    	role.setId(1);
    	role.setName("ROLE_委員長");
    	
    	CommitteeClassification committee = new CommitteeClassification();
    	committee.setId(1);
    	committee.setName("テスト委員");
    	
    	
    	User loginUser = new User();
    	loginUser.setId(1);
    	loginUser.setName("委員長太郎");
    	loginUser.setDistrict(district);
    	loginUser.setRole(role);
    	loginUser.setCommittee(committee);
    	
    	// ログイン中のユーザ情報と権限を持ったUserDetailsを作る
    	UserDetailsImpl userDetails = new UserDetailsImpl(
    			loginUser,
    			List.of(new SimpleGrantedAuthority("ROLE_委員長"))
    	);
    	
    	// 一覧に表示するユーザ
    	User member = new User();
    	member.setId(2);
    	member.setName("会員委員花子");
    	member.setRole(role);
    	member.setDistrict(district);
    	member.setCommittee(committee);
    	
    	// Pageableの設定(Controllerと同じ)
    	Sort sort= Sort.by(Sort.Order.asc("role.id"), Sort.Order.asc("furigana"));
    	Pageable pageable = PageRequest.of(0, 10, sort);
    	
    	// モック設定
    	Page<User> mockUserPage = new PageImpl<>(List.of(member));
    	when(userService.getUsersInSameDistrictAndAreaANDCommittee(eq(loginUser.getId()), eq(pageable))).thenReturn(mockUserPage);
    	
    	// 実行・検証
    	mockMvc.perform(get("/user/committee-members")
    					.param("page", "0")
    					.with(user(userDetails)))
    			.andExpect(status().isOk())
    			.andExpect(view().name("user/committee-members"))
    	.andExpect(model().attributeExists("association"))
    		.andExpect(model().attributeExists("userPage"));
    	
    }
    
    @Test
    void testGetCommitteeMembers_未ログイン時はGoogle認証ページにリダイレクト() throws Exception {
        mockMvc.perform(get("/user/committee-members"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"));
    }

    
    @Test
    void testMypageView_正常にマイページを表示() throws Exception {
    	User loginUser = new User();
    	loginUser.setId(1);
    	loginUser.setEmail("test@mail.com");
    	loginUser.setUserId("saitousan157");
    	loginUser.setName("斎藤義男");
    	loginUser.setFurigana("サイトウヨシオ");
    	loginUser.setPhoneNumber("12345678910");
    	
    	Role role = new Role();
    	role.setId(1);
    	role.setName("ROLE_会員");
    	loginUser.setRole(role);
    	
    	// ログイン中のユーザ情報と権限を持ったUserDetailsを作る
    	UserDetailsImpl userDetails = new UserDetailsImpl(
    			loginUser,
    			List.of(new SimpleGrantedAuthority("ROLE_会員"))
    	);
    	
        mockMvc.perform(get("/user/mypage")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("user/mypage"))
        	.andExpect(model().attributeExists("email", "userId", "name", "furigana", "phoneNumber"));
    }
    
    @Test
    void testMypageView_未ログイン時はGoogle認証ページにリダイレクト() throws Exception {
        mockMvc.perform(get("/user/mypage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"));
    }

    
    @Test
    void testShowEditForm_正常に自分の情報を編集できる() throws Exception {
    	User loginUser = new User();
    	loginUser.setId(1);
    	loginUser.setEmail("test@mail.com");
    	loginUser.setUserId("saitousan157");
    	loginUser.setName("斎藤義男");
    	loginUser.setFurigana("サイトウヨシオ");
    	loginUser.setPhoneNumber("12345678910");
    	
    	Role role = new Role();
    	role.setId(1);
    	role.setName("ROLE_会員");
    	loginUser.setRole(role);
    	
    	// ログイン中のユーザ情報と権限を持ったUserDetailsを作る
    	UserDetailsImpl userDetails = new UserDetailsImpl(
    			loginUser,
    			List.of(new SimpleGrantedAuthority("ROLE_会員"))
    	);
    	
        mockMvc.perform(get("/user/edit-page")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("user/edit-page"))
        	.andExpect(model().attributeExists("form"));
    }
    
    @Test
    void testShowEditForm_未ログイン時はGoogle認証ページにリダイレクト() throws Exception {
        mockMvc.perform(get("/user/edit-page"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"));
    }

    
    @Test
    void testEditUser_正常に情報を更新してマイページにリダイレクト() throws Exception{
    	User loginUser = new User();
    	loginUser.setId(1);
    	loginUser.setEmail("test@mail.com");
    	loginUser.setUserId("saitousan157");
    	loginUser.setName("斎藤義男");
    	loginUser.setFurigana("サイトウヨシオ");
    	loginUser.setPhoneNumber("12345678910");
    	
    	Role role = new Role();
    	role.setId(1);
    	role.setName("ROLE_会員");
    	loginUser.setRole(role);
    	
    	// ログイン中のユーザ情報と権限を持ったUserDetailsを作る
    	UserDetailsImpl userDetails = new UserDetailsImpl(
    			loginUser,
    			List.of(new SimpleGrantedAuthority("ROLE_会員"))
    	);
    	
    	// フォーム入力値
    	String password = "password123";
    	String confirmPassword = "password123";
    	
    	// モック設定
    	when(userService.passwordDoubleCheck(password,  confirmPassword)).thenReturn(true);
    	
    	// 実行・検証
    	mockMvc.perform(post("/user/edit")
    			.param("email", "test@mail.com")
    			.param("userId", "saitousan157")
    			.param("name", "斎藤義男")
    			.param("furigana", "サイトウヨシオ")
    			.param("phoneNumber", "12345678910")
    			.param("password", password)
    			.param("confirmPassword", confirmPassword)
    			.with(csrf())
    			.with(user(userDetails)))
    		.andExpect(status().is3xxRedirection())
    		.andExpect(redirectedUrl("/user/mypage"))
    		.andExpect(flash().attributeExists("successMessage"));
    	
    	// 更新処理が呼び出されていること
    	verify(userService).updateUser(any(UserEditForm.class), eq(loginUser));
    }
    
    @Test
    void testEditUser_バリデーションエラーで編集画面に戻る() throws Exception {
        // ログインユーザ準備
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setEmail("test@mail.com");
        loginUser.setUserId("saitousan157");
        loginUser.setName("斎藤義男");
        loginUser.setFurigana("サイトウヨシオ");
        loginUser.setPhoneNumber("12345678910");

        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_会員");
        loginUser.setRole(role);

        UserDetailsImpl userDetails = new UserDetailsImpl(
                loginUser,
                List.of(new SimpleGrantedAuthority("ROLE_会員"))
        );

        // 実行・検証
        mockMvc.perform(post("/user/edit")
                .param("email", "") // ★ わざと空にしてエラー発生させる
                .param("userId", "saitousan157")
                .param("name", "") // ★ ここも空
                .param("furigana", "サイトウヨシオ")
                .param("phoneNumber", "12345678910")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf())
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("user/edit-page"))
            .andExpect(model().attributeHasFieldErrors("form", "email", "name"));
    }


}