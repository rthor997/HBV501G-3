package org.example.klifurapp.service;

import org.example.klifurapp.DTO.requests.CreateFriendRequestDTO;
import org.example.klifurapp.DTO.responses.FriendRequestResponseDTO;

public interface FriendRequestService {
    FriendRequestResponseDTO sendRequest(
            Long senderId,
            CreateFriendRequestDTO dto
    );
}
