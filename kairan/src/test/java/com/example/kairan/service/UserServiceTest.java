package com.example.kairan.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.kairan.entity.CommitteeClassification;
import com.example.kairan.entity.District;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.event.UserRegistrationEvent;
import com.example.kairan.form.UserEditForm;
import com.example.kairan.repository.CommitteeClassificationRepository;
import com.example.kairan.repository.DistrictRepository;
import com.example.kairan.repository.RoleRepository;
import com.example.kairan.repository.UserRepository;

public class UserServiceTest {

	private UserService userService;
	private UserRepository userRepository;
	private DistrictRepository districtRepository;
	private ApplicationEventPublisher eventPublisher;
	private RoleRepository roleRepository;
	private CommitteeClassificationRepository committeeClassificationRepository;
	private PasswordEncoder passwordEncoder;
	private UserEditForm userEditForm;



	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		districtRepository = mock(DistrictRepository.class);
		eventPublisher = mock(ApplicationEventPublisher.class);
		roleRepository = mock(RoleRepository.class);
		committeeClassificationRepository = mock(CommitteeClassificationRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);	
		userEditForm = mock(UserEditForm.class);

		userService = new UserService(userRepository, districtRepository, eventPublisher, roleRepository, committeeClassificationRepository,
				passwordEncoder);
	}

	// 区長が会員登録するロジックテスト

	@Test
	void testFindByEmail_既存のメールアドレスを検索するとユーザが返る() {
		// Arrange(準備)
		String email = "test@example.com";
		User user = new User();
		user.setEmail(email);

		// userRepository の findByEmail() を呼び出すと、「さっき作ったユーザーを返すようにする」モック設定。
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		// Act(実行) 実際に UserService の findByEmail() を呼んで、結果を取得！
		Optional<User> result = userService.findByEmail(email);

		// Assert(検証) isPresent() = 値が存在するか
		assertTrue(result.isPresent());
		assertEquals(email, result.get().getEmail());
	}

	@Test
	void testFindById_ユーザが見つかる場合は正常に返す() {
		// arrange
		int userId = 1;
		User user = new User();
		user.setId(userId);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// Act
		User result = userService.findById(userId);

		// Assert
		assertNotNull(result);
		assertEquals(userId, result.getId());
	}

	@Test
	void testFindById_ユーザが見つからない場合は例外が投げられる() {
		// arrange
		int userId = 999;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// act assert
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userService.findById(userId);
		});

		assertEquals("ユーザが見つかりません", exception.getMessage());
	}

	@Test
	void testSaveUser_正常にユーザが保存されイベントが発行される() {
		// arrange
		User user = new User();
		user.setEmail("newuser@example.com");

		int districtId = 1;
		District district = new District();
		district.setId(districtId);

		// モックの設定
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty()); // メールアドレス未登録
		when(districtRepository.findById(districtId)).thenReturn(Optional.of(district)); // districtが存在する

		// act
		userService.saveUser(user,districtId);

		// assert
		verify(userRepository).save(user); // ユーザ保存が呼ばれたことを確認
		verify(eventPublisher).publishEvent(any(UserRegistrationEvent.class)); // イベント発行を確認

		// districtが正しく設定されていることをチェック
		assertEquals(district, user.getDistrict());
	}

	@Test
	void testSaveUser_メールアドレスが既に存在する場合はエラー() {
		// Arrange
		User user = new User();
		user.setEmail("duplicate@example.com");

		int districtId = 1;

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user)) ;// すでに登録済み

		// act assert
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userService.saveUser(user, districtId);
		});

		assertEquals("このメールアドレスは既に登録されています: duplicate@example.com", exception.getMessage());

		// save()は呼ばれていないことを確認
		verify(userRepository, never()).save(any(User.class));
		verify(eventPublisher, never()).publishEvent(any(UserRegistrationEvent.class));
	}

	@Test
	void testSaveUser_指定されたDistrictが存在しない場合はエラー() {
		// arrange
		User user = new User();
		user.setEmail("valid@example.com");

		int districtId = 999; // 存在しないdistrictId

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty()); // メールアドレスは未登録
		when(districtRepository.findById(districtId)).thenReturn(Optional.empty()); // districtが存在しない

		// act assert
		Exception exception = assertThrows(RuntimeException.class, () -> {
			userService.saveUser(user, districtId);
		});

		assertEquals("指定されたDistrictが見つかりません:999", exception.getMessage());

		// save()は呼ばれていないことを確認
		verify(userRepository, never()).save(any(User.class));
		verify(eventPublisher, never()).publishEvent(any(UserRegistrationEvent.class));
	}

	@Test
	void testPasswordDoubleCheck_パスワードが一致する場合はtrueを返す() {
		assertTrue(userService.passwordDoubleCheck("password123", "password123"));
	}

	@Test
	void testPasswordDoublecheck_パスワードが一致する場合はTrueを返す() {
		assertTrue(userService.passwordDoubleCheck("password123", "password123"));
	}

	@Test
	void testPasswordDoubleCheck_パスワードがnullの場合はFalseを返す() {
		assertFalse(userService.passwordDoubleCheck(null, "password123"));
	}

	@Test
	void testPassswordDoubleCheck_確認パスワードが違う場合はFalseを返す() {
		assertFalse(userService.passwordDoubleCheck("password123", "pass123"));
	}

	@Test
	void testPasswordDoubleCheck_両方空文字の場合はTruwを返す() {
		assertFalse(userService.passwordDoubleCheck("", ""));
	}


	@Test
	void testChangeUserRole_町内会長が自分の町内のユーザ役職を変更できる() {
		//arrange
		District district = new District();
		district.setId(1);
		district.setAssociation("旭福住町内会");

		Role oldRole = new Role();
		oldRole.setName("ROLE_会員");

		Role newRole = new Role();
		newRole.setName("ROLE_委員");

		Role chairmanRole = new Role();
		chairmanRole.setName("ROLE_町内会長");

		User chairman = new User();
		chairman.setId(1);
		chairman.setDistrict(district);
		chairman.setRole(chairmanRole); // 会長

		User targetUser = new User();
		targetUser.setId(2);
		targetUser.setDistrict(district);
		targetUser.setRole(oldRole);

		when(userRepository.findById(1)).thenReturn(Optional.of(chairman));
		when(userRepository.findById(2)).thenReturn(Optional.of(targetUser));
		when(roleRepository.findByName("ROLE_委員")).thenReturn(Optional.of(newRole));

		// act
		userService.changeUserRole(1, 2, "ROLE_委員");

		// assert
		assertEquals("ROLE_委員", targetUser.getRole().getName());
		verify(userRepository).save(targetUser);
	}

	@Test
	void testGetUsersSortByCommitteeAndFurigana_委員ID昇順かつfurigana昇順() {
	    // arrange
		
		District district = new District();
		district.setAssociation("合同町内会A");
	    
	    District outsideDistrict = new District();
	    outsideDistrict.setAssociation("別の町内会");

	    CommitteeClassification cc1 = new CommitteeClassification();
	    cc1.setId(1); cc1.setName("防災");

	    CommitteeClassification cc2 = new CommitteeClassification();
	    cc2.setId(2); cc2.setName("環境");

	    User loginUser = new User();
	    loginUser.setId(1);
	    loginUser.setDistrict(district);
	    
	    User outsider = new User();
	    outsider.setId(999); outsider.setCommittee(cc2); outsider.setFurigana("カトウ");
	    outsider.setDistrict(outsideDistrict); // 他の町内

	    User userA = new User(); // ID2, 環境, タナカ
	    userA.setId(2); userA.setCommittee(cc2); userA.setFurigana("タナカ");
	    userA.setDistrict(district); userA.setEnabled(true); userA.setDeletedAt(null);

	    User userB = new User(); // ID3, 防災, アオキ
	    userB.setId(3); userB.setCommittee(cc1); userB.setFurigana("アオキ");
	    userB.setDistrict(district); userB.setEnabled(true); userB.setDeletedAt(null);


	    Pageable pageable = PageRequest.of(0, 10);
	    List<User> sortedList = List.of(userB, userA); // 正しい並び: アオキ → タナカ

	    Page<User> userPage = new PageImpl<>(sortedList, pageable, sortedList.size());

	    when(userRepository.findById(1)).thenReturn(Optional.of(loginUser));
	    when(userRepository.findByDistrictAssociationAndEnabledTrueAndDeletedAtIsNull("合同町内会A", pageable))
	    .thenReturn(userPage);


	    // act
	    Page<User> result = userService.getUsersSortByCommitteeAndFurigana(1, pageable);

	    // assert
	    assertEquals(2, result.getContent().size());
	    assertEquals("アオキ", result.getContent().get(0).getFurigana());
	    assertEquals("タナカ", result.getContent().get(1).getFurigana());

	}
	
	@Test
	void updateUser_ユーザ情報更新セーブメソッド() {
		//arrange
		UserEditForm form = new UserEditForm();
		form.setEmail("xxxx@email.com");
		form.setUserId("test1234");
		form.setName("山田太郎");
		form.setFurigana("ヤマダタロウ");
		form.setPhoneNumber("0123456789");
		form.setPassword("newpassword");
		
		User loginUser = new User();
		loginUser.setEmail("old@email.com");
		loginUser.setUserId("olduser");
		loginUser.setName("古い名前");
		loginUser.setFurigana("フルイナマエ");
		loginUser.setPhoneNumber("0000000000");
		
		// パスワードエンコーダのモック
		when(passwordEncoder.encode(form.getPassword())).thenReturn("hashedpassword");
		
		// act
		userService.updateUser(form, loginUser);
		
		// assert
		assertEquals("xxxx@email.com", loginUser.getEmail());
		assertEquals("test1234", loginUser.getUserId());
		assertEquals("山田太郎", loginUser.getName());
		assertEquals("ヤマダタロウ", loginUser.getFurigana());
		assertEquals("0123456789", loginUser.getPhoneNumber());
		assertEquals("hashedpassword", loginUser.getPassword());
		
		// saveメソッドが呼ばれていることを確認
		verify(userRepository).save(loginUser);
	}
	
	@Test
	void casualUserRegi_仮ユーザーを正常に2人作成して保存する() {
	    // arrange
	    District district = new District();
	    district.setId(1);
	    district.setName("テスト町");

	    // UUID重複チェック：すべて false（重複なし）
	    when(userRepository.existsByUserId(anyString())).thenReturn(false);
	    when(userRepository.existsByEmail(anyString())).thenReturn(false);

	    // ロール取得：町内会長・区長
	    Role kaichoRole = new Role();
	    kaichoRole.setName("ROLE_町内会長");

	    Role kuchoRole = new Role();
	    kuchoRole.setName("ROLE_区長");

	    when(roleRepository.findByName("ROLE_町内会長")).thenReturn(Optional.of(kaichoRole));
	    when(roleRepository.findByName("ROLE_区長")).thenReturn(Optional.of(kuchoRole));

	    // パスワードエンコード固定
	    when(passwordEncoder.encode("12345")).thenReturn("encoded_password");

	    // save(): ユーザーを返すように設定（そのまま返却）
	    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

	    // act
	    List<User> result = userService.casualUserRegi(district);

	    // assert
	    assertEquals(2, result.size());

	    User kaicho = result.get(0);
	    User kucho = result.get(1);

	    assertEquals("encoded_password", kaicho.getPassword());
	    assertEquals("encoded_password", kucho.getPassword());
	    assertEquals("ROLE_町内会長", kaicho.getRole().getName());
	    assertEquals("ROLE_区長", kucho.getRole().getName());
	    assertEquals(district, kaicho.getDistrict());
	    assertEquals(district, kucho.getDistrict());

	    // 検証
	    verify(userRepository, atLeastOnce()).existsByUserId(anyString());
	    verify(userRepository, atLeastOnce()).existsByEmail(anyString());
	    verify(roleRepository).findByName("ROLE_町内会長");
	    verify(roleRepository).findByName("ROLE_区長");
	    verify(passwordEncoder, times(2)).encode("12345");
	    verify(userRepository, times(2)).save(any(User.class));
	}
	
	@Test
	void casualUserRegi_町内会長ロールが存在しない場合は例外をスローする() {
	    // arrange
	    District district = new District();

	    // UUID重複チェック：false（重複なし）
	    when(userRepository.existsByUserId(anyString())).thenReturn(false);
	    when(userRepository.existsByEmail(anyString())).thenReturn(false);

	    // ロール取得：町内会長が存在しない
	    when(roleRepository.findByName("ROLE_町内会長")).thenReturn(Optional.empty());

	    // act & assert
	    IllegalArgumentException exception = assertThrows(
	        IllegalArgumentException.class,
	        () -> userService.casualUserRegi(district)
	    );

	    assertEquals("指定した名称の役割が存在しません", exception.getMessage());

	    // 保存されていないことを確認
	    verify(userRepository, never()).save(any(User.class));
	}
	
	@Test
	void casualUserRegi_区長ロールが存在しない場合は例外をスローする() {
	    // arrange
	    District district = new District();

	    // UUID重複チェック：false（重複なし）
	    when(userRepository.existsByUserId(anyString())).thenReturn(false);
	    when(userRepository.existsByEmail(anyString())).thenReturn(false);

	    // モック：町内会長ロールは見つかるが、区長ロールは見つからない
	    Role kaichoRole = new Role();
	    kaichoRole.setName("ROLE_町内会長");
	    when(roleRepository.findByName("ROLE_町内会長")).thenReturn(Optional.of(kaichoRole));
	    when(roleRepository.findByName("ROLE_区長")).thenReturn(Optional.empty());

	    // act & assert
	    IllegalArgumentException exception = assertThrows(
	        IllegalArgumentException.class,
	        () -> userService.casualUserRegi(district)
	    );

	    assertEquals("指定した名称の役割が存在しません", exception.getMessage());

	    // 保存されていないことを確認
	    verify(userRepository, never()).save(any(User.class));
	}



	

}
