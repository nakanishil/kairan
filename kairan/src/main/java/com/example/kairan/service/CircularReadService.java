package com.example.kairan.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.kairan.entity.Circular;
import com.example.kairan.entity.CircularRead;
import com.example.kairan.entity.User;
import com.example.kairan.repository.CircularReadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CircularReadService {
	private final CircularReadRepository circularReadRepository;
	
	// 指定したIDが既読済みかチェック
	public Boolean readStatusCheck(User loginUser, Circular circular){
		int circularId = circular.getId();
		int userId = loginUser.getId();
		
		Optional<CircularRead> status = circularReadRepository.findByCircularIdAndUserId(circularId, userId);
		
		if(status.isEmpty()) {
			return true;
		}
		return false;
	}
	
	// 既読を付ける処理
	public void markAsRead(User loginUser, Circular circular) {
			CircularRead circularRead = new CircularRead();
			circularRead.setCircular(circular);
			circularRead.setUser(loginUser);
			circularReadRepository.save(circularRead);
	}
	
}
