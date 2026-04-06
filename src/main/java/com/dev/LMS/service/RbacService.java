package com.dev.LMS.service;

import com.dev.LMS.dto.AppRoleDto;
import com.dev.LMS.dto.AppRoleRequestDto;
import com.dev.LMS.dto.PrivilegeDto;
import com.dev.LMS.dto.PrivilegeRequestDto;
import com.dev.LMS.dto.RbacUserDto;
import com.dev.LMS.model.AppRole;
import com.dev.LMS.model.Privilege;
import com.dev.LMS.model.User;
import com.dev.LMS.repository.AppRoleRepository;
import com.dev.LMS.repository.PrivilegeRepository;
import com.dev.LMS.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class RbacService {
    private final AppRoleRepository appRoleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final UserRepository userRepository;

    public RbacService(AppRoleRepository appRoleRepository, PrivilegeRepository privilegeRepository, UserRepository userRepository) {
        this.appRoleRepository = appRoleRepository;
        this.privilegeRepository = privilegeRepository;
        this.userRepository = userRepository;
    }

    public PrivilegeDto createPrivilege(PrivilegeRequestDto requestDto) {
        validatePrivilegeNameUniqueness(requestDto.getName(), null);
        Privilege privilege = new Privilege(requestDto.getName().trim().toUpperCase(), requestDto.getDescription().trim());
        return new PrivilegeDto(privilegeRepository.save(privilege));
    }

    public List<PrivilegeDto> getPrivileges() {
        return privilegeRepository.findAll().stream().map(PrivilegeDto::new).toList();
    }

    public PrivilegeDto getPrivilege(UUID privilegeId) {
        return new PrivilegeDto(getPrivilegeEntity(privilegeId));
    }

    public PrivilegeDto updatePrivilege(UUID privilegeId, PrivilegeRequestDto requestDto) {
        Privilege privilege = getPrivilegeEntity(privilegeId);
        validatePrivilegeNameUniqueness(requestDto.getName(), privilegeId);
        privilege.setName(requestDto.getName().trim().toUpperCase());
        privilege.setDescription(requestDto.getDescription().trim());
        return new PrivilegeDto(privilegeRepository.save(privilege));
    }

    public void deletePrivilege(UUID privilegeId) {
        Privilege privilege = getPrivilegeEntity(privilegeId);
        for (AppRole appRole : new LinkedHashSet<>(privilege.getRoles())) {
            appRole.removePrivilege(privilege);
        }
        privilegeRepository.delete(privilege);
    }

    public AppRoleDto createRole(AppRoleRequestDto requestDto) {
        validateRoleNameUniqueness(requestDto.getName(), null);
        AppRole appRole = new AppRole(requestDto.getName().trim().toUpperCase(), requestDto.getDescription().trim());
        syncRolePrivileges(appRole, requestDto.getPrivilegeIds());
        return new AppRoleDto(appRoleRepository.save(appRole));
    }

    public List<AppRoleDto> getRoles() {
        return appRoleRepository.findAll().stream().map(AppRoleDto::new).toList();
    }

    public AppRoleDto getRole(UUID roleId) {
        return new AppRoleDto(getRoleEntity(roleId));
    }

    public AppRoleDto updateRole(UUID roleId, AppRoleRequestDto requestDto) {
        AppRole appRole = getRoleEntity(roleId);
        validateRoleNameUniqueness(requestDto.getName(), roleId);
        appRole.setName(requestDto.getName().trim().toUpperCase());
        appRole.setDescription(requestDto.getDescription().trim());
        syncRolePrivileges(appRole, requestDto.getPrivilegeIds());
        return new AppRoleDto(appRoleRepository.save(appRole));
    }

    public void deleteRole(UUID roleId) {
        AppRole appRole = getRoleEntity(roleId);
        for (User user : new LinkedHashSet<>(appRole.getUsers())) {
            user.removeAccessRole(appRole);
        }
        for (Privilege privilege : new LinkedHashSet<>(appRole.getPrivileges())) {
            appRole.removePrivilege(privilege);
        }
        appRoleRepository.delete(appRole);
    }

    public AppRoleDto assignPrivilegesToRole(UUID roleId, Set<UUID> privilegeIds) {
        AppRole appRole = getRoleEntity(roleId);
        for (UUID privilegeId : privilegeIds) {
            Privilege privilege = getPrivilegeEntity(privilegeId);
            appRole.addPrivilege(privilege);
        }
        return new AppRoleDto(appRoleRepository.save(appRole));
    }

    public AppRoleDto revokePrivilegesFromRole(UUID roleId, Set<UUID> privilegeIds) {
        AppRole appRole = getRoleEntity(roleId);
        for (UUID privilegeId : privilegeIds) {
            Privilege privilege = getPrivilegeEntity(privilegeId);
            appRole.removePrivilege(privilege);
        }
        return new AppRoleDto(appRoleRepository.save(appRole));
    }

    public List<RbacUserDto> getUsers() {
        return userRepository.findAll().stream().map(RbacUserDto::new).toList();
    }

    public RbacUserDto getUser(UUID userId) {
        return new RbacUserDto(getUserEntity(userId));
    }

    public RbacUserDto assignRolesToUser(UUID userId, Set<UUID> roleIds) {
        User user = getUserEntity(userId);
        for (UUID roleId : roleIds) {
            AppRole appRole = getRoleEntity(roleId);
            user.addAccessRole(appRole);
        }
        return new RbacUserDto(userRepository.save(user));
    }

    public RbacUserDto revokeRolesFromUser(UUID userId, Set<UUID> roleIds) {
        User user = getUserEntity(userId);
        for (UUID roleId : roleIds) {
            AppRole appRole = getRoleEntity(roleId);
            user.removeAccessRole(appRole);
        }
        return new RbacUserDto(userRepository.save(user));
    }

    public Set<String> getUserAuthorities(UUID userId) {
        return getUserEntity(userId).getAuthorityNames();
    }

    private void syncRolePrivileges(AppRole appRole, Set<UUID> privilegeIds) {
        Set<Privilege> desiredPrivileges = new LinkedHashSet<>();
        if (privilegeIds != null) {
            for (UUID privilegeId : privilegeIds) {
                desiredPrivileges.add(getPrivilegeEntity(privilegeId));
            }
        }
        for (Privilege privilege : new LinkedHashSet<>(appRole.getPrivileges())) {
            if (!desiredPrivileges.contains(privilege)) {
                appRole.removePrivilege(privilege);
            }
        }
        for (Privilege privilege : desiredPrivileges) {
            if (!appRole.getPrivileges().contains(privilege)) {
                appRole.addPrivilege(privilege);
            }
        }
    }

    private void validateRoleNameUniqueness(String roleName, UUID existingId) {
        appRoleRepository.findByNameIgnoreCase(roleName.trim())
                .filter(role -> existingId == null || !role.getId().equals(existingId))
                .ifPresent(role -> {
                    throw new RuntimeException("Role with this name already exists.");
                });
    }

    private void validatePrivilegeNameUniqueness(String privilegeName, UUID existingId) {
        privilegeRepository.findByNameIgnoreCase(privilegeName.trim())
                .filter(privilege -> existingId == null || !privilege.getId().equals(existingId))
                .ifPresent(privilege -> {
                    throw new RuntimeException("Privilege with this name already exists.");
                });
    }

    private AppRole getRoleEntity(UUID roleId) {
        return appRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
    }

    private Privilege getPrivilegeEntity(UUID privilegeId) {
        return privilegeRepository.findById(privilegeId)
                .orElseThrow(() -> new RuntimeException("Privilege not found: " + privilegeId));
    }

    private User getUserEntity(UUID userId) {
        return userRepository.findByPublicId(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}
