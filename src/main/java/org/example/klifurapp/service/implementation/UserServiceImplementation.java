package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.user.UserDTO;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;

    public UserServiceImplementation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found, ID mismatch")
                );

        // Hér má síðar bæta við upplýsingum um user sem mætti sækja eins og profile pic
        UserDTO userDto = new UserDTO();
        userDto.setEmail(user.getEmail());
        userDto.setUsername(user.getUsername());

        return userDto;
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found, ID mismatch");
        }

        userRepository.deleteById(id);
    }
}