package com.example.kairan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kairan.entity.PaymentMethod;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

}
