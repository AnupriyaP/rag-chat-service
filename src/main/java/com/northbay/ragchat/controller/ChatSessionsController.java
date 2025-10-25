package com.northbay.ragchat.controller;

import com.northbay.ragchat.api.ChatSessionsApi;
import com.northbay.ragchat.model.ChatSessionCreateRequest;
import com.northbay.ragchat.model.ChatSessionDTO;
import com.northbay.ragchat.model.ChatSessionUpdateRequest;
import com.northbay.ragchat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j; // ✅ NEW IMPORT
@Slf4j
@RestController
public class ChatSessionsController implements ChatSessionsApi {

    private final ChatService chatService;

    public ChatSessionsController(ChatService chatService) {
        this.chatService = chatService;
        log.info("ChatSessionsController initialized."); // ✅ LOGGED
    }

    @Override
    public ResponseEntity<ChatSessionDTO> createChatSession(
            @Valid ChatSessionCreateRequest request) {
        log.info("Creating new chat session for owner: {}", request.getOwner()); // ✅ LOGGED
        return ResponseEntity.status(201).body(chatService.createChatSession(request));
    }

    @Override
    public ResponseEntity<List<ChatSessionDTO>> listChatSessions(String owner) {
        return ResponseEntity.ok(chatService.listChatSessions(owner));
    }

    @Override
    public ResponseEntity<ChatSessionDTO> updateChatSession(
            Integer id, @Valid ChatSessionUpdateRequest request) {
        return ResponseEntity.ok(chatService.updateChatSession(id.longValue(), request));
    }

    @Override
    public ResponseEntity<Void> deleteChatSession(Integer id) {
        chatService.deleteChatSession(id.longValue());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ChatSessionDTO>> listFavoriteSessions() {
        return ResponseEntity.ok(chatService.listFavoriteSessions());
    }
}
