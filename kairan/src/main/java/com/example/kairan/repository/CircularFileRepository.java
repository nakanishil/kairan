package com.example.kairan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kairan.entity.Circular;
import com.example.kairan.entity.CircularFile;

@Repository
public interface CircularFileRepository extends JpaRepository<CircularFile, Integer>{
	// 特定の回覧板に紐づくファイル一覧を取得
	List<CircularFile> findByCircularAndDeletedAtIsNullOrderByIdAsc(Circular circular);
}
