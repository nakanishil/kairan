package com.example.kairan.controller;

import java.time.Year;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kairan.entity.MembershipFee;
import com.example.kairan.entity.User;
import com.example.kairan.form.MembershipFeeEditForm;
import com.example.kairan.form.MembershipFeeForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.MembershipFeeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MembershipFeeController {
	private final MembershipFeeService membershipFeeService;


	// 年会費登録画面表示
	@GetMapping("/membership-fee/register-page")
	public String registerPage(
			Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PageableDefault(size = 10) Pageable pageable
			) 
	{
		// モデルに form がなければ新しく追加（これでバリデーション後のformも反映される）
	    if (!model.containsAttribute("form")) {
	        model.addAttribute("form", new MembershipFeeForm());
	    }

		User loginUser = userDetails.getUser();
		Page<MembershipFee> membershipFeePage = membershipFeeService.
				getMembershipFeeList(loginUser, pageable);
		
		// 現在の年を送る
		int currentYear = Year.now().getValue();
		model.addAttribute("currentYear", currentYear);
		model.addAttribute("membershipFeePage", membershipFeePage);

		return "membership-fee/register-page";
	}


	// 年会費登録処理
	@PostMapping("/membership-fee/register")
	public String registerMembershipFee(
			@Valid @ModelAttribute("form") MembershipFeeForm form, BindingResult result,
			RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			Model model,
			@PageableDefault(size = 10) Pageable pageable
			) 
	{
		User loginUser = userDetails.getUser();

		if (result.hasErrors()) {
		    Page<MembershipFee> membershipFeePage = membershipFeeService
		           .getMembershipFeeList(loginUser, pageable);

		    model.addAttribute("membershipFeePage", membershipFeePage); // テーブル再表示用
		    model.addAttribute("form", form);

		    return "membership-fee/register-page";
		}


		//重複チェック
		if(membershipFeeService.sameDistrictIdAndYear(
				loginUser.getDistrict().getId(), form.getYear()).isPresent()) {
			redirectAttributes.addFlashAttribute("errorMessage", "同じ年の年会費は既に登録されています");
			redirectAttributes.addFlashAttribute("form", form);
			return "redirect:/membership-fee/register-page";
		}

		membershipFeeService.regiMembershipFee(form, loginUser);

		redirectAttributes.addFlashAttribute("successMessage", "町内会費を設定しました");
		return "redirect:/membership-fee/register-page";
	}

	// 年会費編集ページ
	@GetMapping("membership-fee/edit-page/{id}")
	public String editMembershipFeePage(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable("id") int id,
			Model model)
	{
		User loginUser = userDetails.getUser();
		MembershipFee membershipFee = 
				membershipFeeService.findByDistrictAndDeletedAtIsNull(id)
				.orElseThrow(() -> new IllegalArgumentException(
						"指定したIDが存在しないか削除されています"));

		MembershipFeeEditForm form = new MembershipFeeEditForm();
		form.setId(membershipFee.getId());
		form.setYear(membershipFee.getYear());
		form.setAmount(membershipFee.getAmount().intValue());

		model.addAttribute("form", form);
		return "membership-fee/edit-page";
	}

	// 年会費編集
	@PostMapping("membership-fee/edit")
	public String editMembershipFee(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@Valid  @ModelAttribute("form") MembershipFeeEditForm form, BindingResult result,
			RedirectAttributes redirectAttributes,
			Model model
			)
	{
		User loginUser = userDetails.getUser();

		if (result.hasErrors()) {
			System.out.println("バリデーションエラー" + result.getAllErrors());
			model.addAttribute("errorMessage", "エラーが発生しました。");
			model.addAttribute("form", form);
			return "membership-fee/edit-page";
		}
		
		// 重複チェック（自分自身を除外）
		if (membershipFeeService.sameDistrictIdAndYear(
				loginUser.getDistrict().getId(), form.getYear()
				).map(existing -> !existing.getId().equals(form.getId())).orElse(false)) {

			model.addAttribute("errorMessage", "同じ年の年会費は既に登録されています");
			model.addAttribute("form", form);
			return "membership-fee/edit-page";
		}

		try {
			membershipFeeService.editMembershipFee(form, loginUser);
			redirectAttributes.addFlashAttribute("successMessage", "町内会費設定を変更しました");
		} catch(IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/membership-fee/register-page";
	}

	// 削除
	@PostMapping("membership-fee/delete/{id}")
	public String softDeletedMembershipFee(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable("id") int id,
			RedirectAttributes redirectAttributes,
			Model model
			)
	{
		try {
			membershipFeeService.softDeletedMembershipFee(id);
			redirectAttributes.addFlashAttribute("successMessage", "町内会費設定を削除しました");
		} catch(IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/membership-fee/register-page";
	}

}
