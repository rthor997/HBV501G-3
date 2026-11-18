package org.example.klifurapp.service;

import org.example.klifurapp.DTO.requests.AdminUpdateUserDTO;
import org.example.klifurapp.DTO.requests.UpdateUserRequestDTO;
import org.example.klifurapp.DTO.responses.PublicUserProfileResponseDTO;
import org.example.klifurapp.DTO.responses.UserResponseDTO;
import org.example.klifurapp.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponseDTO getUser(Long id);
    void deleteUser(Long id);
    User updateUser(Long id, UpdateUserRequestDTO dto);
    User updateUserAsAdmin(Long id, AdminUpdateUserDTO dto);
    PublicUserProfileResponseDTO getPublicUserProfile(Long userId);
    String updateProfilePicture(Long userId, MultipartFile file);
    void deleteProfilePicture(Long userId);
    String getProfilePictureFilename(Long userId);
}