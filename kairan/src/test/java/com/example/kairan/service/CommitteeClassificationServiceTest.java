package com.example.kairan.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.example.kairan.entity.CommitteeClassification;
import com.example.kairan.entity.District;
import com.example.kairan.entity.User;
import com.example.kairan.form.CommitteeRegiForm;
import com.example.kairan.repository.CommitteeClassificationRepository;
import com.example.kairan.repository.UserRepository;
import com.example.kairan.service.CommitteeClassificationService;

public class CommitteeClassificationServiceTest {
	@InjectMocks
	private CommitteeClassificationService committeeClassificationService;
	
	@Mock
	private CommitteeClassificationRepository committeeClassificationRepository;
	
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
		
		loginUser = new User();
		loginUser.setDistrict(district);
		loginUser.setId(1);
	}
	
	@Test
	void committeeCategoryRegi_委員カテゴリを正常に登録できる() {
		// arrange
		CommitteeRegiForm form = new CommitteeRegiForm();
		form.setName("新しい委員カテゴリ");
		
		// まだ10件未満
		org.mockito.Mockito.when(committeeClassificationRepository.countByDistrictIdAndDeletedAtIsNull(1)).thenReturn(Long.valueOf(5L));
		
		// 同名カテゴリなし
		org.mockito.Mockito.when(
				committeeClassificationRepository.existsByDistrictRegionCodeAndDistrictAssociationAndNameAndDeletedAtIsNull(
						"RC001", "A001", "新しい委員カテゴリ"
						)
				).thenReturn(false);
		
		// act
		committeeClassificationService.committeeCategoryRegi(form, loginUser);
		
		// Assert
		org.mockito.Mockito.verify(committeeClassificationRepository).save(org.mockito.Mockito.any(CommitteeClassification.class));
	}
	
	@Test
	void committeeCategoryRegi_登録上限を超えたら例外をスロー() {
		// arrange
		CommitteeRegiForm form = new CommitteeRegiForm();
		form.setName("新しい委員カテゴリ");
		
		// 登録上限を超えている(10件以上)
		org.mockito.Mockito.when(committeeClassificationRepository.countByDistrictIdAndDeletedAtIsNull(1))
			.thenReturn(Long.valueOf(10L));
		
		// act & assert
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> committeeClassificationService.committeeCategoryRegi(form, loginUser)
			);
		
		assertThat(exception.getMessage()).isEqualTo("登録上限(10件)に達しています。");
				
	}
	
	@Test
	void committeeCategoryRegi_同じ名前の委員カテゴリが既に存在する場合は例外をスローする() {
		// arrange
		CommitteeRegiForm form = new CommitteeRegiForm();
		form.setName("重複する委員カテゴリ");
		
		// 登録件数は問題なし(5件)
		Mockito.when(committeeClassificationRepository.countByDistrictIdAndDeletedAtIsNull(1))
			.thenReturn(Long.valueOf(5L));
		
		// 同名カテゴリが存在する
		Mockito.when(committeeClassificationRepository
				.existsByDistrictRegionCodeAndDistrictAssociationAndNameAndDeletedAtIsNull(
						"RC001", "A001", "重複する委員カテゴリ"))
				.thenReturn(true);
		
		// act & assert
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> committeeClassificationService.committeeCategoryRegi(form,  loginUser)
		);
		
		assertThat(exception.getMessage()).isEqualTo("既に同じ名前の委員カテゴリが登録されています");
		
	}
	
	@Test
	void softDeletedCommittee_正常に委員カテゴリをソフトデリートできる() {
		// arrange
		int committeeId = 1;
		
		// テスト用のCommitteeClassificationを作成
		CommitteeClassification committee = new CommitteeClassification();
		District committeeDistrict = new District();
		committeeDistrict.setAssociation("A001");
		committee.setDistrict(committeeDistrict);
		
		// 削除前なのでdeletedAtはnull想定(deleted= false)
		Mockito.when(committeeClassificationRepository.findById(committeeId))
			.thenReturn(Optional.of(committee));
		
		// ログインユーザは存在している想定
		Mockito.when(userRepository.existsById(loginUser.getId())).thenReturn(true);
		
		// loginUserのdistrictも設定済みなのでOk(setupメソッドで)
		
		// act
		committeeClassificationService.softDeletedCommittee(loginUser, committeeId);
		
		// assert
		Mockito.verify(committeeClassificationRepository).save(Mockito.any(CommitteeClassification.class));
		
	}
	
	@Test
	void softDeletedCommittee_既に削除済みの場合は例外をスロー() {
		// arrange
		int committeeId = 1;
		
		// 削除済みのCommitteeClassificationを作成
		CommitteeClassification committee = new CommitteeClassification();
		District committeeDistrict = new District();
		committeeDistrict.setAssociation("A001");
		committee.setDistrict(committeeDistrict);
		
		// 削除済み状態にする
		committee.softDelete();
		
		// リポジトリのモック設定
		Mockito.when(committeeClassificationRepository.findById(committeeId))
			.thenReturn(Optional.of(committee));
		
		Mockito.when(userRepository.existsById(loginUser.getId())).thenReturn(true);
		
		// act & assert
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> committeeClassificationService.softDeletedCommittee(loginUser, committeeId)
		);
		
		assertThat(exception.getMessage()).isEqualTo("既に削除済みです");
				
	}
	
	@Test
	void editCommittee_正常に委員名を編集できる() {
		// arrange
		int committeeId = 1;
		
		// 編集対象のCommitteeClassificationを作成
		CommitteeClassification committee = new CommitteeClassification();
		District committeeDistrict = new District();
		committeeDistrict.setAssociation("A001");
		committee.setDistrict(committeeDistrict);
		
		// 編集前の名前をセットしておく
		committee.setName("古い委員名");
		
		// 編集後の名前を持つフォーム
		CommitteeRegiForm form = new CommitteeRegiForm();
		form.setName("新しい委員名");
		
		// リポジトリのモック設定
		Mockito.when(committeeClassificationRepository.findById(committeeId))
			.thenReturn(Optional.of(committee));
		
		Mockito.when(userRepository.existsById(loginUser.getId()))
			.thenReturn(true);
		
		// act
		committeeClassificationService.editCommittee(loginUser, committeeId, form);
		
		// assert
		Mockito.verify(committeeClassificationRepository).save(Mockito.any(CommitteeClassification.class));
		assertThat(committee.getName()).isEqualTo("新しい委員名");
		
	}
	
	@Test
	void editCommittee_既に削除済みの場合は例外をスロー() {
		// arrange
		int committeeId = 1;
		
		// 削除済みのCommitteeClassificationを作成
		CommitteeClassification committee = new CommitteeClassification();
		District committeeDistrict = new District();
		committeeDistrict.setAssociation("A001");
		committee.setDistrict(committeeDistrict);
		
		// 削除済み状態にする
		committee.softDelete();
		
		// 編集後の名前を持つフォーム
		CommitteeRegiForm form = new CommitteeRegiForm();
		form.setName("新しい委員名");
		
		// リポジトリのモック設定
		Mockito.when(committeeClassificationRepository.findById(committeeId))
			.thenReturn(Optional.of(committee));
		
		Mockito.when(userRepository.existsById(loginUser.getId()))
	    	.thenReturn(true);

			
		// act & assert
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> committeeClassificationService.editCommittee(loginUser,  committeeId,  form)
		);
		
		assertThat(exception.getMessage()).isEqualTo("既に削除済みです");
		
			
	}


}
