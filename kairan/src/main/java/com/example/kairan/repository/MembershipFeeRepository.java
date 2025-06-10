package com.example.kairan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kairan.entity.District;
import com.example.kairan.entity.MembershipFee;

@Repository
public interface MembershipFeeRepository extends JpaRepository<MembershipFee, Integer> {

    // 特定の町内会と年で、まだ削除されていない年会費データを取得
    Optional<MembershipFee> findByDistrictIdAndYearAndDeletedAtIsNull(int districtId, int year);
    
    // Idで、まだ削除されていない年会費データを取得
    Optional<MembershipFee> findByIdAndDeletedAtIsNull(int id);
    
    // DistrictIdから金額設定履歴を取得
    Page<MembershipFee> findByDistrictAndDeletedAtIsNull(District district, Pageable pageable);
    
    List<MembershipFee> findByDistrictAssociationAndDeletedAtIsNull(String association);

}
