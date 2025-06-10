package com.example.kairan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.kairan.entity.AccountingCategory;
import com.example.kairan.repository.AccountingCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountingCategoryService {
	private final AccountingCategoryRepository accountingCategoryRepository;
	
	public List<AccountingCategory> findAll() {
		return accountingCategoryRepository.findAll();
	}
	
	public Optional<AccountingCategory> findById(int id) {
		return accountingCategoryRepository.findById(id);
	}
}
