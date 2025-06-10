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

import com.example.kairan.entity.CommitteeClassification;
import com.example.kairan.entity.User;
import com.example.kairan.form.CommitteeRegiForm;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.CommitteeClassificationService;

import lombok.RequiredArgsConstructor;

@Controller
//必要な引数だけのコンストラクタを自動生成してくれる finalだけ
@RequiredArgsConstructor
public class CommitteeClassificationController {
	
	private final CommitteeClassificationService committeeClassificationService;
	
	@GetMapping("/committee/list")
	public String showCommitteeList(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes,
			Model model)
	{
		// ログインユーザ情報取得
		int districtId = userDetails.getUser().getDistrict().getId();
		String association = userDetails.getUser().getDistrict().getAssociation();
		
		// 閲覧権限
		User loginUser = userDetails.getUser();
		if(!committeeClassificationService.permissions(loginUser)) {
			throw new AccessDeniedException("閲覧権限がありません");
		}
		
		// 委員区分一覧取得
		List<CommitteeClassification> committeeList = 
				committeeClassificationService
				.getCommitteeSameDistrictIdAssosiationAsk(districtId, association	);
		
		// 委員登録フォーム渡す
		model.addAttribute("association", association);
		model.addAttribute("committeeList", committeeList);
		model.addAttribute("committeeRegiForm", new CommitteeRegiForm());
		return "committee/list";
	}
	
	// カテゴリを登録
	@PostMapping("/committee/register")
	public String committeeRegi(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes,
			@Valid @ModelAttribute("committeeRegiForm") CommitteeRegiForm form, BindingResult result,
			Model model
			)
	{
		// 閲覧権限
		User loginUser = userDetails.getUser();
		if(!committeeClassificationService.permissions(loginUser)) {
			throw new AccessDeniedException("閲覧権限がありません");
		}
		
		int districtId = userDetails.getUser().getDistrict().getId();
        String association = userDetails.getUser().getDistrict().getAssociation();
				
		if (result.hasErrors()) {
			System.out.println("バリデーションエラー" + result.getAllErrors());
			model.addAttribute("errorMessage", "エラーが発生しました。");
			
	        
	        List<CommitteeClassification> committeeList =
	                committeeClassificationService
	                .getCommitteeSameDistrictIdAssosiationAsk(districtId, association);

	        model.addAttribute("association", association);
	        model.addAttribute("committeeList", committeeList);
			
			return "committee/list";
		}
		try {
			committeeClassificationService.committeeCategoryRegi(form, loginUser);
			redirectAttributes.addFlashAttribute("successMessage", "委員を追加しました。");
			return "redirect:/committee/list";
		
		} catch (IllegalArgumentException e) {
			// 上限超過 or 重複登録時
			model.addAttribute("errorMessage", e.getMessage());
			
			// 委員区分一覧取得
			List<CommitteeClassification> committeeList = 
					committeeClassificationService
					.getCommitteeSameDistrictIdAssosiationAsk(districtId, association);
			
			model.addAttribute("association", association);
		    model.addAttribute("committeeList", committeeList);
		    model.addAttribute("committeeRegiForm", form); // 入力エラー時のフォームも再セット
			
			return "committee/list";
		}
	}
	
	
	// カテゴリをソフトデリート
	@PostMapping("/committee/delete/{id}")
	public String softdeleteCommittee(
			@PathVariable("id") int committeeId,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes
			)
	{
		try {
			User loginUser = userDetails.getUser();
			committeeClassificationService.softDeletedCommittee(loginUser, committeeId);
			redirectAttributes.addFlashAttribute("successMessage", "委員を削除しました。");
		} catch (IllegalArgumentException | IllegalStateException e){
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		
		return "redirect:/committee/list";
	}
	
	// カテゴリを更新
	@PostMapping("/committee/edit/{id}")
	public String editCommittee(
			@PathVariable("id") int committeeId,
			@Valid @ModelAttribute("committeeRegiForm") CommitteeRegiForm form, BindingResult result,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes
			) 
	{
		
		try {
			User loginUser = userDetails.getUser();
			committeeClassificationService.editCommittee(loginUser, committeeId, form);
			redirectAttributes.addFlashAttribute("successMessage", "委員名を変更しました。");
		} catch (IllegalArgumentException | IllegalStateException e){
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		
		
		return "redirect:/committee/list";
	}
}
