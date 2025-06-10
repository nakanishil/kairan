package com.example.kairan.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.kairan.entity.Circular;

@Repository
public interface CircularRepository extends JpaRepository<Circular, Integer>{
	// 削除されていない回覧板を取得
	List<Circular> findByDeletedAtIsNull();
	
	// 特定ユーザが作成した回覧板を取得
	List<Circular> findByAuthorId(Integer authorId);
	
//	// 最新の回覧板を取得(削除されていないものを降順で) *
//	@Query("SELECT c FROM Circular c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
//	List<Circular> findLatestCirculars(Pageable pageable);
	
	// DistrictAssociationが同一かつ、ソフトデリートされていないかつ投稿日降順で回覧板を取得
	@Query("SELECT c FROM Circular c WHERE c.deletedAt IS NULL AND c.district.association = :association ORDER BY c.createdAt DESC")
	Page<Circular> findByAssociationAndNotDeleted(@Param("association") String association, Pageable pageable);

	// DistrictAssociationが同一かつ、ソフトデリートされていないかつ投稿日降順で回覧板を取得
	@Query("SELECT c FROM Circular c WHERE c.deletedAt IS NULL AND c.district.association = :association ORDER BY c.createdAt DESC")
	List<Circular> findByAssociationAndNotDeletedList(@Param("association") String association, Pageable pageable);

}
