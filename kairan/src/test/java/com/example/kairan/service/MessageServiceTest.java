package com.example.kairan.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.kairan.entity.BoardType;
import com.example.kairan.entity.Message;
import com.example.kairan.entity.Role;
import com.example.kairan.entity.User;
import com.example.kairan.repository.BoardTypeRepository;
import com.example.kairan.repository.MessageRepository;

@ActiveProfiles("test")
@SpringBootTest
public class MessageServiceTest {
	
	@InjectMocks
    private MessageService messageService;

    @Mock
    private BoardTypeRepository boardTypeRepository;

    @Mock
    private MessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // モックを初期化
        messageService = new MessageService(messageRepository, boardTypeRepository);
    }

    @Test
    void testHasPermissionToView_掲示板ごとの権限チェック() {
        // 各掲示板
        BoardType executiveBoard = new BoardType(); executiveBoard.setBoardName("行政会長掲示板");
        BoardType mayorBoard = new BoardType(); mayorBoard.setBoardName("会長委員長掲示板");
        BoardType commissionerBoard = new BoardType(); commissionerBoard.setBoardName("委員長委員掲示板");
        BoardType sectionChiefBoard = new BoardType(); sectionChiefBoard.setBoardName("会長区長掲示板");
        BoardType memberBoard = new BoardType(); memberBoard.setBoardName("区長会員掲示板");

        // 各役職
        Role executive = new Role(); executive.setName("ROLE_行政");
        Role mayor = new Role(); mayor.setName("ROLE_町内会長");
        Role commissioner = new Role(); commissioner.setName("ROLE_委員長");
        Role committee = new Role(); committee.setName("ROLE_委員");
        Role sectionChief = new Role(); sectionChief.setName("ROLE_区長");
        Role member = new Role(); member.setName("ROLE_会員");

        // 権限マトリクス（テストケース）
        Object[][] testCases = {
            {executiveBoard, executive, true}, {executiveBoard, mayor, true}, {executiveBoard, commissioner, false}, {executiveBoard, sectionChief, false}, {executiveBoard, committee, false}, {executiveBoard, member, false},
            {mayorBoard, executive, false}, {mayorBoard, mayor, true}, {mayorBoard, commissioner, true}, {mayorBoard, sectionChief, false}, {mayorBoard, committee, false}, {mayorBoard, member, false},
            {commissionerBoard, executive, false}, {commissionerBoard, mayor, false}, {commissionerBoard, commissioner, true}, {commissionerBoard, sectionChief, false}, {commissionerBoard, committee, true}, {commissionerBoard, member, false},
            {sectionChiefBoard, executive, false}, {sectionChiefBoard, mayor, true}, {sectionChiefBoard, commissioner, false}, {sectionChiefBoard, sectionChief, true}, {sectionChiefBoard, committee, false}, {sectionChiefBoard, member, false},
            {memberBoard, executive, false}, {memberBoard, mayor, false}, {memberBoard, commissioner, true}, {memberBoard, sectionChief, true}, {memberBoard, committee, true}, {memberBoard, member, true}, 
        };

        for (Object[] testCase : testCases) {
            BoardType boardType = (BoardType) testCase[0];
            Role role = (Role) testCase[1];
            boolean expected = (boolean) testCase[2];

            User user = new User();
            user.setRole(role);

            // テスト実行
            boolean result = messageService.hasPermissionToView(boardType, user.getRole().getName());

            // 結果の検証
            assertEquals(expected, result,
                String.format("掲示板 %s | 役職 %s | 期待値: %b", boardType.getBoardName(), role.getName(), expected));
        }
    }

    @Test
    void hasPermissionToView_許可されたユーザならTrue() {
        BoardType boardType = new BoardType();
        boardType.setBoardName("会長委員長掲示板");

        User user = new User();
        Role role = new Role();
        role.setName("ROLE_町内会長");
        user.setRole(role);

        boolean result = messageService.hasPermissionToView(boardType, user.getRole().getName());

        assertTrue(result, "町内課長は会長委員長掲示板を閲覧できる");
    }

    @Test
    void hasPermissionToView_許可されていないユーザならFalse() {
        BoardType boardType = new BoardType();
        boardType.setBoardName("会長委員長掲示板");

        User user = new User();
        Role role = new Role();
        role.setName("ROLE_会員");
        user.setRole(role);

        boolean result = messageService.hasPermissionToView(boardType, user.getRole().getName());

        assertFalse(result, "一般会員は会長委員長掲示板を閲覧できない");
    }
    
    @Test
    void createThread_正常にスレッドが作成される() {
        // Arrange
        User user = new User();
        user.setId(1);
        
        BoardType boardType = new BoardType();
        boardType.setId(1);
        boardType.setBoardName("会長委員長掲示板");
        
        String title = "テストタイトル";
        String comment = "テストコメント";

        Message expectedMessage = new Message();
        expectedMessage.setUser(user);
        expectedMessage.setBoardType(boardType);
        expectedMessage.setTitle(title);
        expectedMessage.setComment(comment);

        // spy前に元の messageService を手動で構築（モックを渡す）
        MessageService realService = new MessageService(messageRepository, boardTypeRepository);
        MessageService spyService = spy(realService);

        // モック定義
        when(boardTypeRepository.findById(1)).thenReturn(Optional.of(boardType));
        when(messageRepository.save(any(Message.class))).thenReturn(expectedMessage);
        doReturn(true).when(spyService).canCreateThread(user, boardType);

        // Act
        Message result = spyService.createThread(user, 1, title, comment);

        // Assert
        assertEquals(title, result.getTitle());
        assertEquals(comment, result.getComment());
        assertEquals(user, result.getUser());
        assertEquals(boardType, result.getBoardType());

        verify(messageRepository, times(1)).save(any(Message.class));
    }
    
    @Test
    void createReply_正常にレスが作成される() {
        User user = new User();
        user.setId(1);

        BoardType boardType = new BoardType();
        boardType.setId(1);
        boardType.setBoardName("委員長委員掲示板");

        int threadId = 100;
        Message parentThread = new Message();
        parentThread.setId(threadId);
        parentThread.setUser(user);
        parentThread.setBoardType(boardType);
        parentThread.setDeletedAt(null);

        String replyComment = "返信コメント";

        Message expectedReply = new Message();
        expectedReply.setId(200);
        expectedReply.setUser(user);
        expectedReply.setBoardType(boardType);
        expectedReply.setParentId(threadId);
        expectedReply.setComment(replyComment);

        when(messageRepository.findById(threadId))
            .thenReturn(Optional.of(parentThread));
        when(messageRepository.save(any(Message.class)))
            .thenReturn(expectedReply);

        Message result = messageService.createReply(user, threadId, replyComment);

        assertEquals(replyComment, result.getComment());
        assertEquals(user, result.getUser());
        assertEquals(boardType, result.getBoardType());
        assertEquals(threadId, result.getParentId());

        verify(messageRepository).findById(threadId);
        verify(messageRepository).save(any(Message.class));
    }
    
    @Test
    void canCreateThread_許可された役職ならTrueを返す() {
        User user = new User();
        Role role = new Role();
        role.setName("ROLE_町内会長"); 
        user.setRole(role);

        BoardType boardType = new BoardType();
        boardType.setBoardName("会長委員長掲示板");

        boolean result = messageService.canCreateThread(user, boardType);

        assertTrue(result, "ROLE_町内会長 はスレッド作成可能であるべき");
    }
    
    @Test
    void canCreateThread_許可されていない役職ならFalseを返す() {
        User user = new User();
        Role role = new Role();
        role.setName("ROLE_会員"); // 許可されていない役職
        user.setRole(role);

        BoardType boardType = new BoardType();
        boardType.setBoardName("区長会員掲示板");

        boolean result = messageService.canCreateThread(user, boardType);

        assertFalse(result, "ROLE_会員 はスレッド作成できないはず");
    }
    
    @Test
    void deleteMessage_正常にソフトデリートできる() {
        // arrange
        int messageId = 1;

        Message message = new Message();
        message.setId(messageId);
        message.setDeletedAt(null);
        message.setUpdatedAt(null);

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        // act
        messageService.deleteMessage(messageId);

        // assert
        assertNotNull(message.getDeletedAt(), "削除日時がセットされていること");
        assertNotNull(message.getUpdatedAt(), "更新日時がセットされていること");
        verify(messageRepository).save(message);
    }
    
    @Test
    void deleteMessage_存在しないIDの場合は例外がスローされる() {
        // arrange
        int messageId = 999;
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        // act & assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            messageService.deleteMessage(messageId);
        });

        assertEquals("指定したIDが存在しないか削除されています", exception.getMessage());
    }
    
    @Test
    void updateMessageThread_正常に更新される() {
        // arrange
        User user = new User();
        user.setId(1);

        BoardType boardType = new BoardType();
        boardType.setId(1);

        Message originalMessage = new Message();
        originalMessage.setId(100);
        originalMessage.setUser(user);
        originalMessage.setBoardType(boardType);
        originalMessage.setTitle("旧タイトル");
        originalMessage.setComment("旧コメント");

        String newTitle = "新しいタイトル";
        String newComment = "新しいコメント";

        Message updatedMessage = new Message();
        updatedMessage.setId(100);
        updatedMessage.setUser(user);
        updatedMessage.setBoardType(boardType);
        updatedMessage.setTitle(newTitle);
        updatedMessage.setComment(newComment);

        when(messageRepository.save(any(Message.class))).thenReturn(updatedMessage);

        // act
        Message result = messageService.updateMessageThread(originalMessage, newTitle, newComment);

        // assert
        assertEquals(newTitle, result.getTitle());
        assertEquals(newComment, result.getComment());
        verify(messageRepository, times(1)).save(originalMessage);
    }
    
    @Test
    void updateMessageReply_正常に更新される() {
        // arrange
        Message reply = new Message();
        reply.setId(100);
        reply.setComment("旧コメント");

        String updatedComment = "新しいコメント";

        Message updatedReply = new Message();
        updatedReply.setId(100);
        updatedReply.setComment(updatedComment);

        when(messageRepository.save(reply)).thenReturn(updatedReply);

        // act
        Message result = messageService.updateMessageReply(reply, updatedComment);

        // assert
        assertEquals(updatedComment, result.getComment());
        verify(messageRepository, times(1)).save(reply);
    }









}
