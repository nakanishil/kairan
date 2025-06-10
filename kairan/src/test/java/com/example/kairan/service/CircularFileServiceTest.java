package com.example.kairan.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kairan.entity.Circular;
import com.example.kairan.entity.CircularFile;
import com.example.kairan.repository.CircularFileRepository;

@ExtendWith(MockitoExtension.class)
public class CircularFileServiceTest {

	@InjectMocks
	private CircularFileService circularFileService;
	
	@Mock
	private CircularFileRepository circularFileRepository;
	
	@Test
	void softDeletedCircularFIle_正常にソフトデリートされる() {
		// arrange
		Circular circular = new Circular();
		circular.setId(2);
		
		CircularFile file1 = new CircularFile();
		CircularFile file2 = new CircularFile();
		
		List<CircularFile> fileList = List.of(file1, file2);
		
		when(circularFileRepository.findByCircularAndDeletedAtIsNullOrderByIdAsc(circular))
			.thenReturn(fileList);
		
		// act
		circularFileService.softDeletedCircularFile(circular);
		
		// assert
		assertThat(file1.getDeletedAt()).isNotNull();
		assertThat(file2.getDeletedAt()).isNotNull();
		
	}
}
