package com.example.kairan.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.kairan.entity.BoardType;
import com.example.kairan.entity.District;
import com.example.kairan.entity.Message;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.repository.BoardTypeRepository;
import com.example.kairan.repository.UserRepository;
import com.example.kairan.security.UserDetailsImpl;
import com.example.kairan.service.MessageService;
import com.example.kairan.service.UserService;

@WebMvcTest(MessageController.class)
public class MessageControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private UserService userService;

	@MockBean
	private BoardTypeRepository boardTypeRepository;

	@MockBean
	private MessageService messageService;
	
	@MockBean
	private UserRepository userRepository; // ← これを追加！

	
	private User loginUser;
	
    private UserDetailsImpl userDetails;

	 @BeforeEach
	    void setUp() {
	        loginUser = new User();
	        loginUser.setId(1);
	        loginUser.setUserId("testuser");
	        loginUser.setName("テストユーザー");
	        loginUser.setEnabled(true);

	        Role role = new Role();
	        role.setName("ROLE_町内会長");
	        loginUser.setRole(role);

	        District district = new District();
	        district.setId(1);
	        district.setAssociation("A001");
	        loginUser.setDistrict(district);

	        userDetails = new UserDetailsImpl(
	            loginUser,
	            List.of(new SimpleGrantedAuthority(role.getName()))
	        );
	    }
// 一覧ページ
	 @Test
	 void showThreads_正常にスレッド一覧が表示される() throws Exception {
	     int boardTypeId = 1;

	     BoardType boardType = new BoardType();
	     boardType.setId(boardTypeId);
	     boardType.setBoardName("会長委員長掲示板");

	     Page<Message> mockThreads = new PageImpl<>(List.of());

	     // モック設定
	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);
	     when(boardTypeRepository.findById(boardTypeId)).thenReturn(Optional.of(boardType));
	     when(messageService.hasPermissionToView(boardType, loginUser.getRole().getName())).thenReturn(true);
	     when(messageService.getThreadsByBoardTypeAndFilterPages(
	             eq(boardTypeId),
	             eq(loginUser.getRole().getName()),
	             isNull(), // userCommitteeId が null
	             eq(loginUser.getDistrict().getId()),
	             eq(loginUser.getDistrict().getAssociation()),
	             any(Pageable.class)
	     )).thenReturn(mockThreads);

	     mockMvc.perform(get("/message/threads/{boardTypeId}", boardTypeId)
	             .with(user(userDetails)))
	             .andExpect(status().isOk())
	             .andExpect(view().name("message/threads"))
	             .andExpect(model().attributeExists("threads"))
	             .andExpect(model().attributeExists("boardTypeId"))
	             .andExpect(model().attributeExists("selectedBoardType"))
	             .andExpect(model().attributeExists("userRole"))
	             .andExpect(model().attributeExists("threadForm"));

	     verify(userService).findByUserId(loginUser.getUserId());
	     verify(boardTypeRepository).findById(boardTypeId);
	     verify(messageService).hasPermissionToView(boardType, loginUser.getRole().getName());
	 }
	 
	 @Test
	 void showThreads_存在しないBoardTypeIdの場合はエラーページが表示される() throws Exception {
	     int invalidBoardTypeId = 999;

	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);
	     when(boardTypeRepository.findById(invalidBoardTypeId)).thenReturn(Optional.empty());

	     mockMvc.perform(get("/message/threads/{boardTypeId}", invalidBoardTypeId)
	             .with(user(userDetails)))
	         .andExpect(status().isOk()) 
	         .andExpect(view().name("error/custom-error"))
	         .andExpect(model().attribute("errorMessage", "無効な掲示板タイプです"));
	 }
	 
	 @Test
	 void showThreads_閲覧権限がない場合はForbiddenExceptionがスローされる() throws Exception {
	     int boardTypeId = 1;

	     BoardType boardType = new BoardType();
	     boardType.setId(boardTypeId);
	     boardType.setBoardName("委員長掲示板");

	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);
	     when(boardTypeRepository.findById(boardTypeId)).thenReturn(Optional.of(boardType));
	     when(messageService.hasPermissionToView(boardType, loginUser.getRole().getName())).thenReturn(false);

	     mockMvc.perform(get("/message/threads/{boardTypeId}", boardTypeId)
	             .with(user(userDetails)))
	         .andExpect(status().isForbidden());
	 }

