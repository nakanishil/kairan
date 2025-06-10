package com.example.kairan.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.kairan.entity.Circular;
import com.example.kairan.entity.User;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.CircularReadService;
import com.example.kairan.service.CircularService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	private final CircularService circularService;
	
	private final CircularReadService circularReadService;
	
	@GetMapping("/")
	public String index(
			Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			HttpSession session) 
	{
		User loginUser = userDetails.getUser();
		String association = loginUser.getDistrict().getAssociation();
		
	    logger.info("HomeController: / にアクセスされました");
	    
	    
	    if (Boolean.TRUE.equals(session.getAttribute("google_link_success"))) {
	        model.addAttribute("successMessage", "Googleアカウントとの連携が完了しました！");
	        session.removeAttribute("google_link_success");
	    }
	    // 回覧板データ取得 3件
	    List<Circular> circularTopThree = circularService.getLatestCircularsThree(association);
	    logger.info("回覧板データ数: {}", circularTopThree.size());
	    
	 // 既読Map作成
	    Map<Integer, Boolean> readMap = new HashMap<>();
	    for (Circular circular : circularTopThree) {
	        boolean isRead = circularReadService.readStatusCheck(loginUser, circular);
	        readMap.put(circular.getId(), isRead);
	    }
	    
	    if (circularTopThree.isEmpty()) {
	    	model.addAttribute("message", "回覧板のデータがありません");
	    }
	    
	    model.addAttribute("readMap", readMap);
	    model.addAttribute("circularTopThree", circularTopThree);
		return "index";
	}
}
