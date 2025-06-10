package com.example.kairan.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kairan.entity.District;
import com.example.kairan.entity.User;
import com.example.kairan.form.DistrictEditForm;
import com.example.kairan.form.DistrictRegiForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.DistrictService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DistrictController {

	private final DistrictService districtService;

	// 区一覧ページ 
	@GetMapping("/district/area-list")
	public String showAreaList(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			Model model) {
		// 閲覧権限
		User loginUser = userDetails.getUser();
		if(!districtService.permissions(loginUser)) {
			throw new AccessDeniedException("閲覧権限がありません");
		}

		// Area取得 同市同町内(regionCode, districtAssociation)ソフトデリートを除くname昇順を検索
		List<District> districtList = districtService.sameRegionCodeAssociationArea(loginUser);

		String association = loginUser.getDistrict().getAssociation();
		model.addAttribute("association", association);
		model.addAttribute("districtList",districtList);
		model.addAttribute("form", new DistrictRegiForm());

		return "district/area-list";
	}

	// 区の登録フォームページ
	@GetMapping("/district/register-page")
	public String districtRegiPage(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			Model model) 
	{
		DistrictRegiForm form = new DistrictRegiForm();
		User loginUser = userDetails.getUser();
		String association = loginUser.getDistrict().getAssociation();

		model.addAttribute("association", association);
		model.addAttribute("form", form);
		return "district/register-page";
	}

	// 登録
	@PostMapping("/district/register")
	public String districtRegi(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes,
			@Valid @ModelAttribute("form") DistrictRegiForm form, BindingResult result,
			Model model)
	{
		// 閲覧権限
		User loginUser = userDetails.getUser();
		if(!districtService.permissions(loginUser)) {
			throw new AccessDeniedException("閲覧権限がありません");
		}

		if (result.hasErrors()) {
			System.out.println("バリデーションエラー" + result.getAllErrors());
			model.addAttribute("errorMessage", "エラーが発生しました。");
			model.addAttribute("form", form);
			return "district/register-page";
		}

		try {
			districtService.districtCategoryRegi(form, loginUser);
			redirectAttributes.addFlashAttribute("successMessage", "区を追加しました。");
			return "redirect:/district/area-list";
		} catch (IllegalArgumentException e){
			// 上限超過 or 重複登録時
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("form", form);

			return "district/register-page";
		}
	}

	// 区カテゴリをソフトデリート
	@PostMapping("/district/delete/{id}")
	public String softDeleteDistrict(
			@PathVariable("id") int districtId,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes) 

	{		
		try {
			User loginUser = userDetails.getUser();
			districtService.softDeletedDistrict(loginUser, districtId);
			redirectAttributes.addFlashAttribute("successMessage", "削除しました。");

		} catch(IllegalArgumentException | IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/district/area-list";
	}
	
	// 編集ページ
	@GetMapping("/district/edit-page/{id}")
	public String showEditPage(
			@PathVariable("id") int districtId,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			Model model) 
	{
		// 閲覧権限
		User loginUser = userDetails.getUser();
		if(!districtService.permissions(loginUser)) {
			throw new AccessDeniedException("閲覧権限がありません");
		}

		// 編集対象取得
		District district = districtService.findByIdAndDeletedAtIsNull(districtId);

		// フォームにセット
		DistrictEditForm form = new DistrictEditForm();
		form.setId(district.getId());
		form.setName(district.getName());
		form.setArea(district.getArea());
		form.setDescription(district.getDescription());


		model.addAttribute("association", district.getAssociation());
		model.addAttribute("form", form);
		return "district/edit-page";
	}

	// 区を更新
	@PostMapping("/district/edit/{id}")
	public String editDistrict(
			@PathVariable("id") int districtId,
			@Valid @ModelAttribute("form") DistrictEditForm form, BindingResult result,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes,
			Model model
			) 
	{
		// バリデーションチェック
		if (result.hasErrors()) {
			model.addAttribute("form", form);
			return "district/edit-page";
		}

		try {
			User loginUser = userDetails.getUser();
			districtService.editDistrict(loginUser, districtId, form);
			redirectAttributes.addFlashAttribute("successMessage", "区情報を更新しました。");

		} catch (IllegalArgumentException | IllegalStateException e){
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("form", form);
		}

		return "redirect:/district/area-list";

	}

}
