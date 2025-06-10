package com.example.kairan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.kairan.entity.District;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer>{

	// 町名で検索
	Optional<District> findByName(String name);
	
	// 町名で検索
	List<District> findByAssociationAndDeletedAtIsNull(String association);

	// 行政コードで検索
	Optional<District> findByRegionCode(String regionCode);

	// Idで検索
	Optional<District> findByIdAndDeletedAtIsNull(int districtId);

	// 町内会名で検索
	List<District> findByAssociation(String association);

	// 同市同町内(regionCode, districtAssociation)ソフトデリートを除くのを検索
	List<District> findByRegionCodeAndAssociationAndDeletedAtIsNullOrderByIdAsc(String regionCode, String association);

	// 同市同町内(regionCode, districtAssociation, area)ソフトデリートを除くID昇順を検索
	List<District> findByRegionCodeAndAssociationAndAreaAndDeletedAtIsNullOrderByIdAsc(String regionCode, String association, String area);

	// 登録上限設定のため deleted_atがnullでRegionCode,Associationが同一なものをカウント
	long countByRegionCodeAndAssociationAndDeletedAtIsNull(String regionCode, String Association);

	// 同市同町内(regionCode, districtAssociation)ソフトデリートを除くのをName昇順
	@Query(value = "SELECT * FROM districts WHERE region_code = :regionCode AND association = :association AND deleted_at IS NULL ORDER BY name COLLATE utf8mb4_ja_0900_as_cs ASC", nativeQuery = true)
	List<District> findByRegionCodeAndAssociationWithJapaneseOrder(
			@Param("regionCode") String regionCode,
			@Param("association") String association
			);

	// RegionCode,Association,nameが同一かつソフトデリートされていない
	boolean existsByRegionCodeAndAssociationAndNameAndDeletedAtIsNull(String regionCode, String association, String name);

	// regionCodeからソフトデリートされていないdistrictを取得
	Page<District> findByRegionCodeAndDeletedAtIsNull(String regionCode, Pageable pageable);

	// regionCodeが同じでIdが小さいもの各種１件のみ association の抽出なので重複を避けるため
	@Query("""
			    SELECT d FROM District d
			    WHERE d.regionCode = :regionCode
			      AND d.deletedAt IS NULL
			      AND d.id IN (
			          SELECT MIN(d2.id) FROM District d2
			          WHERE d2.regionCode = :regionCode AND d2.deletedAt IS NULL
			          GROUP BY d2.association
			      )
			    ORDER BY d.association ASC
			""")
	Page<District> findDistinctAssociationByRegionCode(@Param("regionCode") String regionCode, Pageable pageable);
	
	// regionCode,Associationが同一で町名の昇順 
	@Query("""
		    SELECT d FROM District d
		    WHERE d.regionCode = :regionCode
		      AND d.association = :association
		      AND d.deletedAt IS NULL
		    ORDER BY d.name ASC
		""")
		List<District> findByRegionCodeAndAssociationOrderByNameAsc(
		    @Param("regionCode") String regionCode,
		    @Param("association") String association
		);


}
