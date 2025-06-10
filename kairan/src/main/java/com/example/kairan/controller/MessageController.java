package com.example.kairan.controller;

import java.security.Principal;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.kairan.entity.BoardType;
import com.example.kairan.entity.Message;
import com.example.kairan.entity.User;
import com.example.kairan.form.ReplyForm;
import com.example.kairan.form.ThreadForm;
import com.example.kairan.repository.BoardTypeRepository;
import com.example.kairan.repository.UserRepository;
import com.example.kairan.service.MessageService;
import com.example.kairan.service.UserService;

@Controller
public class MessageController {
	private final MessageService messageService;
	private final UserService userService;
	private final BoardTypeRepository boardTypeRepository;
	private final UserRepository userRepository;

	public MessageController(MessageService messageService,UserService userService, BoardTypeRepository boardTypeRepository, UserRepository userRepository) {
		this.messageService = messageService;
		this.userService = userService;
		this.boardTypeRepository = boardTypeRepository;
		this.userRepository = userRepository;
	}

	@GetMapping("/message/threads/{boardTypeId}")
	public String showThreads(@PathVariable int boardTypeId, Model model, Principal principal, Pageable pageable) {
		// ログインユーザの情報を取得
		User user = userService.findByUserId(principal.getName());
		String userRole = user.getRole().getName();
		Integer userCommitteeId = user.getCommittee() != null ? user.getCommittee().getId() : null;
		Integer userDistrictId = user.getDistrict().getId();
		BoardType selectedBoardType;
		
		// 掲示板情報を取得
		try {
			selectedBoardType = boardTypeRepository.findById(boardTypeId)
				.orElseThrow(() -> new IllegalArgumentException("無効な掲示板タイプです"));
		} catch(IllegalArgumentException e) {
			model.addAttribute("errorMessage", e.getMessage());
		    return "error/custom-error"; // エラーページにリダイレクト
		}

		// 閲覧権限のチェック
		if (!messageService.hasPermissionToView(selectedBoardType, userRole)) {
			throw new ForbiddenException("この掲示板を閲覧する権限がありません");
		}

		// スレッド一覧を取得
		Page<Message> threads = messageService.getThreadsByBoardTypeAndFilterPages(
				boardTypeId, userRole, userCommitteeId, userDistrictId, user.getDistrict().getAssociation(), pageable
				);


		model.addAttribute("threads", threads);
		model.addAttribute("boardTypeId", boardTypeId);
		model.addAttribute("selectedBoardType", selectedBoardType);
		model.addAttribute("userRole", userRole);
		model.addAttribute("threadForm", new ThreadForm());

		return "message/threads";

	}

	// スレッド作成
	@PostMapping("/messages/threads/{boardTypeId}/create")
	public String createThread(@PathVariable int boardTypeId,
			@Valid @ModelAttribute("threadForm") ThreadForm threadForm,
			BindingResult result,
			Principal principal,
			Model model,
			Pageable pageable) {

		User user = userService.findByUserId(principal.getName());

		if (user == null) {
			throw new IllegalStateException("ユーザーが見つかりません: " + principal.getName());
		}

		// バリデーションエラー
		if (result.hasErrors()) {
			BoardType selectedBoardType = boardTypeRepository.findById(boardTypeId)
					.orElse(null); 

			model.addAttribute("selectedBoardType", selectedBoardType);
			model.addAttribute("boardTypeId", boardTypeId);
			model.addAttribute("threadForm", threadForm);
			
			Integer committeeId = user.getCommittee() != null ? user.getCommittee().getId() : null;
	        Integer districtId = user.getDistrict().getId();
	        String userRole = user.getRole().getName();

	        Page<Message> threads = messageService.getThreadsByBoardTypeAndFilterPages(
	                boardTypeId, userRole, committeeId, districtId, user.getDistrict().getAssociation(), pageable);
	        model.addAttribute("threads", threads);
	        model.addAttribute("userRole", userRole);
			

			if (selectedBoardType == null) {
				model.addAttribute("errorMessage", "掲示板が見つかりません");
			}

			return "message/threads";
		}

		// スレッドを作成
		messageService.createThread(user, boardTypeId, threadForm.getTitle(), threadForm.getComment());

		return "redirect:/message/threads/" + boardTypeId; // 掲示板一覧にリダイレクト
	}

	// レス投稿
	@PostMapping("/message/thread/{threadId}/reply")
	public String createReply(@PathVariable int threadId,
			@Valid @ModelAttribute("replyForm") ReplyForm replyForm,
			BindingResult result,
			Principal principal,
			Model model,
			Pageable pageable)
	{
		Message thread = messageService.getMessageById(threadId);
	    BoardType selectedBoardType = thread.getBoardType();
	    User user = userService.findByUserId(principal.getName());
		
		if(result.hasErrors()) {
			Page<Message> replies = messageService.getReplies(threadId, pageable);

	        model.addAttribute("thread", thread);
	        model.addAttribute("selectedBoardType", selectedBoardType);
	        model.addAttribute("replies", replies);
	        model.addAttribute("replyForm", replyForm);

	        return "message/thread";
		}

		messageService.createReply(user, threadId, replyForm.getComment());

		return "redirect:/message/thread/" + threadId;
	}

