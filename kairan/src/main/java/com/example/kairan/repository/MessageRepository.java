package com.example.kairan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.kairan.entity.BoardType;
import com.example.kairan.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Integer>{
	List<Message> findByBoardTypeAndParentIdIsNullAndDeletedAtIsNull(BoardType boardType);

	List<Message> findByParentIdAndDeletedAtIsNull(int parentId);
	
	Optional<Message> findById(int messageId);
	
	List<Message> findByBoardTypeId(int boadTypeId);
	
	// 〇〇掲示板 + committee_id + district_idでスレッド取得（委員長・委員用）
	@Query("SELECT m FROM Message m WHERE m.boardType.id = :boardTypeId " +
		       "AND (:committeeId IS NULL OR m.committeeType IS NULL OR m.committeeType.id = :committeeId) " +
		       "AND m.user.district.id = :districtId " +
		       "AND m.deletedAt IS NULL") 
	List<Message> findByBoardTypeIdAndCommitteeIdAndDistrictId(
			@Param("boardTypeId") int boardTypeId,
			@Param("committeeId") Integer committeeId,
			@Param("districtId") Integer districtId
	);
	
	// 〇〇掲示板 + district_idでスレッド取得（区長・会員用）
	@Query("SELECT m FROM Message m WHERE m.boardType.id = :boardTypeId " +
		       "AND m.user.district.id = :districtId " +
		       "AND m.deletedAt IS NULL") 
	List<Message> findByBoardTypeIdAndDistrictId(
			@Param("boardTypeId") int boardTypeId,
			@Param("districtId") Integer districtId
	);
	
    // 掲示板の種類、委員区分、町内会、ソフトデリートされていない更新日降順
	Page<Message> findByBoardTypeIdAndCommitteeTypeIdAndUserDistrictAssociationAndDeletedAtIsNullAndParentIdIsNullOrderByUpdatedAtDesc(
	        int boardTypeId, Integer committeeId, String association, Pageable pageable);

	// 掲示板の種類、町内会、ソフトデリートされていない更新日降順
    Page<Message> findByBoardTypeIdAndUserDistrictAssociationAndDeletedAtIsNullAndParentIdIsNullOrderByUpdatedAtDesc(
            int boardTypeId, String association, Pageable pageable);
	
    Page<Message> findByParentIdAndDeletedAtIsNullOrderByUpdatedAtDesc(
            int parentId, Pageable pageable);
}
