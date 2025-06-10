package com.example.kairan.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.kairan.entity.Circular;
import com.example.kairan.entity.CircularFile;
import com.example.kairan.entity.District;
import com.example.kairan.entity.User;
import com.example.kairan.form.CircularEditForm;
import com.example.kairan.form.CircularFileForm;
import com.example.kairan.form.CircularForm;
import com.example.kairan.repository.CircularFileRepository;
import com.example.kairan.repository.CircularRepository;

@ExtendWith(MockitoExtension.class)
class CircularServiceTest {

	@InjectMocks
	private CircularService circularService;

	@Mock
	private CircularRepository circularRepository;

	@Mock
	private CircularFileRepository circularFileRepository;

	@TempDir
	Path tempDir; // 一時的なアップロードディレクトリ

	private User loginUser;
	private District district;

	@BeforeEach
	void setUp() throws IOException{
		tempDir = Files.createTempDirectory("upload-test");

	    district = new District();
	    district.setId(1);
	    district.setAssociation("テスト町内会");

	    loginUser = new User();
	    loginUser.setId(1);
	    loginUser.setName("テストユーザー");
	    loginUser.setDistrict(district);

	    // ← ここを追加（@Valueフィールドの代わりに直接代入）
	    ReflectionTestUtils.setField(circularService, "uploadDir", tempDir.toString());
	}

	@Test
	void createCircular_ファイルなし_正常に作成される() throws IOException {
		CircularForm form = new CircularForm();
		form.setName("回覧板のタイトル");
		form.setDescription("説明文です");
		form.setIsUrgent(false);
		form.setFileList(null); // ファイルなし

		Circular savedCircular = new Circular();
		savedCircular.setId(1);
		when(circularRepository.save(any(Circular.class))).thenReturn(savedCircular);

		Circular result = circularService.createCircular(form, loginUser);

		assertThat(result.getId()).isEqualTo(1);
		verify(circularRepository, times(1)).save(any(Circular.class));
		verify(circularFileRepository, never()).save(any());
	}
	
	@Test
	void createCircular_ファイルあり_正常に作成される() throws IOException {

		CircularForm form = new CircularForm();
		form.setName("ファイル付き回覧板");
		form.setDescription("説明あり");
		form.setIsUrgent(true);
		
		// ダミーファイル
		MockMultipartFile mockFile = new MockMultipartFile(
				"file", "test.txt", "text/plain", "ファイルの中身".getBytes());
		
		CircularFileForm fileForm = new CircularFileForm();
		fileForm.setFileName("添付資料1");
		fileForm.setFile(mockFile);
		
		form.setFileList(List.of(fileForm));
		
		// Circularの保存をモック
		Circular savedCircular = new Circular();
		savedCircular.setId(2);
		when(circularRepository.save(any(Circular.class))).thenReturn(savedCircular);
		
		// act
		Circular result = circularService.createCircular(form,  loginUser);
		
		// assert
		assertThat(result.getId()).isEqualTo(2);
		verify(circularRepository, times(1)).save(any(Circular.class));
		verify(circularFileRepository, times(1)).save(any(CircularFile.class));
		
		// アップロードファイルが保存されたか確認（物理ファイルチェック)
		File uploadDirPath = new File(tempDir.toString());
		assertThat(uploadDirPath.listFiles()).isNotEmpty();
	}
	
	@Test
	void editCircularById_ファイル無しで正常に更新される() {
		// arrange
		Circular circular = new Circular();
		circular.setId(1);
		circular.setName("旧タイトル");
		circular.setDescription("旧説明");
		circular.setIsUrgent(false);
		
		CircularEditForm form = new CircularEditForm();
		form.setName("新タイトル");
		form.setDescription("新しい説明");
		form.setIsUrgent(true);
		form.setFileList(List.of());
		
		User loginUser = new User();
		loginUser.setId(1);
		
		// モックの戻り値設定
		when(circularRepository
				.findById(1)).thenReturn(Optional.of(circular));
		when(circularFileRepository
				.findByCircularAndDeletedAtIsNullOrderByIdAsc(circular))
				.thenReturn(List.of());
		
		// act
		circularService.editCircularById(1,  form, loginUser);
		
		// assert
		verify(circularRepository).save(argThat(updated ->
			updated.getName().equals("新タイトル") &&
			updated.getDescription().equals("新しい説明") &&
			updated.getIsUrgent().equals(true)
		));
		verify(circularFileRepository, never()).save(any(CircularFile.class));
	}
	
	@Test
	void editCircularById_ファイルありで正常に更新される() {
		// arrange
		Circular circular = new Circular();
		circular.setId(1);
		circular.setName("旧タイトル");
		circular.setDescription("旧説明");
		circular.setIsUrgent(false);
		
		CircularEditForm form = new CircularEditForm();
		form.setName("新タイトル");
		form.setDescription("新しい説明");
		form.setIsUrgent(true);
		form.setFileList(List.of());
		
		User loginUser = new User();
		loginUser.setId(1);
		
		MockMultipartFile mockFile = new MockMultipartFile(
				"file", "test.txt", "text/plain", "ファイルの中身".getBytes());
		CircularFileForm fileForm = new CircularFileForm();
		fileForm.setFileName("新しいファイル名");
		fileForm.setFile(mockFile);
		
		form.setFileList(List.of(fileForm));
		
		// モックの戻り値設定
		when(circularRepository
				.findById(1)).thenReturn(Optional.of(circular));
		when(circularFileRepository
				.findByCircularAndDeletedAtIsNullOrderByIdAsc(circular))
				.thenReturn(List.of());
		
		// act
		circularService.editCircularById(1,  form, loginUser);
		
		// assert
		verify(circularRepository).save(argThat(updated ->
			updated.getName().equals("新タイトル") &&
			updated.getDescription().equals("新しい説明") &&
			updated.getIsUrgent().equals(true)
		));
		verify(circularFileRepository, times(1)).save(any(CircularFile.class));
		
		// 物理ファイルが保存されているか確認
		File uploadDirPath = new File(tempDir.toString());
		assertThat(uploadDirPath.listFiles()).isNotEmpty();
	}
	
	@Test
	void softDeletedCircular_正常にソフトデリートされる() {
		//arrange
		Circular circular = new Circular();
		assertThat(circular.getDeletedAt()).isNull();
		
		// act
		circularService.softDeletedCircular(circular);
		
		// assert
		assertThat(circular.getDeletedAt()).isNotNull();
	}


}
