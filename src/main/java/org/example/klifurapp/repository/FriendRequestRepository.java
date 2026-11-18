package org.example.klifurapp.repository;

import org.example.klifurapp.entity.FriendRequest;
import org.example.klifurapp.entity.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    boolean existsBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, FriendRequestStatus status);
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId,FriendRequestStatus status);
    List<FriendRequest> findBySenderIdAndStatus (Long senderId, FriendRequestStatus status);
    Optional<FriendRequest> findByIdAndStatus(Long id, FriendRequestStatus status);

}
