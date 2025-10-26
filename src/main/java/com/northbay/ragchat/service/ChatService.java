package com.northbay.ragchat.service;

import com.northbay.ragchat.model.*;
import java.util.List;

/**
 * Service interface defining operations for managing chat sessions
 * and chat messages within the RAG Chat Service.
 * <p>
 * Provides methods for creating, updating, retrieving, and deleting
 * chat sessions, as well as adding and fetching messages.
 */
public interface ChatService {
    ChatSessionDTO createChatSession(ChatSessionCreateRequest request);
    List<ChatSessionDTO> listChatSessions(String owner);
    ChatSessionDTO updateChatSession(Long id, ChatSessionUpdateRequest request);
    void deleteChatSession(Long id);
    List<ChatSessionDTO> listFavoriteSessions();
    ChatMessageDTO addMessage(Long sessionId, ChatMessageCreateRequest request);
    ChatMessagePage getMessages(Long sessionId, Integer page, Integer size);
}
