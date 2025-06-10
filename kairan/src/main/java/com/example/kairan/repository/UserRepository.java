package com.example.kairan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.kairan.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
	
	// userIdの重複チェック用
    boolean existsByUserId(String userId);

    // emailの重複チェック用
    boolean existsByEmail(String email);
    
	// メールアドレスでユーザを検索
	Optional<User> findByEmail(String email);

	// ユーザIDで検索
	Optional<User> findByUserId(String userId);

	// ログイン処理で無効ユーザを除外
	Optional<User> findByUserIdAndEnabledTrue(String userId);

	// 役職ごとにユーザを取得
	List<User> findByRoleId(Integer roleId);

	// 委員ごとにユーザを取得
	List<User> findByCommittee_Id(Integer committeeId);

	// 町内会毎のユーザを取得
	List<User> findByDistrictId(Integer districtId);

	// 町内会毎の役職者を取得
	List<User> findByRoleIdAndDistrictId(Integer roleId, Integer districtId);


	// 「役職IDの昇順 → ふりがな（五十音）の昇順」で並び替え
	@Query("""
		    SELECT u FROM User u
		    WHERE u.district.association = :association
		      AND u.enabled = true
		      AND u.deletedAt IS NULL
		    ORDER BY u.role.id ASC, u.furigana ASC
		""")
		Page<User> findByDistrictAssociationAndEnabledTrueAndDeletedAtIsNull(
		    @Param("association") String association, Pageable pageable);
	
	// 「役職IDの昇順 → ふりがな（五十音）の昇順」で並び替え 行政を除く
	@Query("""
		    SELECT u FROM User u
		    WHERE u.district.association = :association
		      AND u.district.area = :area
		      AND u.enabled = true
		      AND u.deletedAt IS NULL
		      AND u.role.name <> 'ROLE_行政'
		      AND (:nameKeyword IS NULL OR u.name LIKE CONCAT('%', :nameKeyword, '%') OR u.furigana LIKE CONCAT('%', :nameKeyword, '%'))
		      AND (:roleId IS NULL OR u.role.id = :roleId)
		      AND (:committeeId IS NULL OR u.committee.id = :committeeId)
		    ORDER BY u.district.area ASC, u.role.id ASC, u.furigana ASC
		""")
		Page<User> searchByConditions(
		    @Param("association") String association,
		    @Param("area") String area,
		    @Param("nameKeyword") String nameKeyword,
		    @Param("roleId") Integer roleId,
		    @Param("committeeId") Integer committeeId,
		    Pageable pageable
		);


	
	// 「役職IDの昇順 → ふりがな（五十音）の昇順」で並び替え List
	@Query("""
		    SELECT u FROM User u
		    WHERE u.district.association = :association
		      AND u.district.area = :area
		      AND u.enabled = true
		      AND u.deletedAt IS NULL
		      AND u.role.name <> 'ROLE_行政'
		      AND (:nameKeyword IS NULL OR u.name LIKE CONCAT('%', :nameKeyword, '%') OR u.furigana LIKE CONCAT('%', :nameKeyword, '%'))
		      AND (:roleId IS NULL OR u.role.id = :roleId)
		      AND (:committeeId IS NULL OR u.committee.id = :committeeId)
		    ORDER BY u.role.id ASC, u.furigana ASC
		""")
		List<User> searchByConditionsForCsv(
		    @Param("association") String association,
		    @Param("area") String area,
		    @Param("nameKeyword") String nameKeyword,
		    @Param("roleId") Integer roleId,
		    @Param("committeeId") Integer committeeId
		);


	
	// 「役職IDの昇順 → ふりがな（五十音）の昇順」で並び替え List
	@Query("""
		    SELECT u FROM User u
		    WHERE u.district.area = :area
		      AND u.enabled = true
		      AND u.deletedAt IS NULL
		      AND (:nameKeyword IS NULL OR u.name LIKE CONCAT('%', :nameKeyword, '%') OR u.furigana LIKE CONCAT('%', :nameKeyword, '%'))
		      AND (:roleId IS NULL OR u.role.id = :roleId)
		      AND (:committeeId IS NULL OR u.committee.id = :committeeId)
		    ORDER BY u.role.id ASC, u.furigana ASC
		""")
		List<User> searchByAreaConditionsForCsv(
		    @Param("area") String area,
		    @Param("nameKeyword") String nameKeyword,
		    @Param("roleId") Integer roleId,
		    @Param("committeeId") Integer committeeId
		);
		
		//「役職IDの昇順 → ふりがな（五十音）の昇順」で並び替え Page
		@Query("""
		    SELECT u FROM User u
		    WHERE u.district.area = :area
		      AND u.enabled = true
		      AND u.deletedAt IS NULL
		      AND (:nameKeyword IS NULL OR u.name LIKE CONCAT('%', :nameKeyword, '%') OR u.furigana LIKE CONCAT('%', :nameKeyword, '%'))
		      AND (:roleId IS NULL OR u.role.id = :roleId)
		      AND (:committeeId IS NULL OR u.committee.id = :committeeId)
		    ORDER BY u.role.id ASC, u.furigana ASC
		""")
		Page<User> searchByAreaConditions(
		    @Param("area") String area,
		    @Param("nameKeyword") String nameKeyword,
		    @Param("roleId") Integer roleId,
		    @Param("committeeId") Integer committeeId,
		    Pageable pageable
		);
		
		// 町内会が同じ、区同じ、フリガナ昇順、行政を除く
		@Query("""
			    SELECT u FROM User u
			    WHERE u.district.association = :association
			      AND u.district.area = :area
			      AND u.enabled = true
			      AND u.deletedAt IS NULL
			      AND u.role.name <> 'ROLE_行政'
			    ORDER BY u.furigana ASC
			""")
			Page<User> findByDistrictAssociationAndDistrictAreaAndEnabledTrueAndDeletedAtIsNull(
			    @Param("association") String association,
			    @Param("area") String area,
			    Pageable pageable
			);

		
		@Query("""
			    SELECT u FROM User u
			    WHERE u.district.association = :association
			      AND u.district.area = :area
			      AND u.enabled = true
			      AND u.deletedAt IS NULL
			    ORDER BY u.role.id ASC, u.furigana ASC
			""")
			Page<User> findByDistrictAssociationAndAreaAndEnabledTrueAndDeletedAtIsNull(
			    @Param("association") String association,
			    @Param("area") String area,
			    Pageable pageable
			);
		
		List<User> findByDistrictAssociationAndDistrictAreaAndEnabledTrueAndDeletedAtIsNull(
			    String association, String area, Sort sort);
		
		// 閲覧ユーザと町内化association,区area,委員会committeeclassificationが同じ
		@Query("""
			    SELECT u FROM User u
			    WHERE u.district.association = :association
			      AND u.district.area = :area
			      AND u.committee.id = :committeeId
			      AND u.enabled = true
			      AND u.deletedAt IS NULL
			      AND u.role.name <> 'ROLE_行政'
			    ORDER BY u.role.id ASC, u.furigana ASC
			""")
			Page<User> findByDistrictAssociationAndDistrictAreaAndCommitteeIdAndEnabledTrueAndDeletedAtIsNull(
			    @Param("association") String association,
			    @Param("area") String area,
			    @Param("committeeId") int committeeId,
			    Pageable pageable
			);

		
		// 町内会同じ、区同じ、委員同じ
		@Query("""
			    SELECT u FROM User u
			    WHERE u.district.association = :association
			      AND u.district.area = :area
			      AND u.committee.id = :committeeId
			      AND u.enabled = true
			      AND u.deletedAt IS NULL
			      AND u.role.name <> 'ROLE_行政'
			    ORDER BY u.role.id ASC, u.furigana ASC
			""")
			List<User> findNonAdminUsersInSameCommittee(
			    @Param("association") String association,
			    @Param("area") String area,
			    @Param("committeeId") int committeeId,
			    Sort sort
			);

		
		Optional<User> findByIdAndDeletedAtIsNullAndEnabledTrue(Integer id);

		// district(regionCode,association)が同じ町内会長を取得
		@Query("""
			    SELECT u FROM User u
			    WHERE u.district.association = :association
			      AND u.district.regionCode = :regionCode
			      AND u.role.name = 'ROLE_町内会長'
			      AND u.enabled = true
			      AND u.deletedAt IS NULL
			""")
			List<User> findMayorsByAssociationAndRegionCode(
			    @Param("association") String association,
			    @Param("regionCode") String regionCode
			);
		
		// Google認証
		Optional<User> findByGoogleId(String googleId);




}
