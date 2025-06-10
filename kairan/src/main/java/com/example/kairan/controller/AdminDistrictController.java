package com.example.kairan.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kairan.entity.District;
import com.example.kairan.entity.User;
import com.example.kairan.form.AdminDistrictRegiForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.AdminDistrictService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/district")
public class AdminDistrictController {
	
	private final AdminDistrictService adminDistrictService;
	
	// 管轄の町内会表示
	@GetMapping("/association-list")
	public String showDistrictList(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PageableDefault(size = 20)Pageable pageable,
			Model model
			)
	{
		User loginUser = userDetails.getUser();
		String regionCode = loginUser.getDistrict().getRegionCode();
		
		Page<District> listPage = adminDistrictService.getDistinctDistrictsByAssociation(regionCode, pageable);
		model.addAttribute("listPage", listPage);
		return "admin/district/association-list";
	}
	
	// 町内会の詳細ページ
	@GetMapping("/association-detail/{id}")
	public String showDistrictDetail(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable("id") int districtId,
			RedirectAttributes redirectAttributes,
			Model model) 
	{
		User loginUser = userDetails.getUser();
		List<User> mayorsList = adminDistrictService
				.getMayors(loginUser);
		List<District> districtList = adminDistrictService
				.getDistrictsByAssociationSorted(loginUser);
		
		
		
		model.addAttribute("mayorsList", mayorsList);
		model.addAttribute("districtList", districtList);
		
		return "admin/district/association-detail";
	}
	
	// 新規district登録ページ
	@GetMapping("/register-page")
	public String regiDistrictPage(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@ModelAttribute("form") AdminDistrictRegiForm form,
			Model model) 
	{
		
		
		model.addAttribute("form", form);
		return "admin/district/register-page";
	}
	
	// district登録処理
	@PostMapping("/register")
	public String regiDistrict(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@Valid @ModelAttribute("form") AdminDistrictRegiForm form, BindingResult result,
			RedirectAttributes redirectAttributes,
			Model model)
	{
		// バリデーションエラー
		if (result.hasErrors()) {
			System.out.println("バリデーションエラー" + result.getAllErrors());
			model.addAttribute("errorMessage", "エラーが発生しました。");
			return "admin/district/register-page";
		}
		
		try {
			// district作成
			District district = adminDistrictService.regiDistrict(form);
			// 仮会長、区長の作成
			List<User> tempUsers = adminDistrictService.casualUserRegi(district);
			User kaicho = tempUsers.get(0);
			User kucho = tempUsers.get(1);
			
			// デバッグ
			System.out.println(district.getName());
			System.out.println(kaicho.getName());
			System.out.println(kucho.getName());
			
			model.addAttribute("district", district);
			model.addAttribute("kaicho", kaicho);
			model.addAttribute("kucho", kucho);
		} catch(IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/admin/district/association-list";

		}
		model.addAttribute("successMessage", "町内会の作成に成功しました");
		return "admin/district/new-district-detail";
	}
	
}