// スレッド作成
	 @Test
	 void createThread_正常にスレッドが作成されリダイレクトされる() throws Exception {
	     int boardTypeId = 1;

	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);

	     mockMvc.perform(post("/messages/threads/{boardTypeId}/create", boardTypeId)
	             .with(user(userDetails))
	             .with(csrf())
	             .param("title", "テストタイトル")
	             .param("comment", "テストコメント"))
	         .andExpect(status().is3xxRedirection())
	         .andExpect(redirectedUrl("/message/threads/" + boardTypeId));

	     // スレッド作成が呼ばれたか確認
	     verify(messageService).createThread(
	         eq(loginUser), eq(boardTypeId), eq("テストタイトル"), eq("テストコメント")
	     );
	 }
	 
	 @Test
	 void createThread_バリデーションエラーがある場合はスレッド一覧画面を再表示する() throws Exception {
	     int boardTypeId = 1;

	     BoardType boardType = new BoardType();
	     boardType.setId(boardTypeId);
	     boardType.setBoardName("委員長掲示板");

	     
	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);
	     
	     when(boardTypeRepository.findById(boardTypeId)).thenReturn(Optional.of(boardType));
	     
	     when(messageService.getThreadsByBoardTypeAndFilterPages(
	         eq(boardTypeId),
	         eq(loginUser.getRole().getName()),
	         isNull(),
	         eq(loginUser.getDistrict().getId()),
	         eq(loginUser.getDistrict().getAssociation()),
	         any(Pageable.class)
	     )).thenReturn(Page.empty());

	     mockMvc.perform(post("/messages/threads/{boardTypeId}/create", boardTypeId)
	             .with(user(userDetails))
	             .with(csrf())
	             .param("title", "") 
	             .param("comment", "テストコメント"))
	         .andExpect(status().isOk())
	         .andExpect(view().name("message/threads"))
	         .andExpect(model().attributeExists("threadForm"))
	         .andExpect(model().attributeExists("threads"))
	         .andExpect(model().attributeExists("selectedBoardType"))
	         .andExpect(model().attribute("boardTypeId", boardTypeId));
	 }
// リプライ、レス作成	 
	 @Test
	 void createReply_正常にレスが投稿されリダイレクトされる() throws Exception {
	     int threadId = 10;

	     Message thread = new Message();
	     thread.setId(threadId);
	     thread.setTitle("テストスレッド");

	     BoardType boardType = new BoardType();
	     boardType.setId(1);
	     boardType.setBoardName("掲示板");
	     thread.setBoardType(boardType);

	     when(messageService.getMessageById(threadId)).thenReturn(thread);
	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);

	     mockMvc.perform(post("/message/thread/{threadId}/reply", threadId)
	             .with(user(userDetails))
	             .with(csrf())
	             .param("comment", "テスト返信コメント"))
	         .andExpect(status().is3xxRedirection())
	         .andExpect(redirectedUrl("/message/thread/" + threadId));

	     verify(messageService).createReply(loginUser, threadId, "テスト返信コメント");
	 }
	 
	 @Test
	 void createReply_バリデーションエラーがある場合はスレッド詳細画面を再表示する() throws Exception {
	     int threadId = 10;

	     
	     Message thread = new Message();
	     thread.setId(threadId);
	     thread.setTitle("テストスレッド");
	     
	     thread.setUser(loginUser);

	     BoardType boardType = new BoardType();
	     boardType.setId(1);
	     boardType.setBoardName("掲示板");
	     thread.setBoardType(boardType);

	     
	     when(messageService.getMessageById(threadId)).thenReturn(thread);
	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);
	     when(messageService.getReplies(eq(threadId), any(Pageable.class)))
	         .thenReturn(Page.empty());

	     mockMvc.perform(post("/message/thread/{threadId}/reply", threadId)
	             .with(user(userDetails))
	             .with(csrf())
	             .param("comment", "")) 
	         .andExpect(status().isOk())
	         .andExpect(view().name("message/thread"))
	         .andExpect(model().attributeExists("replyForm"))
	         .andExpect(model().attributeExists("thread"))
	         .andExpect(model().attributeExists("selectedBoardType"))
	         .andExpect(model().attributeExists("replies"));
	 }

