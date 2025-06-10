package com.example.kairan.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.kairan.entity.User;
import com.example.kairan.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;
	
	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		User user = userRepository.findByUserId(userId)
			.orElseThrow(() -> new UsernameNotFoundException("ユーザが見つかりませんでした。"));
		
		// ソフトデリートされていたらログイン拒否
		if (user.isDeleted()) {
			throw new UsernameNotFoundException("このユーザは削除済みです。");
		}
		
		String userRoleName = user.getRole().getName();
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(userRoleName));
		
		return new UserDetailsImpl(user, authorities);
		
	}
}
