package com.example.kairan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kairan.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer>{
	
}