// 編集
	 @Test
	 void updateMessage_スレッドの編集が正常に完了しリダイレクトされる() throws Exception {
	     int messageId = 100;

	     
	     Message message = new Message();
	     message.setId(messageId);
	     message.setTitle("旧タイトル");
	     message.setComment("旧コメント");
	     message.setParentId(null); 

	     BoardType boardType = new BoardType();
	     boardType.setId(1);
	     message.setBoardType(boardType);

	     message.setUser(loginUser); 

	     when(messageService.getMessageById(messageId)).thenReturn(message);
	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);

	     mockMvc.perform(post("/message/edit/{id}", messageId)
	             .with(user(userDetails))
	             .with(csrf())
	             .param("title", "新しいタイトル")
	             .param("comment", "新しいコメント"))
	         .andExpect(status().is3xxRedirection())
	         .andExpect(redirectedUrl("/message/threads/" + boardType.getId()));

	     verify(messageService).updateMessageThread(message, "新しいタイトル", "新しいコメント");
	 }
	 
	 @Test
	 void updateMessage_スレッド編集でバリデーションエラーがある場合は編集画面を再表示する() throws Exception {
	     int messageId = 100;

	     Message message = new Message();
	     message.setId(messageId);
	     message.setTitle("旧タイトル");
	     message.setComment("旧コメント");
	     message.setParentId(null); 

	     BoardType boardType = new BoardType();
	     boardType.setId(1);
	     message.setBoardType(boardType);
	     message.setUser(loginUser); 

	     when(messageService.getMessageById(messageId)).thenReturn(message);
	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);

	     mockMvc.perform(post("/message/edit/{id}", messageId)
	             .with(user(userDetails))
	             .with(csrf())
	             .param("title", "") 
	             .param("comment", "内容はOK"))
	         .andExpect(status().isOk())
	         .andExpect(view().name("message/edit"))
	         .andExpect(model().attributeExists("errorMessage"))
	         .andExpect(model().attributeExists("threadForm"))
	         .andExpect(model().attribute("message", message));
	 }
	 
	 @Test
	 void showEditForm_スレッドの編集画面が正常に表示される() throws Exception {
	     int messageId = 100;

	     Message message = new Message();
	     message.setId(messageId);
	     message.setTitle("編集対象スレッド");
	     message.setComment("編集対象本文");
	     message.setParentId(null); 
	     message.setUser(loginUser); 

	     when(messageService.getMessageById(messageId)).thenReturn(message);

	     mockMvc.perform(get("/message/edit/{id}", messageId)
	             .with(user(userDetails)))
	         .andExpect(status().isOk())
	         .andExpect(view().name("message/edit"))
	         .andExpect(model().attributeExists("threadForm"))
	         .andExpect(model().attribute("message", message));
	 }
	 
