package com.example.kairan.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kairan.entity.Circular;
import com.example.kairan.entity.CircularFile;
import com.example.kairan.entity.User;
import com.example.kairan.form.CircularEditForm;
import com.example.kairan.form.CircularForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.CircularFileService;
import com.example.kairan.service.CircularReadService;
import com.example.kairan.service.CircularService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CircularController {
	
	private final CircularService circularService;
	private final CircularFileService circularFileService;
	private final CircularReadService circularReadService;
	
	@GetMapping("/circular")
	public String circular(
			@PageableDefault(
					page = 0, size = 10, sort = "createdAt",
					direction = Direction.DESC
					) Pageable pageable,
			Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetails) 
	{
		User loginUser = userDetails.getUser();
		String association = loginUser.getDistrict().getAssociation();
		
		// データ取得
		Page<Circular> circularPage = circularService.getLatestCirculars(association, pageable);
		
		// ログ出力
		System.out.println("circularPage: " + circularPage);
		System.out.println("circularPage contentt size:" + circularPage.getContent().size());
		
		Map<Integer, Boolean> readMap = new HashMap<>();
		for (Circular circular : circularPage) {
			boolean isRead = circularReadService.readStatusCheck(loginUser, circular);
			readMap.put(circular.getId(), isRead);
		}
	   
	    // データがない場合、メッセージ
	    if (circularPage.isEmpty()) {
	        model.addAttribute("message", "回覧板のデータがありません");
	    }
	    
	    model.addAttribute("readMap", readMap); // ← これをビューで使う
	    model.addAttribute("circularPage", circularPage);
	    return "circular/index";
	}
	
	@GetMapping("/circular/detail/{id}")
	public String circularDetail(
	        @PathVariable Integer id,
	        Model model,
	        @AuthenticationPrincipal UserDetailsImpl userDetails
	) {
	    User loginUser = userDetails.getUser();
	    Optional<Circular> circularOpt = circularService.getCircularById(id);

	    if (circularOpt.isEmpty()) {
	        model.addAttribute("errorMessage", "指定された回覧板が見つかりませんでした。");
	        return "circular/detail";
	    }

	    Circular circular = circularOpt.get();
	    List<CircularFile> circularFiles = circularFileService.getfindByCircularId(circular);

	    // 既読確認と既読追加
	    if (circularReadService.readStatusCheck(loginUser, circular)) {
	        circularReadService.markAsRead(loginUser, circular);
	    } else {
	        model.addAttribute("readStatus", "既読");
	    }

	    model.addAttribute("circularFiles", circularFiles);
	    model.addAttribute("circular", circular); // ←ここを修正！
	    return "circular/detail";
	}

	
// 管理	
	// 回覧板管理ページ
	@GetMapping("/circular/control-page")
	public String circularControlPage(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PageableDefault(size = 20)Pageable pageable,
			Model model)
	{
		User loginUser = userDetails.getUser();
		Page<Circular> circularPage = circularService
				.getLatestCirculars(loginUser.getDistrict().getAssociation(), pageable);
		
		model.addAttribute("association", loginUser.getDistrict().getAssociation());
		model.addAttribute("circularPage", circularPage);
		return "circular/control-page";
	}
	
	// 回覧板登録ページ
	@GetMapping("/circular/control/register-page")
	public String circularRegiPage(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			Model model
			)
	{
		CircularForm form = new CircularForm();
		User loginUser = userDetails.getUser();
		String association = loginUser.getDistrict().getAssociation();
		
		model.addAttribute("association", association);
		model.addAttribute("form", form);
		return "circular/register-page";
	}
	
	// 回覧板登録機能
	@PostMapping("/circular/control/register")
	public String circularRegi(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes,
			Model model,
			@Valid @ModelAttribute("form") CircularForm form, BindingResult result,
			@PageableDefault(size = 20)Pageable pageable
			) 
	{
		User loginUser = userDetails.getUser();
		
		if(result.hasErrors()) {
			System.out.println("バリデーションエラー" + result.getAllErrors());
			model.addAttribute("form", form);
			return "circular/register-page";
		}
		
		try {
			circularService.createCircular(form, loginUser);
			redirectAttributes.addFlashAttribute("successMessage", "回覧板を追加しました。");
			Page<Circular> circularPage = circularService
					.getLatestCirculars(loginUser.getDistrict().getAssociation(), pageable);
			redirectAttributes.addFlashAttribute("circularPage", circularPage);
			return "redirect:/circular/control-page";
			
		} catch(Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("form", form);
		}
		
		return "circular/register-page";
	}
	
	// 投稿編集ページ
	@GetMapping("/circular/control/edit-page/{id}")
	public String circularEditPage(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable("id") int circularId,
			Model model) 
	{
		try {
			User loginUser = userDetails.getUser();
		    Circular circular = circularService.getCircularById(circularId)
		        .orElseThrow(() -> new IllegalArgumentException("指定した投稿が見つかりません"));
		    
			CircularEditForm form = new CircularEditForm();
			form.setName(circular.getName());
		    form.setDescription(circular.getDescription());
		    form.setIsUrgent(circular.getIsUrgent());
		    
			model.addAttribute("form", form);
			model.addAttribute("circularId", circularId);
			return "circular/edit-page";
		} catch(IllegalArgumentException e) {
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("form", new CircularEditForm());
	        return "circular/edit-page";
		}
	}
	
	@PostMapping("/circular/control/edit/{id}")
	public String circularEdit(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes,
			Model model,
			@Valid @ModelAttribute("form") CircularEditForm form, BindingResult result,
			@PathVariable("id") int circularId,
			@PageableDefault(size = 20)Pageable pageable) 
	{
		
		if(result.hasErrors()) {
			System.out.println("バリデーションエラー" + result.getAllErrors());
			model.addAttribute("form", form);
			return "circular/edit-page";
		}
		
		User loginUser = userDetails.getUser();
		try {
			circularService.editCircularById(circularId, form, loginUser);
			redirectAttributes.addFlashAttribute("successMessage", "正常に編集されました");
		} catch(IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		} catch(RuntimeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		Page<Circular> circularPage = circularService
				.getLatestCirculars(loginUser.getDistrict().getAssociation(), pageable);
		redirectAttributes.addFlashAttribute("circularPage", circularPage);
		
		return "redirect:/circular/control-page";
	}
	
	@PostMapping("/circular/control/delete/{id}")
	public String circularDelete(
			@PathVariable("id") int circularId,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes,
			@PageableDefault(size = 20)Pageable pageable)
	{
		User loginUser = userDetails.getUser();
		
		
		try {
			Circular circular = circularService.getCircularById(circularId)
					.orElseThrow(() -> new IllegalArgumentException
							("指定した回覧板は存在しないか既に削除されています"));
			
			circularFileService.softDeletedCircularFile(circular);
			circularService.softDeletedCircular(circular);
			redirectAttributes.addFlashAttribute("successMessage", "回覧板を削除しました");
			
		} catch(IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/circular/control-page";
		}
		
		
		
		Page<Circular> circularPage = circularService
				.getLatestCirculars(loginUser.getDistrict().getAssociation(), pageable);
		redirectAttributes.addFlashAttribute("circularPage", circularPage);
		
		return "redirect:/circular/control-page";
	}
}
