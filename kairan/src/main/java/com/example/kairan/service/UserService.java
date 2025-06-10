package com.example.kairan.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kairan.entity.CommitteeClassification;
import com.example.kairan.entity.District;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.event.UserRegistrationEvent;
import com.example.kairan.form.UserEditForm;
import com.example.kairan.form.UserRoleCommitteeUpdateForm;
import com.example.kairan.repository.CommitteeClassificationRepository;
import com.example.kairan.repository.DistrictRepository;
import com.example.kairan.repository.RoleRepository;
import com.example.kairan.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final DistrictRepository districtRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final RoleRepository roleRepository;
	private final CommitteeClassificationRepository committeeClassificationRepository;
	private final PasswordEncoder passwordEncoder;


	// メールアドレスでユーザ情報を取得
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	// IDでユーザ情報を取得
	public User findById(int userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("ユーザが見つかりません"));
	}

	// 区長が会員を登録
	@Transactional
	public void saveUser(User user,int districtId) {
		System.out.println("🔥 Debug: saveUser() メソッド実行");
		System.out.println("保存するユーザー: " + user.toString());
		
		// メールアドレスの重複チェック
	    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
	        throw new IllegalArgumentException("このメールアドレスは既に登録されています: " + user.getEmail());
	    }

		// districtIdからDistrictを取得
		District district = districtRepository.findById(districtId)
				.orElseThrow(() -> new RuntimeException("指定されたDistrictが見つかりません:" + districtId));

		user.setDistrict(district);
		System.out.println("🔥 Debug: district 設定後 - " + user.toString());

		userRepository.save(user);
		
		System.out.println("🚀 UserRegistrationEvent を発行: " + user.getEmail());
		eventPublisher.publishEvent(new UserRegistrationEvent(this, user, "http://localhost:8080"));
	}

	// ユーザ情報を保存
	public User saveUser(User user) {
		return userRepository.save(user);
	}

	public User findByUserId(String userId) {
		System.out.println("🔍 Debug - findByUserId の引数: " + userId);

		return userRepository.findByUserId(userId)
				.orElse(null);
	}
	
	// 250321一応&& !password.isBlank() 追加
	public Boolean passwordDoubleCheck(String password, String confirmPassword) {
		return password != null && !password.isBlank() && password.equals(confirmPassword);
	}
	
	
	// 会員情報一覧
	public Page<User> getUsersInSameDistrictPage(int userId, Pageable pageable){
		// ユーザを取得（例外で存在チェック）
		User loginUser = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("ユーザが見つかりません"));
		
		
		if (loginUser.getDistrict() == null) {
			throw new IllegalArgumentException("ユーザーの所属町内会が不明です");
		}
		
		// 所属する 町内会を取得
		String districtAssociation = loginUser.getDistrict().getAssociation();
		
		
		// 同じ 町内会に所属するユーザを取得
		return userRepository.findByDistrictAssociationAndEnabledTrueAndDeletedAtIsNull(districtAssociation, pageable);
	}
	
	@Transactional
	public void changeUserRole(int chairmanId, int targetUserId, String newRoleName) {
		// 町内会長（操作主）取得
		User chairman = userRepository.findById(chairmanId)
					.orElseThrow(() -> new IllegalArgumentException("町内会長が見つかりません"));
		
		// 対象ユーザを取得
		User targetUser = userRepository.findById(targetUserId)
					.orElseThrow(() -> new IllegalArgumentException("対象ユーザが見つかりません"));
		
		// 自分と同じ町内化チェック（セキュリティ）
		if (chairman.getDistrict() == null || targetUser.getDistrict() == null ||
				chairman.getDistrict().getId() != targetUser.getDistrict().getId()) {
			throw new IllegalArgumentException("同じ町内のユーザしか変更できません");
		}
		
		// 新しい役職を取得
		Role newRole = roleRepository.findByName(newRoleName)
				.orElseThrow(() -> new IllegalArgumentException("指定された役職が見つかりません"));
		
		// 役職を変更し、保存
		targetUser.setRole(newRole);
		userRepository.save(targetUser);
	}
	
	public Page<User> getUsersSortByCommitteeAndFurigana(int userId, Pageable pageable) {
	    User loginUser = userRepository.findById(userId)
	            .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

	    String association = loginUser.getDistrict().getAssociation();
	    if (association == null) {
	        throw new IllegalArgumentException("町内会が設定されていません");
	    }

	    return userRepository.findByDistrictAssociationAndEnabledTrueAndDeletedAtIsNull(association, pageable);
	}
	
	public Page<User> searchMembers(int userId, String nameKeyword, Integer roleId, Integer committeeId, Pageable pageable)	{
		User loginUser = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("ユーザが見つかりません"));
		
		String association = loginUser.getDistrict().getAssociation();
		String area = loginUser.getDistrict().getArea();
		
		return userRepository.searchByConditions(association, area, nameKeyword, roleId, committeeId, pageable);
	}
	
	// 検索付きの会員一覧取得
	public Page<User> searchUsers(int userId, String nameKeyword, Integer roleId, Integer committeeId, Pageable pageable){
		User loginUser = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("ユーザが見つかりません"));
		
		String association = loginUser.getDistrict().getAssociation();
		if (association == null) {
			throw new IllegalArgumentException("町内会が設定されていません");
			
		}
		String area = loginUser.getDistrict().getArea();
		
		return userRepository.searchByConditions(association, area, nameKeyword, roleId, committeeId, pageable);
	}
	
	// 全ての委員カテゴリを取得
	public List<CommitteeClassification> getAllCommittees() {
		return committeeClassificationRepository.findAll();
	}
	
	public Page<User> searchUsersByConditions(int userId, String nameKeyword,
			Integer roleId,Integer committeeId, Pageable pageable){
		//ログインユーザの取得
		User loginUser = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("ユーザが見つかりません"));
		
		String association = loginUser.getDistrict().getAssociation();
		String area = loginUser.getDistrict().getArea();
		
		if (association == null || area == null) {
			throw new IllegalArgumentException("町内会または区情報が設定されていません");
		}
		
		// 条件に合致するユーザ一覧を取得
		return userRepository.searchByConditions(association, area, nameKeyword, roleId, committeeId, pageable);
	}
	
	public List<User> searchUsersForCsv(int userId, String nameKeyword, Integer roleId, Integer committeeId) {
		User loginUser = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("ユーザが見つかりません"));
		
		String association = loginUser.getDistrict().getAssociation();
		String area = loginUser.getDistrict().getArea();
		
		return userRepository.searchByConditionsForCsv(association, area, nameKeyword, roleId, committeeId);
	}
	
	// 区長が会員をソフトデリート
	@Transactional
	public void softDeletedUser(int loginUserId, int targetUserId) {
		// ログインユーザを取得（区長）
		User loginUser = userRepository.findById(loginUserId)
				.orElseThrow(() -> new IllegalArgumentException("ログインユーザが見つかりません。"));
		
		// 対象ユーザを取得
		User targetUser = userRepository.findById(targetUserId)
				.orElseThrow(() -> new IllegalArgumentException("対象ユーザが見つかりません"));
		
		// 同じ町内に所属しているか確認
		if (!loginUser.getDistrict().getId().equals(targetUser.getDistrict().getId())) {
			throw new IllegalArgumentException("同じ町内の会員のみ削除可能です。");
		}
		
		// 既に削除済みかチェック
		if (targetUser.isDeleted()) {
			throw new IllegalArgumentException("このユーザは既に削除済みです");
		}
		
		// 削除処理（ソフトデリート）
		targetUser.softDelete();
		userRepository.save(targetUser);
	}
	
	public Page<User> findUsersInSameArea(String association, String area, Pageable pageable){
		return userRepository.findByDistrictAssociationAndDistrictAreaAndEnabledTrueAndDeletedAtIsNull(
				association, area, pageable);
				
	}
	
	public Page<User> getUsersInSameDistrictAndArea(int userId, Pageable pageable) {
	    // ユーザー取得（例外処理あり）
	    User loginUser = userRepository.findById(userId)
	        .orElseThrow(() -> new IllegalArgumentException("ユーザが見つかりません"));

	    String association = loginUser.getDistrict().getAssociation();
	    String area = loginUser.getDistrict().getArea();

	    if (association == null || area == null) {
	        throw new IllegalArgumentException("町内会または区が設定されていません");
	    }

	    return userRepository.findByDistrictAssociationAndDistrictAreaAndEnabledTrueAndDeletedAtIsNull(
	            association, area, pageable);
	}
	
	public List<User> getUsersInSameDistrictAndAreaForCsv(int loginUserId) {
		User loginUser = userRepository.findById(loginUserId)
				.orElseThrow(() -> new IllegalArgumentException("ユーザがみつかりません"));
		
		String association = loginUser.getDistrict().getAssociation();
		String area = loginUser.getDistrict().getArea();
		
		if(association == null || area == null) {
			throw new IllegalArgumentException("町内会または区が設定されていません");
		}
		
		// 並び順 役職ID　→フリガナ
		Sort sort = Sort.by(Sort.Order.asc("role.id"), Sort.Order.asc("furigana"));
		return userRepository.findByDistrictAssociationAndDistrictAreaAndEnabledTrueAndDeletedAtIsNull(association, area, sort);
	}
	
	// 委員長が同町内同区の委員情報を取得
	public Page<User> getUsersInSameDistrictAndAreaANDCommittee(int userId, Pageable pageable) {
		User loginUser = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("ユーザが見つかりません"));
		
		String association = loginUser.getDistrict().getAssociation();
		String area = loginUser.getDistrict().getArea();
		int committee = loginUser.getCommittee().getId();
		
		if(association == null || area == null ) {
			throw new IllegalArgumentException("町内会または区、委員が設定されていません");
		}
		
		// 並び順 roleid →フリガナ
		Sort sort = Sort.by(Sort.Order.asc("role.id"), Sort.Order.asc("furigana"));
		return userRepository.findByDistrictAssociationAndDistrictAreaAndCommitteeIdAndEnabledTrueAndDeletedAtIsNull
