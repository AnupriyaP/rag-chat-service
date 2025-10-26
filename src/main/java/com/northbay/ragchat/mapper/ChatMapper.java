package com.northbay.ragchat.mapper;

import com.northbay.ragchat.entity.ChatMessage;
import com.northbay.ragchat.entity.ChatSession;
import com.northbay.ragchat.model.ChatMessageDTO;
import com.northbay.ragchat.model.ChatSessionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Mapper interface for converting between entity and DTO objects used
 * in the chat service.
 * <p>
 * This interface is implemented automatically by MapStruct at build time.
 * It provides mappings for chat sessions and chat messages, handling
 * type conversions such as {@link Instant} to {@link OffsetDateTime}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMapper {

    /**
     * Converts a {@link ChatSession} entity to a {@link ChatSessionDTO}.
     *
     * @param entity the ChatSession entity
     * @return the mapped ChatSessionDTO
     */
    // Chat Session mappings
    @Mapping(source = "createdAt", target = "createdAt")
    ChatSessionDTO toSessionDTO(ChatSession entity);

    List<ChatSessionDTO> toSessionDTOList(List<ChatSession> entities);

    // Chat Message mapping
    @Mapping(target = "context",
            expression = "java(entity.getContext() != null ? org.openapitools.jackson.nullable.JsonNullable.of(entity.getContext()) : org.openapitools.jackson.nullable.JsonNullable.undefined())")

    @Mapping(source = "createdAt", target = "createdAt")
    ChatMessageDTO toMessageDTO(ChatMessage entity);

    // Instant -> OffsetDateTime helper for MapStruct
    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    // Optional: map list of messages
    List<ChatMessageDTO> toMessageDTOList(List<ChatMessage> entities);
}
