package com.example.kairan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kairan.entity.CircularRead;

@Repository
public interface CircularReadRepository extends JpaRepository<CircularRead, Integer>{
	// 特定のユーザが既読した回覧板を取得
	Optional<CircularRead> findByCircularIdAndUserId(Integer circularId, Integer userId);
	
	// 特定の回覧板に紐づく既読状況を取得
	long countByCircularId(Integer circularId);
}
