package org.example.klifurapp.DTO.responses;

public record PublicUserProfileResponseDTO(
        Long id,
        String username,
        long totalClimbs
        // profilePictureUrl not yet implemented
) {
}
