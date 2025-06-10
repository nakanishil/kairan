package com.example.kairan.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.kairan.entity.District;
import com.example.kairan.entity.User;
import com.example.kairan.form.AdminDistrictRegiForm;
import com.example.kairan.repository.DistrictRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDistrictService {
	
	private final DistrictRepository districtRepository;
	
	private final UserService userService;
	
	// showDistrictListで使用 regionCodeが同じでIdが小さいもの各種１件のみ
	public Page<District> getDistinctDistrictsByAssociation(String regionCode, Pageable pageable) {
		return districtRepository.findDistinctAssociationByRegionCode(regionCode, pageable);
	}
	
	public Optional<District> districtGetfindId(int districtId){
		return districtRepository.findById(districtId);
	}
	
	//districtAssociationでRole_町内会長を取得
	public List<User> getMayors(User loginUser) {
        return userService.getMayors(loginUser);
    }
	
	// regionCode,Associationが同一で町名の昇順 
	public List<District> getDistrictsByAssociationSorted(User loginUser) {
		String association = loginUser.getDistrict().getAssociation();
		String regionCode = loginUser.getDistrict().getRegionCode();
		return districtRepository.findByRegionCodeAndAssociationOrderByNameAsc(
				regionCode, association);
	}
	
	// district登録処理
	public District regiDistrict(AdminDistrictRegiForm form) {
		form.normalize();
		
		List<District> checkDistrict = districtRepository
				.findByAssociationAndDeletedAtIsNull(form.getAssociation());
		
		// 既存のdistrictに登録しようとした町内会名が有った場合
		if(!checkDistrict.isEmpty()) {
			throw new IllegalArgumentException("入力した町内会名は既に存在します");
		}
		
		District district = new District();
		district.setName(form.getName());
		district.setRegionCode(form.getRegioncode());
		district.setAssociation(form.getAssociation());
		district.setArea(form.getArea());
		district.setDescription(form.getDescription());
		
		districtRepository.save(district);
		
		return district;
	}
	
	// 仮ユーザの作成 return kaicholist, kuchoList
	public List<User> casualUserRegi(District district){
		return userService.casualUserRegi(district);
	}
	
	
	
	
}
