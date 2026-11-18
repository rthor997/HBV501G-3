package org.example.klifurapp.service.implementation;

import jakarta.transaction.Transactional;
import org.example.klifurapp.DTO.requests.CreateFriendRequestDTO;
import org.example.klifurapp.DTO.responses.FriendRequestResponseDTO;
import org.example.klifurapp.entity.FriendRequest;
import org.example.klifurapp.entity.FriendRequestStatus;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.exception.BadRequestException;
import org.example.klifurapp.exception.ConflictException;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.repository.FriendRequestRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.FriendRequestService;
import org.springframework.stereotype.Service;

@Service
public class FriendRequestServiceImplementation implements FriendRequestService {
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public FriendRequestServiceImplementation(
            FriendRequestRepository friendRequestRepository,
            UserRepository userRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public FriendRequestResponseDTO sendRequest(
            Long senderId,
            CreateFriendRequestDTO dto) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User receiver = userRepository.findById(dto.receiverId())
                .orElseThrow(()-> new ResourceNotFoundException("Receiver not found"));

        if (senderId.equals(receiver.getId())){
            throw new BadRequestException("You cannot send a friend request to yourself");
        }

        boolean alreadyPending = friendRequestRepository
                .existsBySenderIdAndReceiverIdAndStatus(
                        senderId,
                        receiver.getId(),
                        FriendRequestStatus.PENDING);
        if (alreadyPending) {
            throw new ConflictException("Friend request already exists");
        }

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);

        FriendRequest saved = friendRequestRepository.save(request);

        return toResponseDTO(saved);
    }
    // skal gera helper örugglega þarf að gera þetta nokkrum skipti
    private FriendRequestResponseDTO toResponseDTO(
            FriendRequest request) {
        return new FriendRequestResponseDTO(
                request.getId(),
                request.getSender().getId(),
                request.getSender().getUsername(),
                request.getReceiver().getId(),
                request.getReceiver().getUsername(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
