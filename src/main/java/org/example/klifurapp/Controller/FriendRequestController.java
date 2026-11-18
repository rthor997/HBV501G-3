package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.requests.CreateFriendRequestDTO;
import org.example.klifurapp.DTO.responses.FriendRequestResponseDTO;
import org.example.klifurapp.service.FriendRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/friend-requests")
public class FriendRequestController {
    private final FriendRequestService friendRequestService;

    public FriendRequestController(
            FriendRequestService friendRequestService) {
        this.friendRequestService = friendRequestService;
    }

    @PostMapping
    public ResponseEntity<FriendRequestResponseDTO> sendFriendRequest(
            @Valid @RequestBody CreateFriendRequestDTO dto,
            Authentication authentication) {
        Long senderId = (Long) authentication.getPrincipal();

        FriendRequestResponseDTO response =
                friendRequestService.sendRequest(
                        senderId,
                        dto
                );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
