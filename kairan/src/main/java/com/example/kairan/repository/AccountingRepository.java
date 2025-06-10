package com.example.kairan.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.kairan.entity.Accounting;
import com.example.kairan.entity.Accounting.Type;
import com.example.kairan.entity.User;

@Repository
public interface AccountingRepository extends JpaRepository<Accounting, Integer> {
	
	//accountingIdで取得 deletedAtがNULL
	Optional <Accounting> findByIdAndDeletedAtIsNull(Integer accountingId);
	
	// recorded_by、categoryが1（町内会費収入）、取引年が今年のもの、ソフトデリートされていないもの取得
	@Query("""
		    SELECT a FROM Accounting a
		    WHERE a.recordedBy.id = :userId
		      AND a.accountingCategory.id = :categoryId
		      AND a.type = :type
		      AND a.transactionDate BETWEEN :start AND :end
		      AND a.deletedAt IS NULL
		""")
		Optional<Accounting> findOneByAllConditions(
		    @Param("userId") Integer userId,
		    @Param("categoryId") Integer categoryId,
		    @Param("type") Accounting.Type type,
		    @Param("start") LocalDateTime start,
		    @Param("end") LocalDateTime end
		);

	
	// districtId(町内会)とdeletedAtがnullを条件に一覧取得
	Page<Accounting> findByDistrictIdAndDeletedAtIsNull(Integer districtId, Pageable pageable);
	
