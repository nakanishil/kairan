package com.example.kairan.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kairan.csv.UserCsvData;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.form.UserEditForm;
import com.example.kairan.form.UserRegiForm;
import com.example.kairan.form.UserRoleCommitteeUpdateForm;
import com.example.kairan.repository.CommitteeClassificationRepository;
import com.example.kairan.repository.EmailVerificationTokenRepository;
import com.example.kairan.repository.RoleRepository;
import com.example.kairan.repository.UserRepository;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.UserService;
import com.orangesignal.csv.Csv;
import com.orangesignal.csv.CsvConfig;
import com.orangesignal.csv.handlers.CsvEntityListHandler;
@Controller
public class UserController {
	
	private EmailVerificationTokenRepository emailVerificationTokenRepository;
	private UserRepository userRepository;
	private RoleRepository roleRepository;
	private UserService userService;
	private CommitteeClassificationRepository committeeClassificationRepository;
	private final PasswordEncoder passwordEncoder;
	
	
	
	public UserController(UserRepository userRepository, EmailVerificationTokenRepository emailVerificationTokenRepository,
			UserService userService, PasswordEncoder passwordEncoder, RoleRepository roleRepository,
			CommitteeClassificationRepository committeeClassificationRepository) {
		this.userRepository = userRepository;
		this.emailVerificationTokenRepository = emailVerificationTokenRepository;
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
		this.committeeClassificationRepository = committeeClassificationRepository;
	}
	
