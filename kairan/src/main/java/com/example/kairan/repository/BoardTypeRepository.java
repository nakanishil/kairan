package com.example.kairan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kairan.entity.BoardType;

public interface BoardTypeRepository extends JpaRepository<BoardType, Integer> {
    List<BoardType> findByBoardName(String boardName);
}

