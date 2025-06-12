package com.example.kairan.security;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.kairan.entity.User;
import com.example.kairan.repository.UserRepository;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    /** Spring が自動注入で使うコンストラクタ */
    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository) {
        this(userRepository, new DefaultOAuth2UserService());
    }

    /** テスト時にモックを注入できるコンストラクタ */
    public CustomOAuth2UserService(UserRepository userRepository,
                                   OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate) {
        this.userRepository = userRepository;
        this.delegate = delegate;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String googleId = oauth2User.getAttribute("sub");

        // --- セッション連携処理（省略） ---
        var requestAttributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes attributes) {
            var session = attributes.getRequest().getSession(false);
            boolean isLinkMode = session != null && Boolean.TRUE.equals(session.getAttribute("oauth_link_mode"));
            if (isLinkMode) {
                session.removeAttribute("oauth_link_mode");
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl currentUserDetails) {
                    User currentUser = currentUserDetails.getUser();
                    currentUser.setGoogleId(googleId);
                    currentUser.setGoogleLinked(true);
                    userRepository.save(currentUser);
                    session.setAttribute("google_link_success", true);
                    return new UserDetailsImpl(
                        currentUser,
                        List.of(new SimpleGrantedAuthority(currentUser.getRole().getName())),
                        oauth2User.getAttributes()
                    );
                }
            }
        }

        // --- 通常のログインフロー ---
        Optional<User> userOpt = userRepository.findByGoogleId(googleId);
        if (userOpt.isEmpty()) {
            throw new OAuth2AuthenticationException("このGoogleアカウントはKairanに連携されていません。");
        }
        User user = userOpt.get();
        if (!user.getGoogleLinked()) {
            throw new OAuth2AuthenticationException("このアカウントはGoogleログイン未許可です。");
        }
        return new UserDetailsImpl(
            user,
            List.of(new SimpleGrantedAuthority(user.getRole().getName())),
            oauth2User.getAttributes()
        );
    }
}