(association, area, committee, pageable);
	}
	
	public List<User> getUserInSameDistrictAndAreaAndCommitteeForCsv(int loginUserId){
		User loginUser = userRepository.findById(loginUserId)
				.orElseThrow(() -> new IllegalArgumentException("ユーザが見つかりません"));
		
		String association = loginUser.getDistrict().getAssociation();
		String area = loginUser.getDistrict().getArea();
		int committeeId = loginUser.getCommittee().getId();
		
		if(association == null || area == null) {
			throw new IllegalArgumentException("町内会または区が設定されていません");
		}
		
		// 並び順 役職ID → フリガナ
		Sort sort = Sort.by(Sort.Order.asc("role.id"),
				Sort.Order.asc("furigana"));
		
		return userRepository.findNonAdminUsersInSameCommittee(
				association, area, committeeId, sort);
		
	}
	
	// ユーザ情報をmypageから更新
	public void updateUser(UserEditForm form, User loginUser) {
		form.normalize();
		
		loginUser.setEmail(form.getEmail());
		loginUser.setUserId(form.getUserId());
		loginUser.setName(form.getName());
		loginUser.setFurigana(form.getFurigana());
		loginUser.setPhoneNumber(form.getPhoneNumber());
		
		// パスワードをハッシュ化
		String hashedPassword = passwordEncoder.encode(form.getPassword());
		loginUser.setPassword(hashedPassword);
		
		// 更新
		userRepository.save(loginUser);
		
	}
	
	@Transactional
	public void updateUserRolesAndCommittees(UserRoleCommitteeUpdateForm form) {
		for (UserRoleCommitteeUpdateForm.UserFormRow userRow : form.getUsers()) {
			// userIdからUserを探す
			User user = userRepository.findByIdAndDeletedAtIsNullAndEnabledTrue(userRow.getUserId())
					.orElse(null);
			
			if (user == null) {
				continue; 
			}
			
			// roleId更新
			if (userRow.getRoleId() != null) {
				Role role = roleRepository.findById(userRow.getRoleId())
						.orElse(null);
				user.setRole(role);
			}
			
			// committeeId更新
			if (userRow.getCommitteeId() != null) {
				CommitteeClassification committee = committeeClassificationRepository.findById(userRow.getCommitteeId())
						.orElse(null);
				user.setCommittee(committee);
			} else {
				user.setCommittee(null);
			}
			
			// 保存
			userRepository.save(user);
		}
	}
	
	//districtAssociationでRole_町内会長を取得
	public List<User> getMayors(User user){
		String association = user.getDistrict().getAssociation();
		String regionCode = user.getDistrict().getRegionCode();
		return userRepository.findMayorsByAssociationAndRegionCode(association, regionCode);
	};
	
	// 仮ユーザーの追加処理(町内会長、区長）
	public List<User> casualUserRegi(District district) {
		String kaichoUserId, kaichoEmail, kuchoUserId, kuchoEmail, domain;
		
		do {
			kaichoUserId = UUID.randomUUID().toString().replace("-", "").substring(0,10);
			kuchoUserId = UUID.randomUUID().toString().replace("-", "").substring(0,10);
			domain = "@example.com";
			kaichoEmail = kaichoUserId + domain;
			kuchoEmail = kuchoUserId + domain;
		} while(userRepository.existsByUserId(kaichoUserId) || userRepository.existsByUserId(kuchoUserId)
				|| userRepository.existsByEmail(kaichoEmail) || userRepository.existsByEmail(kuchoEmail));
		
		User kaicho = new User();
		User kucho = new User();
		
		Role kaichoRole = roleRepository.findByName("ROLE_町内会長")
				.orElseThrow(() -> new IllegalArgumentException("指定した名称の役割が存在しません"));
		
		Role kuchoRole = roleRepository.findByName("ROLE_区長")
				.orElseThrow(() -> new IllegalArgumentException("指定した名称の役割が存在しません"));
		
		kaicho.setEmail(kaichoEmail);
		kaicho.setUserId(kaichoUserId);
		kaicho.setPassword(passwordEncoder.encode("12345"));
		kaicho.setName("会長テスト");
		kaicho.setFurigana("カイチョウテスト");
		kaicho.setPhoneNumber("12345678");
		kaicho.setPostalCode("12345678");
		kaicho.setAddress("テスト住所");
		kaicho.setDistrict(district);
		kaicho.setEnabled(true);
		kaicho.setRole(kaichoRole);
		
		kucho.setEmail(kuchoEmail);
		kucho.setUserId(kuchoUserId);
		kucho.setPassword(passwordEncoder.encode("12345"));
		kucho.setName("区長テスト");
		kucho.setFurigana("クチョウテスト");
		kucho.setPhoneNumber("12345678");
		kucho.setPostalCode("12345678");
		kucho.setAddress("テスト住所");
		kucho.setDistrict(district);
		kucho.setEnabled(true);
		kucho.setRole(kuchoRole);
		
		userRepository.save(kaicho);
		userRepository.save(kucho);
		
		return List.of(kaicho, kucho);
	}

}
