package org.example.klifurapp.DTO.responses;

import org.example.klifurapp.entity.FriendRequestStatus;

import java.time.LocalDateTime;


public record FriendRequestResponseDTO(
        Long id,
        Long senderId,
        String senderUsername,
        Long receiverId,
        String receiverUsername,
        FriendRequestStatus status,
        LocalDateTime createdAt
) {}
