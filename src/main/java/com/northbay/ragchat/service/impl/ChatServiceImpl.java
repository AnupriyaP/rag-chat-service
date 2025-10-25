package com.northbay.ragchat.service.impl;

import com.northbay.ragchat.entity.ChatMessage;
import com.northbay.ragchat.entity.ChatSession;
import com.northbay.ragchat.mapper.ChatMapper;
import com.northbay.ragchat.model.*;
import com.northbay.ragchat.repository.ChatMessageRepository;
import com.northbay.ragchat.repository.ChatSessionRepository;
import com.northbay.ragchat.service.ChatService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j; // ✅ NEW IMPORT
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // ✅ ADDED Lombok logger
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final ChatMapper mapper;

    public ChatServiceImpl(ChatSessionRepository sessionRepo, ChatMessageRepository messageRepo, ChatMapper mapper) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
        this.mapper = mapper;
        log.info("ChatServiceImpl initialized and ready."); // ✅ LOGGED
    }

    @Override
    public ChatSessionDTO createChatSession(ChatSessionCreateRequest request) {
        log.debug("Service: Creating session with title '{}' for owner '{}'", request.getTitle(), request.getOwner()); // ✅ LOGGED
        ChatSession session = ChatSession.builder()
                .title(request.getTitle())
                .owner(request.getOwner())
                .favorite(false)
                .build();
        sessionRepo.save(session);
        return mapper.toSessionDTO(session);
    }

    @Override
    public List<ChatSessionDTO> listChatSessions(String owner) {
        List<ChatSession> sessions = (owner != null)
                ? sessionRepo.findByOwner(owner)
                : sessionRepo.findAll();
        return mapper.toSessionDTOList(sessions);
    }

    @Override
    public ChatSessionDTO updateChatSession(Long id, ChatSessionUpdateRequest request) {
        ChatSession session = sessionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chat session not found"));
        if (request.getTitle() != null) session.setTitle(request.getTitle());
        if (request.getFavorite() != null) session.setFavorite(request.getFavorite());
        sessionRepo.save(session);
        return mapper.toSessionDTO(session);
    }

    @Override
    public void deleteChatSession(Long id) {
        log.warn("Service: Attempting to delete chat session with ID: {}", id); // ✅ LOGGED
        if (!sessionRepo.existsById(id)) {
            throw new EntityNotFoundException("Chat session not found");
        }
        sessionRepo.deleteById(id);
    }

    @Override
    public List<ChatSessionDTO> listFavoriteSessions() {
        return mapper.toSessionDTOList(sessionRepo.findByFavoriteTrue());
    }

    @Override
    public ChatMessageDTO addMessage(Long sessionId, ChatMessageCreateRequest request) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        ChatMessage msg = ChatMessage.builder()
                .session(session)
                .sender(request.getSender())
                .content(request.getContent())
                .context(request.getContext() != null && request.getContext().isPresent()
                        ? request.getContext().get()
                        : null)
                .build();

        messageRepo.save(msg);
        return mapper.toMessageDTO(msg);
    }

    @Override
    public ChatMessagePage getMessages(Long sessionId, Integer page, Integer size) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        Page<ChatMessage> messagePage = messageRepo.findBySession(session, PageRequest.of(page, size));

        ChatMessagePage apiPage = new ChatMessagePage();
        apiPage.setContent(
                messagePage.getContent().stream()
                        .map(mapper::toMessageDTO)
                        .collect(Collectors.toList())
        );
        apiPage.setPage(messagePage.getNumber());
        apiPage.setSize(messagePage.getSize());
        apiPage.setTotalElements((int) messagePage.getTotalElements());
        apiPage.setTotalPages(messagePage.getTotalPages());
        apiPage.setFirst(messagePage.isFirst());
        apiPage.setLast(messagePage.isLast());
        return apiPage;
    }
}
