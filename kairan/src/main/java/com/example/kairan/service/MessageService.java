package com.example.kairan.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kairan.entity.BoardType;
import com.example.kairan.entity.Message;
import com.example.kairan.entity.User;
import com.example.kairan.repository.BoardTypeRepository;
import com.example.kairan.repository.MessageRepository;

@Service
public class MessageService {

	private final MessageRepository messageRepository;
	private final BoardTypeRepository boardTypeRepository;

	public MessageService(MessageRepository messageRepository, BoardTypeRepository boardTypeRepository) {
		this.messageRepository = messageRepository;
		this.boardTypeRepository = boardTypeRepository;
	}

	// 指定された掲示板のスレッド一覧を取得
	public List<Message> getThreadsByBoardType(int boardTypeId){
		BoardType boardType = boardTypeRepository.findById(boardTypeId)
				.orElseThrow(() -> new IllegalArgumentException("無効な掲示板タイプ"));

		return messageRepository.findByBoardTypeAndParentIdIsNullAndDeletedAtIsNull(boardType);
	}


	// 指定されたスレッドのレス一覧を取得
	public List<Message> getReplies(int parentId) {
		return messageRepository.findByParentIdAndDeletedAtIsNull(parentId);
	}

	// スレッド作成（上位者のみ）
	@Transactional
	public Message createThread(User user, int boardTypeId, String title, String comment) {
		BoardType boardType = boardTypeRepository.findById(boardTypeId)
				.orElseThrow(() -> new IllegalArgumentException("無効な掲示板タイプ"));

		if (!canCreateThread(user, boardType)) {
			throw new IllegalStateException("スレッド作成権限がありません");
		}

		Message message = new Message();
		message.setUser(user);
		message.setBoardType(boardType);
		message.setTitle(title);
		message.setComment(comment);
		
		if(user.getCommittee() != null) {
			message.setCommitteeType(user.getCommittee());
		}

		return messageRepository.save(message);
	}

	// レス投稿
	public Message createReply(User user, int threadId, String comment) {
		Message parentThreadOpt = messageRepository.findById(threadId)
				.orElseThrow(() -> new IllegalArgumentException("指定したスレッドが存在しません"));

		Message reply = new Message();
		reply.setUser(user);
		reply.setBoardType(parentThreadOpt.getBoardType());
		reply.setParentId(threadId);
		reply.setComment(comment);
		reply.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		reply.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		
		if(user.getCommittee() != null) {
			reply.setCommitteeType(user.getCommittee());
		}

		return messageRepository.save(reply);
	}

	private static final List<String> THREAD_CREATORS = List.of(
			"ROLE_行政", "ROLE_町内会長", "ROLE_委員長", "ROLE_区長"
			);

	// スレッド作成の権限チェック
	protected Boolean canCreateThread(User user, BoardType boardType) {
		String roleName = user.getRole().getName(); // 役職名(ROLE_〇〇)

		return THREAD_CREATORS.contains(roleName); // 設定リストに含まれていればOK
	}

	// ソフトデリート
	@Transactional
	public void deleteMessage(int messageId) {
		Message message = getMessageById(messageId);
		message.softDelete();
		message.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		messageRepository.save(message);
	}

	// メッセージIDからメッセージを取得
	public Message getMessageById(int messageId) {
		return messageRepository.findById(messageId)
				.orElseThrow(() -> new IllegalArgumentException("指定したIDが存在しないか削除されています"));

	}

	public boolean hasPermissionToView(BoardType boardType, String userRole) {
		String boardName = boardType.getBoardName().trim();
		userRole = userRole.trim(); // 役職名の前後の空白を除去

		System.out.println("🔍 Debug - boardName: [" + boardName + "]");
		System.out.println("🔍 Debug - userRole: [" + userRole + "]");

		boolean result = false;

		if (boardName.equalsIgnoreCase("行政会長掲示板")) {
			result = userRole.equalsIgnoreCase("ROLE_行政") || userRole.equalsIgnoreCase("ROLE_町内会長");
		} else if (boardName.equalsIgnoreCase("会長委員長掲示板")) {
			result = userRole.equalsIgnoreCase("ROLE_町内会長") || userRole.equalsIgnoreCase("ROLE_委員長");
		} else if (boardName.equalsIgnoreCase("委員長委員掲示板")) {
			result = userRole.equalsIgnoreCase("ROLE_委員長") || userRole.equalsIgnoreCase("ROLE_委員");
		} else if (boardName.equalsIgnoreCase("会長区長掲示板")) {
			result = userRole.equalsIgnoreCase("ROLE_町内会長") || userRole.equalsIgnoreCase("ROLE_区長");
		} else if (boardName.equalsIgnoreCase("区長会員掲示板")) {
			result = userRole.equalsIgnoreCase("ROLE_区長") 
					|| userRole.equalsIgnoreCase("ROLE_会員") 
					|| userRole.equalsIgnoreCase("ROLE_委員")  // ★追加
					|| userRole.equalsIgnoreCase("ROLE_委員長"); // ★追加
		} else {
			System.out.println("❌ 未知の掲示板: [" + boardName + "]");
		}

		System.out.println("🛠 許可: " + result);
		return result;
	}