	@GetMapping("/auth/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("userRegiForm", new UserRegiForm());
		return "auth/register";
	}
	
	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute UserRegiForm form, BindingResult result,
								@AuthenticationPrincipal UserDetails userDetails,
								Model model,
								RedirectAttributes redirectAttributes) {
		
	
		form.normalize();
		// ログインユーザのuserIdを確認
		System.out.println("🔍 Debug - ログインユーザーの userId: " + userDetails.getUsername());
		
		if (result.hasErrors()) {
			System.out.println("バリデーションエラーが発生しました: " + result.getAllErrors());
			model.addAttribute("userRegiForm", form);
			// 入力エラー時は登録画面へ
			model.addAttribute("errorMessage", "入力エラーがあります。修正してください。");
			return "auth/register"; 
		}
		
		 // パスワードと確認用パスワードが一致しない場合、エラーを追加
	    if (!userService.passwordDoubleCheck(form.getPassword(), form.getConfirmPassword())) {
	        result.rejectValue("confirmPassword", "password.mismatch", "パスワードと確認用パスワードが一致していません。");
	        model.addAttribute("userRegiForm", form);
	        return "auth/register";
	    }
	    
	    // **既存のメールアドレスがある場合
	    Optional<User> existingUser = userService.findByEmail(form.getEmail());
	    if (existingUser.isPresent()) {
	        model.addAttribute("userRegiForm", form);
	        model.addAttribute("errorMessage", "このメールアドレスは既に登録されています: " + form.getEmail());
	        return "auth/register"; // 登録画面に戻る
	    }
	    
	    // 既存のユーザIDと重複する場合
	    if (userService.findByUserId(form.getUserId()) != null) {
	    	model.addAttribute("userRegiForm", form);
	    	model.addAttribute("errorMessage", "このユーザIDは既に使用されています: " + form.getUserId());
	    	return "auth/register";
	    }
		
		if(userService.passwordDoubleCheck(form.getPassword(), form.getConfirmPassword())){
			// 区長の'districtId'を取得
			User loginUser = userService.findByUserId(userDetails.getUsername());
			int districtId = loginUser.getDistrict().getId();
			
			// デフォルトの役職を設定
			Role defaultRole = roleRepository.findByName("ROLE_会員")
					.orElseThrow(() -> new IllegalArgumentException("役職が見つかりません"));
			
			// userRegiFormからUserエンティティを作成
			User user = new User();
		    user.setEmail(form.getEmail());
		    user.setUserId(form.getUserId());
		    user.setPassword(passwordEncoder.encode(form.getPassword()));
		    user.setName(form.getName());
		    user.setFurigana(form.getFurigana());
		    user.setPhoneNumber(form.getPhoneNumber());
		    user.setPostalCode(form.getPostalCode());
		    user.setAddress(form.getAddress());
		    user.setEnabled(false); // 認証メールで有効化するまで false
		    user.setRole(defaultRole);
			
			userService.saveUser(user, districtId);
			
			
			redirectAttributes.addFlashAttribute("successMessage", "会員を登録しました。新規会員様に認証メールを送信しました。");
			return "redirect:/"; 
		}
		model.addAttribute("errorMessage", "パスワードと確認用パスワードが一致していません。");
		return "auth/register";
	}
	
	// 町内会長が町内会員の情報を閲覧
	@GetMapping("/user/member-list")
	public String getMemberList(@AuthenticationPrincipal UserDetailsImpl userDetails, // コントローラでログインユーザ情報を受け取る
								@RequestParam(defaultValue = "0") int page,
								@RequestParam(required = false) String nameKeyword,
								@RequestParam(required = false) Integer roleId,
								@RequestParam(required = false) Integer committeeId,
								HttpServletRequest request,
								Model model
	) {
		// ログインユーザのID取得
		User loginUser = userDetails.getUser();
		int userId = userDetails.getUser().getId();
		String userAssociation = userDetails.getUser().getDistrict().getAssociation();
		
		// ページ情報にソート条件を適用
		Sort sort = Sort.by(Sort.Order.asc("role.id"), Sort.Order.asc("furigana"));
		Pageable pageable = PageRequest.of(page, 10, sort);
		
		
		// サービスを経由して検索結果取得
		Page<User> userPage = userService.searchUsersByConditions(
				userId, nameKeyword, roleId, committeeId, pageable);
		
		model.addAttribute("userAssociation", userAssociation);
		model.addAttribute("userPage", userPage);
		model.addAttribute("nameKeyword", nameKeyword);
		model.addAttribute("roleId", roleId);
		model.addAttribute("committeeId", committeeId);
		model.addAttribute("roles", roleRepository.findAll());
		model.addAttribute("committees", committeeClassificationRepository.findAll());
		
		return "user/member-list"; 
		
	}
	
	@GetMapping("/user/member-list/csv")
	public void exportMemberListCsv(
	        @AuthenticationPrincipal UserDetailsImpl userDetails,
	        @RequestParam(required = false) String nameKeyword,
	        @RequestParam(required = false) Integer roleId,
	        @RequestParam(required = false) Integer committeeId,
	        HttpServletResponse response) throws IOException {

	    int userId = userDetails.getUser().getId();
	    List<User> users = userService.searchUsersForCsv(userId, nameKeyword, roleId, committeeId);

	    // ファイル名設定
	    response.setContentType("text/csv; charset=UTF-8");
	    response.setHeader("Content-Disposition", "attachment; filename=\"member-list.csv\"");

	    // BOM付きUTF-8のOutputStreamWriterを使う
	    try (var out = response.getOutputStream()) {
	        // UTF-8のBOMを先に書き込む（3バイト）
	        out.write(0xEF);
	        out.write(0xBB);
	        out.write(0xBF);

	        // 本体のWriter
	        try (var writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
	            writer.println("氏名,フリガナ,役職,委員,町名");
	            for (User user : users) {
	                String name = user.getName();
	                String furigana = user.getFurigana();
	                String role = user.getRole().getName().replace("ROLE_", "");
	                String committee = user.getCommittee() != null ? user.getCommittee().getName() : "未設定";
	                String district = user.getDistrict().getName();
	                writer.printf("%s,%s,%s,%s,%s%n", name, furigana, role, committee, district);
	            }
	        }
	    }
	}

	
	@GetMapping("/user/member-list/export")
	public void exportCsv(
	        @AuthenticationPrincipal UserDetailsImpl userDetails,
	        @RequestParam(required = false) String nameKeyword,
	        @RequestParam(required = false) Integer roleId,
	        @RequestParam(required = false) Integer committeeId,
	        HttpServletResponse response) throws IOException {

	    int userId = userDetails.getUser().getId();
	    List<User> users = userService.searchUsersForCsv(userId, nameKeyword, roleId, committeeId);

	    // 出力先ファイル情報
	    File file = new File("user_export.csv");

	    // User → UserCsvData にマッピング
	    List<UserCsvData> csvDataList = new ArrayList<>();
	    for (User user : users) {
	        UserCsvData data = new UserCsvData();
	        data.setName(user.getName());
	        data.setFurigana(user.getFurigana());
	        data.setRoleName(user.getRole() != null ? user.getRole().getName().replace("ROLE_", "") : "");
	        data.setCommitteeName(user.getCommittee() != null ? user.getCommittee().getName() : "");
	        data.setDistrictName(user.getDistrict() != null ? user.getDistrict().getName() : "");
	        csvDataList.add(data);
	    }

	    // OrangeSignal CSV 設定
	    CsvConfig cfg = new CsvConfig(',', '"', '"'); // コンマ区切り、囲みあり
	    String encode = "MS932"; // Excel対応の文字コード（Shift_JIS）

	    // 保存処理（出力先を response.getOutputStream() にして直接出力も可）
	    Csv.save(
	        csvDataList,
	        file,
	        encode,
	        cfg,
	        new CsvEntityListHandler<>(UserCsvData.class)
	    );

	    // クライアントにCSVを返す
	    response.setContentType("text/csv; charset=MS932");
	    response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''user_export.csv");
	    Files.copy(file.toPath(), response.getOutputStream());
	}
	
	@GetMapping("/user/area-members")
	public String getAreaMemberList(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@RequestParam(defaultValue = "0") int page,
			Model model
	) {
		// ログインユーザ情報を取得（区長）
		User loginUser = userDetails.getUser();
		String association = loginUser.getDistrict().getAssociation();
		String area = loginUser.getDistrict().getArea();
		
		// ソート条件:役職ID 昇順 → フリガナ昇順
		Sort sort = Sort.by(Sort.Order.asc("role.id"), Sort.Order.asc("furigana"));
		Pageable pageable = PageRequest.of(page, 10, sort);
		
		// 同じ町内association && 区areaのユーザを取得
		 Page<User> userPage = userService.findUsersInSameArea(association, area, pageable);
		 
		model.addAttribute("userPage", userPage);
		model.addAttribute("area", area);
		model.addAttribute("association", association);
		model.addAttribute("area", area);
		
		return "user/area-members";
	}
	
	@PostMapping("/user/delete")
	public String softDeleteUser(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@RequestParam("userId") Integer targetUserId,
			RedirectAttributes redirectAttributes) {
		
		int loginUserId = userDetails.getUser().getId();
		
		try {
			userService.softDeletedUser(loginUserId, targetUserId);
			redirectAttributes.addFlashAttribute("successMessage", "ユーザを削除しました");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		
		return "redirect:/user/area-members";
	}
	
	@GetMapping("/user/area-members/csv")
	public void exportAreaMembersCsv(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			HttpServletResponse response) throws IOException {
		// ログインユーザID取得
		int loginUserId = userDetails.getUser().getId();
		
		// ユーザ一覧(csvソート)を取得
		List<User> users = userService.getUsersInSameDistrictAndAreaForCsv(loginUserId);
		
		// ファイル名と文字コード
		String fileName = "area_members.csv";
		response.setContentType("text/csv;p charset=MS932");
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
		
		// 書き込み
		try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "MS932"))){
			writer.println("氏名,フリガナ,役職,委員,電話番号,住所");
			
			for (User user : users) {
				String name = user.getName();
				String furigana = user.getFurigana();
				String role = user.getRole() != null ? user.getRole().getName().replace("ROLE_", "") : "";
				String committee = user.getCommittee() != null ? user.getCommittee().getName() : "未設定";
				String phone = user.getPhoneNumber();
				String address = user.getAddress();
				
				writer.printf("%s,%s,%s,%s,%s,%s%n", name, furigana, role, committee, phone, address);
			}
		}
		
	}
	
	@GetMapping("/user/committee-members")
	public String getCommitteeMemberList(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@RequestParam(defaultValue = "0") int page,
			Model model) {
		
		// ログインユーザを取得
		User loginUser = userDetails.getUser();
		String association = loginUser.getDistrict().getAssociation();
		String area = loginUser.getDistrict().getArea();
		int committeeId = loginUser.getCommittee().getId();
		String committeeName = loginUser.getCommittee().getName();
		
		
		// ソート条件：役職ID 昇順 → フリガナ昇順
		Sort sort = Sort.by(Sort.Order.asc("role.id"), Sort.Order.asc("furigana"));
		Pageable pageable = PageRequest.of(page, 10, sort);
		
		// 同じ町内、区、委員のユーザを取得
		Page<User> userPage = userService.getUsersInSameDistrictAndAreaANDCommittee(loginUser.getId(), pageable);
		
		model.addAttribute("userPage", userPage);
		model.addAttribute("area", area);
		model.addAttribute("association", association);
		model.addAttribute("committeeId", committeeId);
		model.addAttribute("committeeName", committeeName);
		
		return "user/committee-members";
		
	}
	
	@GetMapping("/user/committee-members/csv")
	public void exportCommitteeMembersCsv(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			HttpServletResponse response) throws IOException {
		// ログインユーザID取得
		int loginUserId = userDetails.getUser().getId();
		
		// ユーザ一覧を取得 (csvソート）
		List<User> users = userService.getUserInSameDistrictAndAreaAndCommitteeForCsv(loginUserId);
		
		// ファイル名と文字コード
		String fileName = "committee-members.csv";
		response.setContentType("text/csv;p charset=MS932");
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
		
		// 書き込み
		try(PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "MS932"))){
			writer.println("氏名,フリガナ,役職,委員,電話番号,住所");
			
			for (User user : users) {
				String name = user.getName();
				String furigana = user.getFurigana();
				String role = user.getRole() != null ? user.getRole().getName().replace("ROLE_", "") : "";
				String committee = user.getCommittee() !=null ? user.getCommittee().getName() : "未設定";
				String phone = user.getPhoneNumber();
				String address = user.getAddress();
				
				writer.printf("%s,%s,%s,%s,%s,%s%n", name, furigana, role, committee, phone, address);
			}
		}
	}
	
	// マイページ 表示
	@GetMapping("/user/mypage")
	public String mypageView(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			Model model) 
	{	
		User loginUser = userDetails.getUser();
		
		String email = loginUser.getEmail();
		String userId = loginUser.getUserId();
		String name = loginUser.getName();
		String furigana = loginUser.getFurigana();
		String phoneNumber = loginUser.getPhoneNumber();
		
		model.addAttribute("email", email);
		model.addAttribute("userId", userId);
		model.addAttribute("name", name);
		model.addAttribute("furigana", furigana);
		model.addAttribute("phoneNumber", phoneNumber);
		
		//google連携のため
		model.addAttribute("user", loginUser);
		
		
		return "user/mypage";
	}
	
	// 自分の情報を編集
	@GetMapping("/user/edit-page")
	public String showEditForm(
			Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetails)
	{
		User loginUser = userDetails.getUser();
		UserEditForm userEditForm = new UserEditForm();
				
		userEditForm.setEmail(loginUser.getEmail());
		userEditForm.setUserId(loginUser.getUserId());
		userEditForm.setName(loginUser.getName());
		userEditForm.setFurigana(loginUser.getFurigana());
		userEditForm.setPhoneNumber(loginUser.getPhoneNumber());
		
		model.addAttribute("form", userEditForm);
		
		return "user/edit-page";
	}
	
	// 自分の情報を記録
	@PostMapping("/user/edit")
	public String editUser(
			@Valid @ModelAttribute("form") UserEditForm form,
			BindingResult result,
			Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes) 
	{
		if (result.hasErrors()) {
			System.out.println("バリデーションエラー" + result.getAllErrors());
			
			return "user/edit-page";
		}
		
		if (!userService.passwordDoubleCheck(form.getPassword(), form.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "password.mismatch",
					"パスワードと確認用パスワードが一致していません。");
			
			return "user/edit-page";
		}
		
		
		User loginUser = userDetails.getUser(); 
		
		
		userService.updateUser(form, loginUser);
		
		redirectAttributes.addFlashAttribute("successMessage", "会員情報を変更しました。");
		
		return "redirect:/user/mypage";
	}
	
	@PostMapping("/user/member-list/update")
	public String updateMemberList(
			@ModelAttribute UserRoleCommitteeUpdateForm form,
			@RequestParam(required = false) String nameKeyword,
			@RequestParam(required = false) Integer roleId,
			@RequestParam(required = false) Integer committeeId,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) Integer page
			)
	{
		userService.updateUserRolesAndCommittees(form);
		
		StringBuilder redirectUrl = new StringBuilder("redirect:/user/member-list");
		
		boolean hasParam = false;
		
		if (nameKeyword != null && !nameKeyword.isEmpty()) {
			redirectUrl.append(hasParam ? "&" : "?").append("nameKeyword=").append(nameKeyword);
			hasParam = true;
		}
		if (roleId != null) {
			redirectUrl.append(hasParam ? "&" : "?").append("roleId=").append(roleId);
			hasParam = true;
		}
		if (committeeId != null) {
			redirectUrl.append(hasParam ? "&" : "?").append("committeeId=").append(committeeId);
			hasParam = true;
		}
		if (sort != null && !sort.isEmpty()) {
			redirectUrl.append(hasParam ? "&" : "?").append("sort=").append(sort);
			hasParam = true;
		}
		if (page != null) {
			redirectUrl.append(hasParam ? "&": "?").append("page=").append(page);
		}
		
		return redirectUrl.toString();
	}
}
	
	