// 削除
	 @Test
	 void softDeleteMessage_スレッドを投稿者が削除した場合はリダイレクトされる() throws Exception {
	     int messageId = 100;

	     Message message = new Message();
	     message.setId(messageId);
	     message.setTitle("削除対象スレッド");
	     message.setComment("削除対象本文");
	     message.setParentId(null);
	     message.setUser(loginUser);
	     BoardType boardType = new BoardType();
	     boardType.setId(1);
	     message.setBoardType(boardType);

	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);
	     when(messageService.getMessageById(messageId)).thenReturn(message);

	     mockMvc.perform(post("/thread/softdelete/{messageId}", messageId)
	             .with(user(userDetails))
	             .with(csrf()))
	         .andExpect(status().is3xxRedirection())
	         .andExpect(redirectedUrl("/message/threads/" + boardType.getId()))
	         .andExpect(flash().attribute("successMessage", "スレッドを削除しました"));

	     verify(messageService).deleteMessage(messageId);
	 }
	 
	 @Test
	 void softDeleteMessage_コメントを投稿者が削除した場合はリダイレクトされる() throws Exception {
	     int messageId = 200;
	     int parentId = 100;

	     Message reply = new Message();
	     reply.setId(messageId);
	     reply.setComment("削除対象のコメント");
	     reply.setParentId(parentId);
	     reply.setUser(loginUser);

	     BoardType boardType = new BoardType();
	     boardType.setId(1);
	     reply.setBoardType(boardType);

	     when(userService.findByUserId(loginUser.getUserId())).thenReturn(loginUser);
	     when(messageService.getMessageById(messageId)).thenReturn(reply);

	     mockMvc.perform(post("/thread/softdelete/{messageId}", messageId)
	             .with(user(userDetails))
	             .with(csrf()))
	         .andExpect(status().is3xxRedirection())
	         .andExpect(redirectedUrl("/message/thread/" + parentId))
	         .andExpect(flash().attribute("successMessage", "コメントを削除しました"));

	     verify(messageService).deleteMessage(messageId);
	 }
// 詳細画面
	 @Test
	 void showThreadDetail_スレッド詳細が正常に表示される() throws Exception {
	     int threadId = 100;

	     Message thread = new Message();
	     thread.setId(threadId);
	     thread.setTitle("テストスレッド");
	     thread.setComment("本文");
	     thread.setDeletedAt(null);
	     thread.setUser(loginUser);
	     
	     BoardType boardType = new BoardType();
	     boardType.setId(1);
	     boardType.setBoardName("テスト掲示板");
	     thread.setBoardType(boardType);

	     when(messageService.getMessageById(threadId)).thenReturn(thread);
	     when(messageService.getReplies(eq(threadId), any(Pageable.class)))
	         .thenReturn(Page.empty());

	     mockMvc.perform(get("/message/thread/{threadId}", threadId)
	             .with(user(userDetails)))
	         .andExpect(status().isOk())
	         .andExpect(view().name("message/thread"))
	         .andExpect(model().attribute("thread", thread))
	         .andExpect(model().attribute("selectedBoardType", boardType))
	         .andExpect(model().attributeExists("replies"))
	         .andExpect(model().attributeExists("replyForm"));
	 }
	 
	 @Test
	 void showThreadDetail_存在しないスレッドの場合はエラーページを表示する() throws Exception {
	     int threadId = 999;

	     when(messageService.getMessageById(threadId)).thenReturn(null);

	     mockMvc.perform(get("/message/thread/{threadId}", threadId)
	             .with(user(userDetails)))
	         .andExpect(status().isOk())
	         .andExpect(view().name("error/custom-error"))
	         .andExpect(model().attribute("errorMessage", "指定されたスレッドが見つかりません"));
	 }
	 
	 @Test
	 void showThreadDetail_論理削除されたスレッドの場合はエラーページを表示する() throws Exception {
	     int threadId = 100;

	     Message deletedThread = new Message();
	     deletedThread.setId(threadId);
	     deletedThread.setTitle("削除済みスレッド");
	     deletedThread.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
	     deletedThread.setUser(loginUser);

	     when(messageService.getMessageById(threadId)).thenReturn(deletedThread);

	     mockMvc.perform(get("/message/thread/{threadId}", threadId)
	             .with(user(userDetails)))
	         .andExpect(status().isOk())
	         .andExpect(view().name("error/custom-error"))
	         .andExpect(model().attribute("errorMessage", "指定されたスレッドが見つかりません"));
	 }











	 
}
