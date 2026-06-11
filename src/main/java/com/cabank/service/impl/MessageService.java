package com.cabank.service.impl;

import com.cabank.dto.response.MessageResponse;
import com.cabank.entity.Message;
import com.cabank.entity.User;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.MessageRepository;
import com.cabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public List<MessageResponse> getMessages(String email) {
        User user = getUser(email);
        return messageRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(String messageId, String email) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));
        message.setRead(true);
        messageRepository.save(message);
    }

    public long getUnreadCount(String email) {
        User user = getUser(email);
        return messageRepository.countByUserIdAndReadFalse(user.getId());
    }

    /**
     * Generic helper used by other services to push a notification/message
     * to a user whenever they perform an action (transfer, bill pay, card add, etc.)
     */
    @Transactional
    public void createMessage(User user, String sender, String content, String preview, Message.MessageType type) {
        Message message = Message.builder()
                .sender(sender)
                .content(content)
                .preview(preview)
                .read(false)
                .type(type)
                .user(user)
                .build();
        messageRepository.save(message);
    }

    private MessageResponse toResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .sender(m.getSender())
                .content(m.getContent())
                .preview(m.getPreview())
                .read(m.isRead())
                .type(m.getType().name())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}