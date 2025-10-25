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

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMapper {

    // Chat Session mappings
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