	public boolean hasPermissionToCreateThread(BoardType boardType, String userRole) {
		String boardName = boardType.getBoardName().trim();
		userRole = userRole.trim(); // 役職名の前後の空白を除去

		System.out.println("🔍 Debug - boardName: [" + boardName + "]");
		System.out.println("🔍 Debug - userRole: [" + userRole + "]");

		boolean result = false;

		if (boardName.equalsIgnoreCase("行政会長掲示板")) {
			result = userRole.equalsIgnoreCase("ROLE_行政");
		} else if (boardName.equalsIgnoreCase("会長委員長掲示板")) {
			result = userRole.equalsIgnoreCase("ROLE_町内会長");
		} else if (boardName.equalsIgnoreCase("委員長委員掲示板")) {
			result = userRole.equalsIgnoreCase("ROLE_委員長");
		} else if (boardName.equalsIgnoreCase("会長区長掲示板")) {
			result = userRole.equalsIgnoreCase("ROLE_町内会長");
		} else if (boardName.equalsIgnoreCase("区長会員掲示板")) {
			result = userRole.equalsIgnoreCase("ROLE_区長");
		} else {
			System.out.println("❌ 未知の掲示板: [" + boardName + "]");
		}

		System.out.println("🛠 スレッド作成許可: " + result);
		return result;
	}

	public Page<Message> getThreadsByBoardTypeAndFilterPages(
			int boardTypeId, String userRole, Integer userCommitteeId,
			Integer userDistrictId, String userAssociation, Pageable pageable){

		// 行政の掲示板制限 1だけ
		if ("ROLE_行政".equals(userRole) && boardTypeId == 1 ) {
			return messageRepository
					.findByBoardTypeIdAndUserDistrictAssociationAndDeletedAtIsNullAndParentIdIsNullOrderByUpdatedAtDesc(
							boardTypeId, userAssociation, pageable);
		}

		// 町内会長の掲示板制限 (1, 2, 4 のみ)
		if ("ROLE_町内会長".equals(userRole) && (boardTypeId == 1 || boardTypeId == 2 || boardTypeId == 4)) {
			return messageRepository
					.findByBoardTypeIdAndUserDistrictAssociationAndDeletedAtIsNullAndParentIdIsNullOrderByUpdatedAtDesc(
							boardTypeId, userAssociation, pageable);
		}

		// 委員長の掲示板制限 会長委員長掲示板
		if ("ROLE_委員長".equals(userRole) && boardTypeId == 2) {
			return messageRepository
					.findByBoardTypeIdAndUserDistrictAssociationAndDeletedAtIsNullAndParentIdIsNullOrderByUpdatedAtDesc(
							boardTypeId, userAssociation, pageable);
		}


		// 委員長・委員掲示板はcommittee_id,associationでフィルタ
		if (("ROLE_委員長".equals(userRole) || "ROLE_委員".equals(userRole)) && boardTypeId == 3) {
			return messageRepository
					.findByBoardTypeIdAndCommitteeTypeIdAndUserDistrictAssociationAndDeletedAtIsNullAndParentIdIsNullOrderByUpdatedAtDesc(
							boardTypeId, userCommitteeId, userAssociation, pageable); 
		}

		// 会長区長掲示板
		if ("ROLE_区長".equals(userRole) && boardTypeId == 4) {
			return messageRepository
					.findByBoardTypeIdAndUserDistrictAssociationAndDeletedAtIsNullAndParentIdIsNullOrderByUpdatedAtDesc(
							boardTypeId, userAssociation, pageable);
		}

		// 区長・会員のスレッドはassociationでフィルタ
		if (("ROLE_区長".equals(userRole) || 
				"ROLE_会員".equals(userRole) || 
				"ROLE_委員".equals(userRole) ||
				"ROLE_委員長".equals(userRole)) &&
				boardTypeId == 5) {
			return messageRepository
					.findByBoardTypeIdAndUserDistrictAssociationAndDeletedAtIsNullAndParentIdIsNullOrderByUpdatedAtDesc(
							boardTypeId, userAssociation, pageable);
		}

		// デフォルト該当なしの場合
		return Page.empty(pageable) ;
	}

	public Page<Message> getReplies(int parentId, Pageable pageable) {
		return messageRepository.findByParentIdAndDeletedAtIsNullOrderByUpdatedAtDesc(parentId, pageable);
	}

	// スレッド更新
	@Transactional
	public Message updateMessageThread(Message message, String title, String comment) {
		message.setTitle(title);
		message.setComment(comment);
		message.onUpdate();

		return messageRepository.save(message);
	}

	// レスの更新
	@Transactional
	public Message updateMessageReply(Message message, String comment) {
		message.setComment(comment);
		message.onUpdate();

		return messageRepository.save(message);
	}


}
