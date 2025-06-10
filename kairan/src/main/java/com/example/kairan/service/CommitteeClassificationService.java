package com.example.kairan.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kairan.entity.CommitteeClassification;
import com.example.kairan.entity.District;
import com.example.kairan.entity.User;
import com.example.kairan.form.CommitteeRegiForm;
import com.example.kairan.repository.CommitteeClassificationRepository;
import com.example.kairan.repository.DistrictRepository;
import com.example.kairan.repository.RoleRepository;
import com.example.kairan.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
// 必要な引数だけのコンストラクタを自動生成してくれる finalだけ
@RequiredArgsConstructor
public class CommitteeClassificationService {
	private final UserRepository userRepository;
	private final CommitteeClassificationRepository committeeClassificationRepository;
	private final RoleRepository roleRepository;
	private final DistrictRepository districtRepository;
	
	// 同ditrictId,association,ソフトデリートnull 委員Id昇順
	public List<CommitteeClassification> getCommitteeSameDistrictIdAssosiationAsk(int districtId, String association){
		return committeeClassificationRepository.findByDistrictIdAndDistrictAssociationAndDeletedAtIsNullOrderByIdAsc(
				districtId, association);
		
	}
	
	// 閲覧権限 町内会長
	public boolean permissions(User loginUser) {
		return "ROLE_町内会長".equals(loginUser.getRole().getName());
	}
	
	// 委員カテゴリ登録
	@Transactional
	public void committeeCategoryRegi(CommitteeRegiForm form, User loginUser) {
		
		int districtId = loginUser.getDistrict().getId();
		
		// 登録上限チェック
		long activeCount = committeeClassificationRepository.countByDistrictIdAndDeletedAtIsNull(districtId);
		
		if (activeCount >= 10) {
			throw new IllegalArgumentException("登録上限(10件)に達しています。");
		}
		
		String formName = form.getName();
		//formの中身チェック
		if(formName == null || formName.isEmpty()) {
			throw new IllegalArgumentException("formが空の状態でcommitteeCategoryRegiが起動");
		}
		
		// 委員カテゴリの重複チェック
		String loginUserAssociation = loginUser.getDistrict().getAssociation();
		String loginUserRegionCode = loginUser.getDistrict().getRegionCode();
		
		boolean exists = committeeClassificationRepository
			    .existsByDistrictRegionCodeAndDistrictAssociationAndNameAndDeletedAtIsNull(
			        loginUserRegionCode, loginUserAssociation, formName);

			if (exists) {
			    throw new IllegalArgumentException("既に同じ名前の委員カテゴリが登録されています");
			}
		
		// 委員カテゴリ登録
		District district = loginUser.getDistrict();
		CommitteeClassification committee = new CommitteeClassification();
		committee.setName(formName);
		committee.setDistrict(district);
		committeeClassificationRepository.save(committee);
	}
	
	
	// 委員カテゴリソフトデリート
	@Transactional
	public void softDeletedCommittee(User loginUser, int committeeId) {
		
		
		// ログインユーザの確認
		if(!userRepository.existsById(loginUser.getId())) {
			throw new IllegalArgumentException("ログインユーザが見つかりません");
		}
		
		CommitteeClassification committee = committeeClassificationRepository.findById(committeeId)
				.orElseThrow(() -> new IllegalArgumentException("対象の委員区分が見つかりません"));
		
		// すでに削除済みかチェック
		if(committee.isDeleted()) {
			throw new IllegalStateException("既に削除済みです");
		}
		
		// 対象委員の町内会に所属しているか確認
		String loginUserAssociation = loginUser.getDistrict().getAssociation();
		String committeeAssociation = committee.getDistrict().getAssociation();
		if(!loginUserAssociation.equals(committeeAssociation)) {
			throw new IllegalArgumentException("指定した委員の町内会が異なります。");
		}
		
		// 削除処理
		committee.softDelete();
		// softdelete()でエンティティのdeletedatを更新し、@transactionalがついている場合はsave()しなくてもいいらしい
		committeeClassificationRepository.save(committee);
		
	}
	
	// 委員名編集
	@Transactional
	public void editCommittee(User loginUser, int committeeId, CommitteeRegiForm form) {
		
		// ログインユーザの確認
		if(!userRepository.existsById(loginUser.getId())) {
			throw new IllegalArgumentException("ログインユーザが見つかりません");
		}
		
		CommitteeClassification committee = committeeClassificationRepository.findById(committeeId)
				.orElseThrow(() -> new IllegalArgumentException("対象の委員区分が見つかりません"));
		
		// すでに削除済みかチェック
		if(committee.isDeleted()) {
			throw new IllegalStateException("既に削除済みです");
		}
		
		// 対象委員の町内会に所属しているか確認
		String loginUserAssociation = loginUser.getDistrict().getAssociation();
		String committeeAssociation = committee.getDistrict().getAssociation();
		if(!loginUserAssociation.equals(committeeAssociation)) {
			throw new IllegalArgumentException("指定した委員の町内会が異なります。");
		}
		
		String newName = form.getName();
		
		committee.setName(newName);
		/* onUpdateで更新日を更新し、@transactionalがついている場合はsave()しなくてもいい
		   明示的に表示 */  
		committeeClassificationRepository.save(committee);
		
	}
	
	

}
