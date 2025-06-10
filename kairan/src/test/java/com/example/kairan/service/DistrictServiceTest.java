package com.example.kairan.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.example.kairan.entity.District;
import com.example.kairan.entity.User;
import com.example.kairan.form.DistrictEditForm;
import com.example.kairan.form.DistrictRegiForm;
import com.example.kairan.repository.DistrictRepository;
import com.example.kairan.repository.UserRepository;
import com.example.kairan.service.DistrictService;

public class DistrictServiceTest {
	
	@InjectMocks
	private DistrictService districtService;
	
	@Mock
	private DistrictRepository districtRepository;
	
	@Mock
	private UserRepository userRepository;
	
	private User loginUser;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		
		// テスト用ログインユーザ作成
		District district = new District();
		district.setId(1);
		district.setRegionCode("RC001");
		district.setAssociation("A001");
		district.setArea("テスト区");
		
		loginUser = new User();
		loginUser.setDistrict(district);
		loginUser.setId(1);
	}
	
	@Test
	void districtCategoryRegi_区カテゴリを正常に登録できる() {
		// arrange
		DistrictRegiForm form = new DistrictRegiForm();
		form.setName("新しい区カテゴリ");
		
		// まだ15件未満
		org.mockito.Mockito.when(districtRepository.
				countByRegionCodeAndAssociationAndDeletedAtIsNull("RC001","A001"))
					.thenReturn(Long.valueOf(5L));
		
		// formのareaが空の場合
		Mockito.when(districtRepository.
				findByRegionCodeAndAssociationAndAreaAndDeletedAtIsNullOrderByIdAsc(
						"RC001", "A001", null))
					.thenReturn(Collections.emptyList());
		
		// act
		districtService.districtCategoryRegi(form, loginUser);
		
		// assert
		org.mockito.Mockito.verify(districtRepository).save(org.mockito.Mockito.any(District.class));
	}
	
	@Test
	void districtCategoryRegi_登録上限1５件超えた時に例外をスロー() {
		// arrange
		DistrictRegiForm form = new DistrictRegiForm();
		form.setName("新しい区カテゴリ");
		
		// 登録件数15件を超えている
		org.mockito.Mockito.when(districtRepository.
				countByRegionCodeAndAssociationAndDeletedAtIsNull("RC001", "A001"))
					.thenReturn(Long.valueOf(15L));
		
		//act & assert
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> districtService.districtCategoryRegi(form, loginUser)
			);
		
		assertThat(exception.getMessage()).isEqualTo("登録上限(15件)に達しています。");
	}
	
	@Test
	void districtCategoryRegi_同じ名前の区カテゴリが存在する場合は例外をスロー() {
		// arrange
		DistrictRegiForm form = new DistrictRegiForm();
		form.setName("重複する区カテゴリ");
		form.setArea("重複するエリア");
		
		// 登録件数問題なし
		org.mockito.Mockito.when(districtRepository.
				countByRegionCodeAndAssociationAndDeletedAtIsNull("RC001", "A001"))
					.thenReturn(Long.valueOf(5L));
		
		// 同名カテゴリが存在する
		Mockito.when(districtRepository
				.findByRegionCodeAndAssociationAndAreaAndDeletedAtIsNullOrderByIdAsc(
						"RC001", "A001", form.getArea()))
					.thenReturn(List.of(new District()));
		
		// act & assert
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> districtService.districtCategoryRegi(form,  loginUser)
		);
		
		assertThat(exception.getMessage()).isEqualTo("既に同じ名前の区が登録されています。");
	}
		
	@Test
	void softDeletedDistrict_正常に区カテゴリをソフトデリートできる() {
		// arrange
		int districtId = 1;
		Mockito.when(userRepository.existsById(loginUser.getId())).thenReturn(true);
		
		District district = new District();
		district.setId(districtId);
		district.setAssociation("A001");
		district.setDeletedAt(null);
		
		Mockito.when(districtRepository.findById(districtId))
				.thenReturn(Optional.of(district));
		
		// act
		districtService.softDeletedDistrict(loginUser, districtId);
		
		// assert
		Mockito.verify(districtRepository).save(Mockito.any(District.class));
	}
	
	@Test
	void softDeletedDistrict_既に削除済みの場合は例外をスロー() {
		// arrange
		int districtId = 1;
		
		Mockito.when(userRepository.existsById(loginUser.getId())).thenReturn(true);
		
		// 削除済みのdistrictを作成
		District deletedDistrict = new District();
		deletedDistrict.softDelete();
		
		// リポジトリのモック設定
		Mockito.when(districtRepository.findById(districtId))
				.thenReturn(Optional.of(deletedDistrict));
		
		// act & assert
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> districtService.softDeletedDistrict(loginUser, districtId)
		);
		
		assertThat(exception.getMessage()).isEqualTo("既に削除済みです");
	}
	
	@Test
	void editDistrict_正常に区名を編集できる() {
		// arange
		int districtId = 2;
		
		// 編集対象のdistrictを作成
		District district = new District();
		district.setId(2);
		district.setName("古い町名");
		district.setRegionCode("RC001");
		district.setAssociation("A001");
		district.setArea("古い区");
		district.setDescription("古い説明文");
		
		// 編集後のフォーム
		DistrictEditForm form = new DistrictEditForm();
		form.setName("新しい町名");
		form.setArea("新しい区名");
		form.setDescription("新しい説明文");
		
		// リポジトリのモック設定
		Mockito.when(userRepository.existsById(loginUser.getId()))
			.thenReturn(true);
		
		Mockito.when(districtRepository.findById(districtId))
			.thenReturn(Optional.of(district));
		
		// act
		districtService.editDistrict(loginUser, districtId, form);
		
		// assert
		Mockito.verify(districtRepository).save(Mockito.any(District.class));
		assertThat(district.getName()).isEqualTo("新しい町名");
		assertThat(district.getArea()).isEqualTo("新しい区名");
		assertThat(district.getDescription()).isEqualTo("新しい説明文");
	}
	
	@Test
	void editDistrict_既に削除済みの場合は例外をスロー() {
		// arange
		int districtId = 2;
		
		// 編集対象のdistrictを作成
		District district = new District();
		district.setId(2);
		district.setName("古い町名");
		district.setRegionCode("RC001");
		district.setAssociation("A001");
		district.setArea("古い説明文");
		district.setDescription("古い説明文");
		
		// 削除済みにする
		district.softDelete();
		
		DistrictEditForm form = new DistrictEditForm();
		form.setName("新しい町名");
		form.setArea("新しい区名");
		form.setDescription("新しい説明文");
		
		// リポジトリのモック設定
		Mockito.when(userRepository.existsById(loginUser.getId()))
			.thenReturn(true);
	
		Mockito.when(districtRepository.findById(districtId))
			.thenReturn(Optional.of(district));
		
		// act & assert
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> districtService.editDistrict(loginUser, districtId, form)
		);
		
		assertThat(exception.getMessage()).isEqualTo("既に削除済みです");
				
	}
}