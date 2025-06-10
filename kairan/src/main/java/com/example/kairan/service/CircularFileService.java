package com.example.kairan.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kairan.entity.Circular;
import com.example.kairan.entity.CircularFile;
import com.example.kairan.repository.CircularFileRepository;

@Service
public class CircularFileService {
	private final CircularFileRepository circularFileRepository;
	
	public CircularFileService(CircularFileRepository circularFileRepository) {
		this.circularFileRepository = circularFileRepository;
	}

	
	// 特定の回覧板から対応するcircularFileを取得
	public List<CircularFile> getfindByCircularId(Circular circular) {
		return circularFileRepository.findByCircularAndDeletedAtIsNullOrderByIdAsc(circular);
	}
	
	// 回覧板のファイルソフトデリート
	@Transactional
	public void softDeletedCircularFile(Circular circular) {
		List<CircularFile> circularFileList = getfindByCircularId(circular);
		for (CircularFile circularFile : circularFileList) {
			circularFile.softDelete();
		}
	}
}
