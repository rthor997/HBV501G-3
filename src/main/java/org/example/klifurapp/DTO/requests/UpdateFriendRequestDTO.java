package org.example.klifurapp.DTO.requests;

import jakarta.validation.constraints.NotNull;
import org.example.klifurapp.entity.FriendRequestStatus;

public record UpdateFriendRequestDTO(
        @NotNull
        FriendRequestStatus status
){}
