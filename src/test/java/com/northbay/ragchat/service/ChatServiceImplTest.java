package com.northbay.ragchat.service;

import com.northbay.ragchat.entity.ChatMessage;
import com.northbay.ragchat.entity.ChatSession;
import com.northbay.ragchat.mapper.ChatMapper;
import com.northbay.ragchat.model.*;
import com.northbay.ragchat.repository.ChatMessageRepository;
import com.northbay.ragchat.repository.ChatSessionRepository;
import com.northbay.ragchat.service.impl.ChatServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatSessionRepository sessionRepo;

    @Mock
    private ChatMessageRepository messageRepo;

    @Mock
    private ChatMapper mapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    private ChatSession session;
    private ChatMessage message;
    private ChatSessionDTO sessionDTO;
    private ChatMessageDTO messageDTO;

    @BeforeEach
    void setup() {
        session = ChatSession.builder()
                .id(1L)
                .title("Test Session")
                .owner("user1")
                .favorite(false)
                .build();

        message = ChatMessage.builder()
                .id(10L)
                .session(session)
                .sender("user1")
                .content("Hello")
                .build();

        sessionDTO = new ChatSessionDTO();
        sessionDTO.setId(1);
        sessionDTO.setTitle("Test Session");

        messageDTO = new ChatMessageDTO();
        messageDTO.setId(10);
        messageDTO.setContent("Hello");
    }

    // ✅ createChatSession
    @Test
    void shouldCreateChatSession() {
        ChatSessionCreateRequest req = new ChatSessionCreateRequest();
        req.setTitle("My Chat");
        req.setOwner("user1");

        when(sessionRepo.save(any(ChatSession.class))).thenReturn(session);
        when(mapper.toSessionDTO(any(ChatSession.class))).thenReturn(sessionDTO);

        ChatSessionDTO result = chatService.createChatSession(req);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Session");
        verify(sessionRepo).save(any(ChatSession.class));
    }

    // ✅ listChatSessions
    @Test
    void shouldListChatSessions() {
        when(sessionRepo.findByOwner("user1")).thenReturn(List.of(session));
        when(mapper.toSessionDTOList(anyList())).thenReturn(List.of(sessionDTO));

        List<ChatSessionDTO> result = chatService.listChatSessions("user1");

        assertThat(result).hasSize(1);
        verify(sessionRepo).findByOwner("user1");
    }

    // ✅ updateChatSession
    @Test
    void shouldUpdateChatSessionTitle() {
        ChatSessionUpdateRequest req = new ChatSessionUpdateRequest();
        req.setTitle("Updated Title");

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepo.save(any(ChatSession.class))).thenReturn(session);
        when(mapper.toSessionDTO(any(ChatSession.class))).thenReturn(sessionDTO);

        ChatSessionDTO result = chatService.updateChatSession(1L, req);

        assertThat(result).isNotNull();
        verify(sessionRepo).save(session);
    }

    // ❌ updateChatSession - not found
    @Test
    void shouldThrowWhenUpdatingNonexistentSession() {
        when(sessionRepo.findById(99L)).thenReturn(Optional.empty());
        ChatSessionUpdateRequest req = new ChatSessionUpdateRequest();
        req.setTitle("New");

        assertThatThrownBy(() -> chatService.updateChatSession(99L, req))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Chat session not found");
    }

    // ✅ deleteChatSession
    @Test
    void shouldDeleteExistingChatSession() {
        when(sessionRepo.existsById(1L)).thenReturn(true);

        chatService.deleteChatSession(1L);

        verify(sessionRepo).deleteById(1L);
    }

    // ❌ deleteChatSession - not found
    @Test
    void shouldThrowWhenDeletingNonexistentSession() {
        when(sessionRepo.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> chatService.deleteChatSession(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Chat session not found");
    }

    // ✅ listFavoriteSessions
    @Test
    void shouldListFavoriteSessions() {
        when(sessionRepo.findByFavoriteTrue()).thenReturn(List.of(session));
        when(mapper.toSessionDTOList(anyList())).thenReturn(List.of(sessionDTO));

        List<ChatSessionDTO> result = chatService.listFavoriteSessions();

        assertThat(result).hasSize(1);
        verify(sessionRepo).findByFavoriteTrue();
    }

    // ✅ addMessage
    @Test
    void shouldAddMessageToSession() {
        ChatMessageCreateRequest req = new ChatMessageCreateRequest();
        req.setSender("user1");
        req.setContent("Hello");

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));
        when(messageRepo.save(any(ChatMessage.class))).thenReturn(message);
        when(mapper.toMessageDTO(any(ChatMessage.class))).thenReturn(messageDTO);

        ChatMessageDTO result = chatService.addMessage(1L, req);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hello");
        verify(messageRepo).save(any(ChatMessage.class));
    }

    // ❌ addMessage - session not found
    @Test
    void shouldThrowWhenAddingMessageToNonexistentSession() {
        ChatMessageCreateRequest req = new ChatMessageCreateRequest();
        req.setSender("user1");
        req.setContent("Test");
        when(sessionRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.addMessage(999L, req))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Session not found");
    }

    // ✅ getMessages
    @Test
    void shouldReturnMessagesForSession() {
        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));
        Page<ChatMessage> page = new PageImpl<>(List.of(message));
        when(messageRepo.findBySession(eq(session), any(PageRequest.class))).thenReturn(page);
        when(mapper.toMessageDTO(any(ChatMessage.class))).thenReturn(messageDTO);

        ChatMessagePage result = chatService.getMessages(1L, 0, 5);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    // ❌ getMessages - session not found
    @Test
    void shouldThrowWhenGettingMessagesForNonexistentSession() {
        when(sessionRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getMessages(999L, 0, 5))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Session not found");
    }
}
