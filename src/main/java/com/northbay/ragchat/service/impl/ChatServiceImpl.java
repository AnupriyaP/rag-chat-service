package com.northbay.ragchat.service.impl;

import com.northbay.ragchat.entity.ChatMessage;
import com.northbay.ragchat.entity.ChatSession;
import com.northbay.ragchat.mapper.ChatMapper;
import com.northbay.ragchat.model.*;
import com.northbay.ragchat.repository.ChatMessageRepository;
import com.northbay.ragchat.repository.ChatSessionRepository;
import com.northbay.ragchat.service.ChatService;
import com.northbay.ragchat.service.GroqLLMService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j; // ✅ NEW IMPORT
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link ChatService} interface.
 * <p>
 * Handles all business logic for chat sessions and chat messages,
 * including creation, retrieval, updates, and deletion.
 */
@Slf4j // ✅ ADDED Lombok logger
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final ChatMapper mapper;
    private final GroqLLMService groqLLMService;

    public ChatServiceImpl(ChatSessionRepository sessionRepo, ChatMessageRepository messageRepo, ChatMapper mapper, GroqLLMService groqLLMService) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
        this.mapper = mapper;
        this.groqLLMService = groqLLMService;
        log.info("ChatServiceImpl initialized and ready."); // ✅ LOGGED
    }

    /**
     * Creates a new chat session.
     *
     * @param request the session creation request
     * @return the created chat session as a DTO
     */
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

    /**
     * Returns all chat sessions for a given owner, or all sessions if no owner is provided.
     *
     * @param owner the owner of the sessions (optional)
     * @return list of chat sessions
     */
    @Override
    public List<ChatSessionDTO> listChatSessions(String owner) {
        List<ChatSession> sessions = (owner != null)
                ? sessionRepo.findByOwner(owner)
                : sessionRepo.findAll();
        return mapper.toSessionDTOList(sessions);
    }

    /**
     * Updates a chat session (title or favorite flag).
     *
     * @param id      the session ID
     * @param request the update request
     * @return the updated chat session as a DTO
     * @throws EntityNotFoundException if the session does not exist
     */
    @Override
    public ChatSessionDTO updateChatSession(Long id, ChatSessionUpdateRequest request) {
        ChatSession session = sessionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chat session not found"));
        if (request.getTitle() != null) session.setTitle(request.getTitle());
        if (request.getFavorite() != null) session.setFavorite(request.getFavorite());
        sessionRepo.save(session);
        return mapper.toSessionDTO(session);
    }

    /**
     * Deletes a chat session by ID.
     *
     * @param id the session ID
     * @throws EntityNotFoundException if the session does not exist
     */
    @Override
    public void deleteChatSession(Long id) {
        log.warn("Service: Attempting to delete chat session with ID: {}", id); // ✅ LOGGED
        if (!sessionRepo.existsById(id)) {
            throw new EntityNotFoundException("Chat session not found");
        }
        sessionRepo.deleteById(id);
    }

    /**
     * Retrieves all sessions marked as favorite.
     *
     * @return list of favorite sessions
     */
    @Override
    public List<ChatSessionDTO> listFavoriteSessions() {
        return mapper.toSessionDTOList(sessionRepo.findByFavoriteTrue());
    }

    /**
     * Adds a new message to a specific chat session.
     *
     * @param sessionId the ID of the chat session
     * @param request   the message creation request
     * @return the created message as a DTO
     * @throws EntityNotFoundException if the session does not exist
     */
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
        if ("user".equalsIgnoreCase(request.getSender())) {
            try {
                log.debug("Calling Groq LLM for session {} message id {}", sessionId, msg.getId());
                String assistantText = groqLLMService.generateCompletion(request.getContent());

                ChatMessage assistant = ChatMessage.builder()
                        .session(session)
                        .sender("assistant")
                        .content(assistantText)
                        .context("{\"source\":\"groq\",\"model\":\"" + System.getenv().getOrDefault("GROQ_MODEL","llama3-70b-8192") + "\"}")
                        .build();

                messageRepo.save(assistant);

                return mapper.toMessageDTO(assistant);

            } catch (Exception e) {
                log.error("Error calling Groq LLM: {}", e.getMessage(), e);
                // Fallback: return the stored user message DTO so the client still gets the persisted message
                return mapper.toMessageDTO(msg);
            }
        }

        // Non-user senders: return the stored message DTO
        return mapper.toMessageDTO(msg);
    }

    /**
     * Retrieves paginated chat messages for a given session.
     *
     * @param sessionId the ID of the chat session
     * @param page      the page number
     * @param size      the number of messages per page
     * @return a paginated list of messages wrapped in {@link ChatMessagePage}
     * @throws EntityNotFoundException if the session does not exist
     */
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