	// districtIdと取引範囲と未削除を条件に取得
	Page<Accounting> findByDistrictIdAndTransactionDateBetweenAndDeletedAtIsNull(
			Integer districtId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	
	// 合計金額を取得するメソッド
	@Query("SELECT SUM(a.amount) FROM Accounting a WHERE a.district.id = :districtId "
			+ "AND a.deletedAt IS NULL AND a.transactionDate BETWEEN :startDate AND :endDate")
	BigDecimal sumAmountByDistrictIdAndTransactionDateBetween(
			@Param("districtId") Integer districtId,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate
	);
	
	// 収入だけの合計
	@Query("SELECT SUM(a.amount) FROM Accounting a WHERE a.district.id = :districtId "
	     + "AND a.deletedAt IS NULL AND a.type = '収入' "
	     + "AND a.transactionDate BETWEEN :startDate AND :endDate")
	BigDecimal sumIncomeByDistrictIdAndTransactionDateBetween(
	    @Param("districtId") Integer districtId,
	    @Param("startDate") LocalDateTime startDate,
	    @Param("endDate") LocalDateTime endDate
	);

	// 支出だけの合計
	@Query("SELECT SUM(a.amount) FROM Accounting a WHERE a.district.id = :districtId "
	     + "AND a.deletedAt IS NULL AND a.type = '支出' "
	     + "AND a.transactionDate BETWEEN :startDate AND :endDate")
	BigDecimal sumExpenseByDistrictIdAndTransactionDateBetween(
	    @Param("districtId") Integer districtId,
	    @Param("startDate") LocalDateTime startDate,
	    @Param("endDate") LocalDateTime endDate
	);

	// districtidと取引範囲と収支タイプと未削除を条件に取得
	Page<Accounting> findByDistrictIdAndTransactionDateBetweenAndDeletedAtIsNullAndType(
			Integer districtId, LocalDateTime startDate, LocalDateTime endDate, Type type, Pageable pageable);
	
	// districtIdと収支タイプと未削除を条件に取得
	Page<Accounting> findByDistrictIdAndDeletedAtIsNullAndType(
			Integer districtId, Type type, Pageable pageable);
	
	// カテゴリで絞り込み（削除されてないもの）
	Page<Accounting> findByDistrictIdAndAccountingCategoryIdAndDeletedAtIsNull(
	    Integer districtId, Integer accountingCategoryId, Pageable pageable
	);

	// 年月＋カテゴリで絞り込み（削除されてないもの）
	Page<Accounting> findByDistrictIdAndTransactionDateBetweenAndAccountingCategoryIdAndDeletedAtIsNull(
	    Integer districtId, LocalDateTime startDate, LocalDateTime endDate, Integer accountingCategoryId, Pageable pageable
	);

	// 収支区分＋カテゴリで絞り込み（削除されてないもの）
	Page<Accounting> findByDistrictIdAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(
	    Integer districtId, com.example.kairan.entity.Accounting.Type type, Integer accountingCategoryId, Pageable pageable
	);

	// 年月＋収支区分＋カテゴリで絞り込み（削除されてないもの）
	Page<Accounting> findByDistrictIdAndTransactionDateBetweenAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(
	    Integer districtId, LocalDateTime startDate, LocalDateTime endDate, com.example.kairan.entity.Accounting.Type type, Integer accountingCategoryId, Pageable pageable
	);
	
// NonPayment用
	// 町内会長用
	//  Assosiationが同じ || accountcategoryidが１の'町内会費収入' || typeが'収入' || 指定した期間の取引していないユーザ
	@Query("""
		    SELECT u FROM User u
		    WHERE u.district.association = :association
		    AND u.enabled = true
		    AND u.role.name <> 'ROLE_行政'
		    AND EXISTS (
		        SELECT mf FROM MembershipFee mf
		        WHERE mf.district.id = u.district.id
		        AND mf.year = :year
		    )
		    AND u.id NOT IN (
		        SELECT a.recordedBy.id FROM Accounting a
		        WHERE a.accountingCategory.id = :categoryId
		        AND a.type = :type
		        AND a.transactionDate BETWEEN :start AND :end
		        AND a.deletedAt IS NULL
		    )
		    ORDER BY u.id ASC
		""")
		Page<User> findNonPayersByAssociationIfMembershipFeeExists(
		    @Param("association") String association,
		    @Param("categoryId") int categoryId,
		    @Param("type") Accounting.Type type,
		    @Param("year") int year,
		    @Param("start") LocalDateTime start,
		    @Param("end") LocalDateTime end,
		    Pageable pageable
		);
	// 区長用
	//  AssosiationとArea同じ || accountcategoryidが１の'町内会費収入' || typeが'収入' || 指定した期間の取引していないユーザ
	@Query("""
		    SELECT u FROM User u
		    WHERE u.district.association = :association
		    AND u.district.area = :area
		    AND u.enabled = true
		    AND u.role.name <> 'ROLE_行政'
		    AND EXISTS (
		        SELECT mf FROM MembershipFee mf
		        WHERE mf.district.id = u.district.id
		        AND mf.year = :year
		    )
		    AND u.id NOT IN (
		        SELECT a.recordedBy.id FROM Accounting a
		        WHERE a.accountingCategory.id = :categoryId
		        AND a.type = :type
		        AND a.district.association = :association
		        AND a.district.area = :area
		        AND a.transactionDate BETWEEN :start AND :end
		        AND a.deletedAt IS NULL
		    )
		    ORDER BY u.id ASC
		""")
		Page<User> findNonPayersByAssociationAreaIfMembershipFeeExists(
		    @Param("association") String association,
		    @Param("categoryId") int categoryId,
		    @Param("type") Accounting.Type type,
		    @Param("area") String area,
		    @Param("year") int year,
		    @Param("start") LocalDateTime start,
		    @Param("end") LocalDateTime end,
		    Pageable pageable
		);



//	
	
// csv用
	// districtId、取引日範囲、type、categoryIdで絞り込み
	List<Accounting> findByDistrictIdAndTransactionDateBetweenAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(
	    Integer districtId, LocalDateTime startDate, LocalDateTime endDate, Accounting.Type type, Integer categoryId);

	// districtId、取引日範囲、typeで絞り込み
	List<Accounting> findByDistrictIdAndTransactionDateBetweenAndTypeAndDeletedAtIsNull(
	    Integer districtId, LocalDateTime startDate, LocalDateTime endDate, Accounting.Type type);

	// districtId、取引日範囲、categoryIdで絞り込み
	List<Accounting> findByDistrictIdAndTransactionDateBetweenAndAccountingCategoryIdAndDeletedAtIsNull(
	    Integer districtId, LocalDateTime startDate, LocalDateTime endDate, Integer categoryId);

	// districtId、取引日範囲だけで絞り込み
	List<Accounting> findByDistrictIdAndTransactionDateBetweenAndDeletedAtIsNull(
	    Integer districtId, LocalDateTime startDate, LocalDateTime endDate);

	// districtId、typeとcategoryIdで絞り込み
	List<Accounting> findByDistrictIdAndTypeAndAccountingCategoryIdAndDeletedAtIsNull(
	    Integer districtId, Accounting.Type type, Integer categoryId);

	// districtId、typeだけで絞り込み
	List<Accounting> findByDistrictIdAndTypeAndDeletedAtIsNull(
	    Integer districtId, Accounting.Type type);

	// districtId、categoryIdだけで絞り込み
	List<Accounting> findByDistrictIdAndAccountingCategoryIdAndDeletedAtIsNull(
	    Integer districtId, Integer categoryId);

	// districtIdだけで絞り込み
	List<Accounting> findByDistrictIdAndDeletedAtIsNull(
	    Integer districtId);




}
