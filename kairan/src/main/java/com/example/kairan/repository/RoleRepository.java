package com.example.kairan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kairan.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
	// 役職名で検索
	Optional<Role> findByName(String name);

}
