package com.example.kairan.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.kairan.entity.District;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.form.AdminDistrictRegiForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.AdminDistrictService;

@WebMvcTest(AdminDistrictController.class)
class AdminDistrictControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminDistrictService adminDistrictService;

    private User loginUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        // モックユーザーとログイン情報作成
        District district = new District();
        district.setRegionCode("RC001");

        loginUser = new User();
        loginUser.setDistrict(district);
        Role role = new Role();
        role.setName("ROLE_行政");
        loginUser.setRole(role);

        userDetails = new UserDetailsImpl(loginUser, List.of(new SimpleGrantedAuthority("ROLE_行政")));
    }
// 一覧ページ
    @Test
    void associationList_正常に表示される() throws Exception {
        // モックページ作成
        Page<District> page = new PageImpl<>(List.of(new District()));

        when(adminDistrictService.getDistinctDistrictsByAssociation(eq("RC001"), any(Pageable.class)))
            .thenReturn(page);

        mockMvc.perform(get("/admin/district/association-list")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/district/association-list"))
            .andExpect(model().attributeExists("listPage"));
    }
// 詳細ページ 
    @Test
    void associationDetail_正常に表示される() throws Exception {
        int districtId = 1;

        // モックデータ
        User mayor = new User();
        District district = new District();
        district.setRegionCode("RC001");
        loginUser.setDistrict(district);

        List<User> mockMayors = List.of(mayor);
        List<District> mockDistricts = List.of(district);

        // モック定義
        when(adminDistrictService.getMayors(loginUser)).thenReturn(mockMayors);
        when(adminDistrictService.getDistrictsByAssociationSorted(loginUser)).thenReturn(mockDistricts);

        mockMvc.perform(get("/admin/district/association-detail/{id}", districtId)
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/district/association-detail"))
            .andExpect(model().attribute("mayorsList", mockMayors))
            .andExpect(model().attribute("districtList", mockDistricts));
    }
    
    @Test
    void registerPage_正常に表示される() throws Exception {
        mockMvc.perform(get("/admin/district/register-page")
                .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/district/register-page"))
            .andExpect(model().attributeExists("form"));
    }
    
    @Test
    void registerDistrict_正常に登録処理が完了し詳細ページに遷移する() throws Exception {
        // arrange
        AdminDistrictRegiForm form = new AdminDistrictRegiForm();
        form.setName("東町");
        form.setRegioncode("RC001");
        form.setAssociation("東町内会");
        form.setArea("1区");
        form.setDescription("テスト町内会");

        // モック：登録後のDistrict
        District mockDistrict = new District();
        mockDistrict.setName("東町");

        // モック：仮ユーザー（会長・区長）
        User kaicho = new User(); kaicho.setName("会長テスト");
        User kucho = new User(); kucho.setName("区長テスト");
        List<User> mockUsers = List.of(kaicho, kucho);

        when(adminDistrictService.regiDistrict(any(AdminDistrictRegiForm.class))).thenReturn(mockDistrict);
        when(adminDistrictService.casualUserRegi(mockDistrict)).thenReturn(mockUsers);

        mockMvc.perform(post("/admin/district/register")
        		.param("name", "東町")
                .param("regioncode", "123456")     
                .param("association", "東町内会")
                .param("area", "1区")
                .param("description", "テスト説明")
                .with(user(userDetails))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/district/new-district-detail"))
            .andExpect(model().attribute("district", mockDistrict))
            .andExpect(model().attribute("kaicho", kaicho))
            .andExpect(model().attribute("kucho", kucho))
            .andExpect(model().attribute("successMessage", "町内会の作成に成功しました"));

        // 呼び出し確認
        verify(adminDistrictService).regiDistrict(any(AdminDistrictRegiForm.class));
        verify(adminDistrictService).casualUserRegi(mockDistrict);
    }
    
    @Test
    void registerDistrict_バリデーションエラーで登録ページに戻る() throws Exception {
        mockMvc.perform(post("/admin/district/register")
                .param("name", "東町")
                .param("regioncode", "") //
                .param("association", "東町内会")
                .param("area", "1区")
                .param("description", "テスト説明")
                .with(user(userDetails))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/district/register-page"))
            .andExpect(model().attribute("errorMessage", "エラーが発生しました。"));

        // Serviceメソッドは呼ばれていないことを確認
        verify(adminDistrictService, never()).regiDistrict(any());
        verify(adminDistrictService, never()).casualUserRegi(any());
    }
    
    @Test
    void registerDistrict_サービス層で例外が発生しリダイレクトされる() throws Exception {
        // モック：regiDistrict が例外をスロー
        when(adminDistrictService.regiDistrict(any(AdminDistrictRegiForm.class)))
            .thenThrow(new IllegalArgumentException("登録済みの町内会名です"));

        mockMvc.perform(post("/admin/district/register")
                .param("name", "東町")
                .param("regioncode", "123456")
                .param("association", "東町内会")
                .param("area", "1区")
                .param("description", "テスト説明")
                .with(user(userDetails))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/district/association-list"))
            .andExpect(flash().attribute("errorMessage", "登録済みの町内会名です"));

        // casualUserRegi は呼ばれていないことを確認
        verify(adminDistrictService, never()).casualUserRegi(any());
    }





}
