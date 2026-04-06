package com.dev.LMS.service;

import com.dev.LMS.dto.UpdateProfileDto;
import com.dev.LMS.model.AppRole;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.AppRoleRepository;
import com.dev.LMS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.dev.LMS.util.JwtUtil;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AppRoleRepository appRoleRepository;

    public void register(User user) {
        if (!userRepo.findByEmail(user.getEmail()).isEmpty()) {
            throw new RuntimeException("User with this email already exists.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        syncLegacyRoleMembership(user);

        userRepo.save(user);
    }

    public String login(User user) {
        return login(user.getEmail(), user.getPassword());
    }

    public String login(String email, String rawPassword) {
        User existingUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(rawPassword, existingUser.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        syncLegacyRoleMembership(existingUser);
        userRepo.save(existingUser);
        return jwtUtil.generateToken(existingUser);
    }

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public User updateUser(String email, UpdateProfileDto updateProfileDto) {
        User existingUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        existingUser.setName(updateProfileDto.getName());
        existingUser.setEmail(updateProfileDto.getEmail());
        existingUser.setPassword(passwordEncoder.encode(updateProfileDto.getPassword()));

        return userRepo.save(existingUser);
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(int id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User getUserByPublicId(UUID publicId) {
        return userRepo.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + publicId));
    }

    public User createUser(User user) {
        return userRepo.save(user);
    }

    public void deleteUser(int id) {
        userRepo.deleteById(id);
    }

    private void syncLegacyRoleMembership(User user) {
        if (user.getRole() == null) {
            return;
        }
        String roleName = user.getRole().canonical().name();
        AppRole appRole = appRoleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new RuntimeException("RBAC role not found for legacy role: " + roleName));
        boolean alreadyAssigned = user.getAccessRoles().stream()
                .anyMatch(existingRole -> existingRole.getName().equalsIgnoreCase(appRole.getName()));
        if (!alreadyAssigned) {
            user.addAccessRole(appRole);
        }
    }
}
