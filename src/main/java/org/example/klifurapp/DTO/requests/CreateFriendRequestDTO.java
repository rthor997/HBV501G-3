package org.example.klifurapp.DTO.requests;

import jakarta.validation.constraints.NotNull;

public record CreateFriendRequestDTO(
        @NotNull
        Long receiverId
){}
