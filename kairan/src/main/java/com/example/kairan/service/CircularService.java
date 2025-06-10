package com.example.kairan.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.kairan.entity.Circular;
import com.example.kairan.entity.CircularFile;
import com.example.kairan.entity.User;
import com.example.kairan.form.CircularEditForm;
import com.example.kairan.form.CircularFileForm;
import com.example.kairan.form.CircularForm;
import com.example.kairan.repository.CircularFileRepository;
import com.example.kairan.repository.CircularReadRepository;
import com.example.kairan.repository.CircularRepository;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class CircularService {
	private final CircularRepository circularRepository;
	private final CircularFileRepository circularFileRepository;
	private final CircularReadRepository circularReadRepository;

	@Value("${upload.dir}")
	private String uploadDir;


	// 回覧板の作成
	@Transactional
	public Circular createCircular(CircularForm form, User loginUser) throws IOException {
		Circular circular = new Circular();

		circular.setName(form.getName());
		circular.setDescription(form.getDescription());
		circular.setAuthor(loginUser);
		circular.setDistrict(loginUser.getDistrict());
		circular.setIsUrgent(form.getIsUrgent());

		// circularを保存してidを確定
		Circular savedCircular = circularRepository.save(circular);

		// ファイル情報がある場合にCircularFileも作成
		List<CircularFileForm> fileList = form.getFileList();
		if(fileList != null) {
			for(CircularFileForm fileForm : fileList) {
				MultipartFile file = fileForm.getFile();
				if(file != null && !file.isEmpty()) {
					// 保存ファイル名をUUIDで作成
					String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
					String savePath = uploadDir + "/" + uniqueName;

					// ファイル保存
					file.transferTo(new File(savePath));


					CircularFile circularFile = new CircularFile();
					circularFile.setCircular(savedCircular);
					circularFile.setFileName(fileForm.getFileName());
					circularFile.setFilePath(savePath);
					circularFileRepository.save(circularFile);
				}
			}
		}

		return savedCircular;

	}

	//最新順に回覧板を取得 *
	@Transactional(readOnly = true)
	public Page<Circular> getLatestCirculars(String association, Pageable pageable) {
		return circularRepository.findByAssociationAndNotDeleted(association, pageable);
	}

	// 最新3件の回覧板を取得 *
	@Transactional(readOnly = true)
	public List<Circular> getLatestCircularsThree(String association){
		Pageable topThree = PageRequest.of(0, 3); // 0ページ目から3件取得
		return circularRepository.findByAssociationAndNotDeletedList(association, topThree);
	}

	// 特定IDの回覧板を取得（ソフトデリートされていないもの）*
	public Optional<Circular> getCircularById(Integer id) {
		return circularRepository.findById(id).filter(c -> c.getDeletedAt() == null);
	}

	// 編集機能
	public void editCircularById(int circularId, CircularEditForm form, User loginUser) {
		Circular circular = getCircularById(circularId)
			.orElseThrow(() -> new IllegalArgumentException("指定したIDが存在しないか削除されています。"));

		List<CircularFile> oldFiles = circularFileRepository
			.findByCircularAndDeletedAtIsNullOrderByIdAsc(circular);

		List<CircularFileForm> newFiles = form.getFileList();

		// Circular更新
		circular.setName(form.getName());
		circular.setDescription(form.getDescription());
		circular.setIsUrgent(form.getIsUrgent());
		circularRepository.save(circular);


		if(!oldFiles.isEmpty()) {
			for(CircularFile oldFile : oldFiles) {
				oldFile.softDelete();
				// 明示的なセーブ
				circularFileRepository.save(oldFile);
			}
		}
		
		for (CircularFileForm newFileForm : form.getFileList()) {
		    MultipartFile file = newFileForm.getFile();

		    if (file != null && !file.isEmpty()) {
		        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
		        String savePath = uploadDir + "/" + uniqueName;

		        try {
		            file.transferTo(new File(savePath));
		        } catch (IOException e) {
		            throw new RuntimeException("ファイル保存に失敗しました", e);
		        }

		        CircularFile newFile = new CircularFile();
		        newFile.setCircular(circular);
		        newFile.setFileName(newFileForm.getFileName()); // ←表示名を使う
		        newFile.setFilePath(savePath);
		        circularFileRepository.save(newFile);
		    }
		}

	}

	// 回覧板の削除（ソフトデリート）
	@Transactional
	public void softDeletedCircular(Circular circular) {
		circular.softDelete();
	}
	
}
