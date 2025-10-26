package com.northbay.ragchat.controller;

import com.northbay.ragchat.api.ChatMessagesApi;
import com.northbay.ragchat.model.ChatMessageCreateRequest;
import com.northbay.ragchat.model.ChatMessageDTO;
import com.northbay.ragchat.model.ChatMessagePage;
import com.northbay.ragchat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

/**
 * Controller handling chat messages within a session.
 */
@RestController
public class ChatMessagesController implements ChatMessagesApi {

    private final ChatService chatService;

    public ChatMessagesController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Retrieves messages for a specific chat session with pagination support.
     *
     * @param id   the ID of the chat session
     * @param page the page number to retrieve
     * @param size the number of messages per page
     * @return a ResponseEntity containing the paginated chat messages
     */
    @Override
    public ResponseEntity<ChatMessagePage> getMessages(
            Integer id,
            @Valid Integer page,
            @Valid Integer size) {

        ChatMessagePage response = chatService.getMessages(id.longValue(), page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a new message to the specified chat session.
     *
     * @param id      the ID of the chat session
     * @param request the message creation request
     * @return a ResponseEntity containing the created chat message
     */
    @Override
    public ResponseEntity<ChatMessageDTO> addMessage(
            Integer id,
            @Valid ChatMessageCreateRequest request) {

        ChatMessageDTO saved = chatService.addMessage(id.longValue(), request);
        return ResponseEntity.status(201).body(saved);
    }
}
