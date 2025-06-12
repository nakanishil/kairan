package com.example.kairan.security;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private OAuth2User oauth2User;

    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        // テスト対象サービスを delegate 付きで生成
        service = new CustomOAuth2UserService(userRepository, delegate);
        // delegate.loadUser() はいつも oauth2User を返す
        when(delegate.loadUser(userRequest)).thenReturn(oauth2User);
    }

    private User createTestUser(String googleId, boolean linked) {
        Role role = new Role();
        role.setName("ROLE_会員");

        User u = new User();
        u.setGoogleId(googleId);
        u.setGoogleLinked(linked);
        u.setRole(role);
        return u;
    }

    @Test
    void loadUser_正常にUserDetailsImplを返す() {
        // Arrange
        String googleId = "abc123";
        when(oauth2User.getAttribute("sub")).thenReturn(googleId);

        User user = createTestUser(googleId, true);
        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.of(user));

        // Act
        OAuth2User result = service.loadUser(userRequest);

        // Assert
        assertThat(result).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl details = (UserDetailsImpl) result;
        assertThat(details.getUser().getGoogleId()).isEqualTo(googleId);
        assertThat(details.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_会員");
    }

    @Test
    void loadUser_google連携未許可の場合は例外をスロー() {
        // Arrange
        String googleId = "abc123";
        when(delegate.loadUser(userRequest)).thenReturn(oauth2User);
        when(oauth2User.getAttribute("sub")).thenReturn(googleId);

        User user = createTestUser(googleId, false);
        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.of(user));

        // Act  Assert
        assertThatThrownBy(() -> service.loadUser(userRequest))
            .isInstanceOf(OAuth2AuthenticationException.class);
    }
    
    @Test
    void loadUser_googleId未登録の場合は例外をスロー() {
        // Arrange
        String googleId = "nonexistent";
        when(delegate.loadUser(userRequest)).thenReturn(oauth2User);
        when(oauth2User.getAttribute("sub")).thenReturn(googleId);

        when(userRepository.findByGoogleId(googleId)).thenReturn(Optional.empty());

        // Act
        OAuth2AuthenticationException ex = 
            assertThrows(OAuth2AuthenticationException.class,
                         () -> service.loadUser(userRequest));

        // Assert: エラーコード（元の throw の引数）を検証
        assertThat(ex.getError().getErrorCode())
            .contains("このGoogleアカウントはKairanに連携されていません。");
    }


    

}
