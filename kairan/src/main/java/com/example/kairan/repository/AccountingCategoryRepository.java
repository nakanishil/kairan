package com.example.kairan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kairan.entity.AccountingCategory;

@Repository
public interface AccountingCategoryRepository extends JpaRepository<AccountingCategory, Integer> {
	
	// Idで検索
	Optional<AccountingCategory> findById(int id);

}