	// ソフトデリート 
	@PostMapping("/thread/softdelete/{messageId}")
	public String softDeleteMessage(@PathVariable int messageId, Principal principal, RedirectAttributes redirectAttributes) {
		User user = userService.findByUserId(principal.getName());
		Message message = messageService.getMessageById(messageId);


		if (!message.getUser().equals(user)) {
			throw new SecurityException("削除権限がありません");
		}

		Integer parentId = message.getParentId();
		BoardType boardType = message.getBoardType();

		// ソフトデリート処理
		messageService.deleteMessage(messageId);

		if (parentId == null) {
			redirectAttributes.addFlashAttribute("successMessage", "スレッドを削除しました");
			return "redirect:/message/threads/" + boardType.getId();
		} else {
			redirectAttributes.addFlashAttribute("successMessage", "コメントを削除しました");
			return "redirect:/message/thread/" + parentId;
		}
	}


	@GetMapping("/message/thread/{threadId}")
	public String showThreadDetail(@PathVariable int threadId, Model model, Pageable pageable)	{

	    try {
	        Message thread = messageService.getMessageById(threadId);

	        if (thread == null || thread.isDeleted()) {
	            throw new IllegalArgumentException("指定されたスレッドが見つかりません");
	        }

	        BoardType selectedBoardType = thread.getBoardType();

	        if (selectedBoardType == null) {
	            throw new IllegalArgumentException("スレッドに紐づく掲示板情報が見つかりません");
	        }

	        Page<Message> replies = messageService.getReplies(threadId, pageable);

	        model.addAttribute("thread", thread);
	        model.addAttribute("selectedBoardType", selectedBoardType);
	        model.addAttribute("replies", replies);
	        model.addAttribute("replyForm", new ReplyForm());

	        return "message/thread";

	    } catch (IllegalArgumentException e) {
	        model.addAttribute("errorMessage", e.getMessage());
	        return "error/custom-error"; // 独自エラーページへ
	    }
	}

	@ResponseStatus(HttpStatus.FORBIDDEN) // ✅ 403エラーを返す
	public class ForbiddenException extends RuntimeException {
		public ForbiddenException(String message) {
			super(message);
		}
	}

	// 編集画面を表示
	@GetMapping("/message/edit/{id}")
	public String showEditForm(@PathVariable int id, Model model, Principal principal) {
		Message message = messageService.getMessageById(id);
		
		// 権限チェック （投稿者のみ編集可能）
		if (!message.getUser().getUserId().equals(principal.getName())) {
			throw new SecurityException("編集権限がありません");
		}
		
		// スレッドの場合
		if (message.getTitle() != null) {
			ThreadForm threadForm = new ThreadForm();
			threadForm.setTitle(message.getTitle());
			threadForm.setComment(message.getComment());
			
			model.addAttribute("threadForm", threadForm);
			model.addAttribute("message", message);
			
			return "message/edit";
		} else {
			// レスの場合
			ReplyForm replyForm = new ReplyForm();
			replyForm.setComment(message.getComment());
			
			model.addAttribute("replyForm", replyForm);
			model.addAttribute("message", message);
			
			return "message/edit";
		}
		
	}
	
	@PostMapping("/message/edit/{id}")
	public String updateMessage(@PathVariable("id") int messageId,
			@Valid @ModelAttribute("threadForm") ThreadForm threadForm,
			BindingResult threadresult,
			@Valid @ModelAttribute("replyForm") ReplyForm replyForm,
			BindingResult replyresult,
			Principal principal,
			RedirectAttributes redirectAttributes,
			Model model) {
		
		
		
		Message message = messageService.getMessageById(messageId);
		
		// 権限チェック
		if (!message.getUser().getUserId().equals(principal.getName())) {
			redirectAttributes.addFlashAttribute("errorMessage", "編集権限がありません");
		        throw new SecurityException("編集権限がありません");
		    }
		
		if(message.getParentId() == null) {
			if (threadresult.hasErrors()) {
				model.addAttribute("errorMessage", "入力に誤りがあります。");
				model.addAttribute("message", message);
				return "message/edit";
			}
			messageService.updateMessageThread(message, threadForm.getTitle(), threadForm.getComment());
			redirectAttributes.addFlashAttribute("successMessage", "スレッドを編集しました");
			return "redirect:/message/threads/" + message.getBoardType().getId();
		} else {
			if (replyresult.hasErrors()) {
				return "message/edit";
			}
			messageService.updateMessageReply(message, replyForm.getComment());
			redirectAttributes.addFlashAttribute("successMessage", "コメントを編集しました");
			return "redirect:/message/thread/" + message.getParentId();
		}
		
		
	}



}
