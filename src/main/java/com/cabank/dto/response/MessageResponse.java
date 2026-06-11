package com.cabank.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private String id;
    private String sender;
    private String content;
    private String preview;
    private boolean read;
    private String type;
    private LocalDateTime createdAt;
}