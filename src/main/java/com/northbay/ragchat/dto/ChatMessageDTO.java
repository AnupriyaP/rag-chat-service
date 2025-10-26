package com.northbay.ragchat.dto;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Data Transfer Object (DTO) representing a chat message.
 * <p>
 * Used to transfer message data between the backend and clients
 * without exposing internal entity details.
 */
public class ChatMessageDTO {


    private Long id;
    private String sender;
    private String content;
    private String context;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")

    private OffsetDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")

    private OffsetDateTime updatedAt;


    // getters & setters
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getSender(){return sender;} public void setSender(String sender){this.sender=sender;}
    public String getContent(){return content;} public void setContent(String content){this.content=content;}
    public String getContextJson(){return context;} public void setContext(String context){this.context=context;}
    public OffsetDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(OffsetDateTime createdAt){this.createdAt=createdAt;}
    public OffsetDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(OffsetDateTime updatedAt){this.updatedAt=updatedAt;}

}
