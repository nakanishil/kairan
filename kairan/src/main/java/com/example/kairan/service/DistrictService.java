package com.example.kairan.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.kairan.entity.District;
import com.example.kairan.entity.User;
import com.example.kairan.form.DistrictEditForm;
import com.example.kairan.form.DistrictRegiForm;
import com.example.kairan.repository.DistrictRepository;
import com.example.kairan.repository.RoleRepository;
import com.example.kairan.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistrictService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final DistrictRepository districtRepository;

	// 同市同町内(regionCode, districtAssociation, area)ソフトデリートを除くname昇順を検索
	public List<District> sameRegionCodeAssociationArea(User loginUser) {
		String regionCode = loginUser.getDistrict().getRegionCode();
		String association = loginUser.getDistrict().getAssociation();
		return districtRepository.findByRegionCodeAndAssociationWithJapaneseOrder
				(regionCode, association);
	}

	// 閲覧権限 町内会長
	public boolean permissions(User loginUser) {
		return "ROLE_町内会長".equals(loginUser.getRole().getName());
	}

	// 区カテゴリ登録
	@Transactional
	public void districtCategoryRegi(DistrictRegiForm form, User loginUser) {

		// ログイン者から抽出
		String userRegionCode = loginUser.getDistrict().getRegionCode();
		String userAssociation = loginUser.getDistrict().getAssociation();

		// フォームから抽出
		String formName = form.getName();
		String formArea = form.getArea();
		String formDescription = form.getDescription();

		// 登録上限チェック
		long activeCount = 
				districtRepository.countByRegionCodeAndAssociationAndDeletedAtIsNull(
						userRegionCode, userAssociation);
		if (activeCount >= 15) {
			throw new IllegalArgumentException("登録上限(15件)に達しています。");
		}

		// form中身チェック
		if(formName == null || formName.isEmpty()) {
			throw new IllegalArgumentException("formが空の状態でdistrictRegiが起動");
		}

		// 重複チェック assosiationが同じかつareaが同じ場合エラー
		if(sameRegionCodeAssociationAreaCheck(userRegionCode, userAssociation, formArea)) {
			throw new IllegalArgumentException("既に同じ名前の区が登録されています。");
		}

		// district登録
		District district = new District();
		district.setName(formName);
		district.setRegionCode(userRegionCode);
		district.setAssociation(userAssociation);
		district.setArea(formArea);
		district.setDescription(formDescription);

		districtRepository.save(district);

	}

	// 重複チェック用 assosiationが同じかつareaが同じ場合エラー
	public boolean sameRegionCodeAssociationAreaCheck(String regionCode, String association, String area){
		List<District> districtList = 
				districtRepository.findByRegionCodeAndAssociationAndAreaAndDeletedAtIsNullOrderByIdAsc(
						regionCode, association, area);
		return !districtList.isEmpty();
	}

	// districtカテゴリソフトデリート
	public void softDeletedDistrict(User loginUser, int districtId) {

		// ログインユーザの確認
		if(!userRepository.existsById(loginUser.getId())) {
			throw new IllegalArgumentException("ログインユーザが見つかりません");
		}

		District district = districtRepository.findById(districtId)
				.orElseThrow(() -> new IllegalArgumentException("対象の区が見つかりません"));

		// すでに削除済みかチェック
		if(district.isDeleted()) {
			throw new IllegalStateException("既に削除済みです");
		}

		// 対象区の町内会に所属しているか確認
		String loginUserAssociation = loginUser.getDistrict().getAssociation();
		String districtAssociation = district.getAssociation();
		if(!loginUserAssociation.equals(districtAssociation)) {
			throw new IllegalArgumentException("指定した区の町内会が異なります。");
		}

		// 削除処理
		district.softDelete();
		// softdelete()でエンティティのdeletedatを更新し、@transactionalがついている場合はsave()しなくてもいいらしい
		districtRepository.save(district);

	}

	// districtを編集
	@Transactional
	public void editDistrict(User loginUser, int districtId, DistrictEditForm form) {

		// ログインユーザの確認
		if(!userRepository.existsById(loginUser.getId())) {
			throw new IllegalArgumentException("ログインユーザが見つかりません");
		}

		District district = districtRepository.findById(districtId)
				.orElseThrow(() -> new IllegalArgumentException("対象の委員区分が見つかりません"));

		// すでに削除済みかチェック
		if(district.isDeleted()) {
			throw new IllegalStateException("既に削除済みです");
		}
		
		

		// 対象区の町内会に所属しているか確認
		String loginUserAssociation = loginUser.getDistrict().getAssociation();
		String districtAssociation = district.getAssociation();
		if(!loginUserAssociation.equals(districtAssociation)) {
			throw new IllegalArgumentException("指定した区の町内会が異なります。");
		}
		
		district.setId(districtId);
		district.setName(form.getName());
		district.setArea(form.getArea());
		district.setDescription(form.getDescription());
		
		/* onUpdateで更新日を更新し、@transactionalがついている場合はsave()しなくてもいい
		   明示的に表示 */  
		districtRepository.save(district);
		
	}
	
	//districtIdで検索 Optionalからdistrictへ
	public District findByIdAndDeletedAtIsNull(int districtId) {
		return districtRepository.findByIdAndDeletedAtIsNull(districtId)
				.orElseThrow(() -> new IllegalArgumentException("指定した区が存在しません。"));
	}
	
	
}	