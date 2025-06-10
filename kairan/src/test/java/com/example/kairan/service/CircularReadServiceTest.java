package com.example.kairan.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kairan.entity.Circular;
import com.example.kairan.entity.CircularRead;
import com.example.kairan.entity.User;
import com.example.kairan.repository.CircularReadRepository;

@ExtendWith(MockitoExtension.class)
public class CircularReadServiceTest {
	
	@InjectMocks
	private CircularReadService circularReadService;
	
	@Mock
	private CircularReadRepository circularReadRepository;
	
	private User user;
	private Circular circular;
	
	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1);
		
		circular = new Circular();
		circular.setId(10);
	}
	
	@Test
	void readStatusCheck_未読の場合trueを返す() {
		when(circularReadRepository.findByCircularIdAndUserId(10, 1))
			.thenReturn(Optional.empty());
		
		Boolean result = circularReadService.readStatusCheck(user, circular);
		
		assertThat(result).isTrue();
	}
	
	@Test
	void readStatusCheck_未読の場合falseを返す() {
		CircularRead read = new CircularRead();
		when(circularReadRepository.findByCircularIdAndUserId(10, 1))
			.thenReturn(Optional.of(read));
		
		Boolean result = circularReadService.readStatusCheck(user, circular);
		
		assertThat(result).isFalse();
	}
}
