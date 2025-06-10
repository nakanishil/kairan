package com.example.kairan.form;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class CircularFileForm {
	private MultipartFile file;
	private String fileName;
}
