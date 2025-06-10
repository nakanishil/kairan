package com.example.kairan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kairan.entity.CommitteeClassification;

@Repository
public interface CommitteeClassificationRepository extends JpaRepository<CommitteeClassification, Integer>{
	// districtId毎の委員区分を取得
	List<CommitteeClassification> findByDistrictId(Integer districtId);
	
	/* 
	 * 同町内会districtId.districtAssociationかつ、
	 * ソフトデリートnullかつ、
	 * 委員区分テーブルId昇順
	 */
	List<CommitteeClassification> findByDistrictIdAndDistrictAssociationAndDeletedAtIsNullOrderByIdAsc(
			int districtId, String association);
	
	// 同町内会districtId.districtAssociationでかつソフトデリートされていない
	boolean existsByDistrictRegionCodeAndDistrictAssociationAndNameAndDeletedAtIsNull(String regionCode, String association, String name);

	// deleted_atがnullでdistrictIdのカウント
	long countByDistrictIdAndDeletedAtIsNull(int districtId);
	
	

}
