package com.dev.LMS.controller;

import com.dev.LMS.dto.AppRoleDto;
import com.dev.LMS.dto.AppRoleRequestDto;
import com.dev.LMS.dto.PrivilegeIdsRequestDto;
import com.dev.LMS.dto.PrivilegeDto;
import com.dev.LMS.dto.PrivilegeRequestDto;
import com.dev.LMS.dto.RoleIdsRequestDto;
import com.dev.LMS.dto.RbacUserDto;
import com.dev.LMS.service.RbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/rbac")
@PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
@Tag(name = "RBAC", description = "System administrator endpoints for managing privileges, roles, and role assignments for users.")
public class RbacController {
    private final RbacService rbacService;

    public RbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @PostMapping("/privileges")
    @Operation(summary = "Create a privilege", description = "Creates a new privilege that can later be attached to one or more roles.")
    public ResponseEntity<PrivilegeDto> createPrivilege(@Valid @RequestBody PrivilegeRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rbacService.createPrivilege(requestDto));
    }

    @GetMapping("/privileges")
    @Operation(summary = "List privileges", description = "Returns all privileges currently defined in the RBAC system.")
    public ResponseEntity<List<PrivilegeDto>> getPrivileges() {
        return ResponseEntity.ok(rbacService.getPrivileges());
    }

    @GetMapping("/privileges/{privilegeId}")
    @Operation(summary = "Get a privilege", description = "Returns one privilege by UUID.")
    public ResponseEntity<PrivilegeDto> getPrivilege(@PathVariable UUID privilegeId) {
        return ResponseEntity.ok(rbacService.getPrivilege(privilegeId));
    }

    @PutMapping("/privileges/{privilegeId}")
    @Operation(summary = "Update a privilege", description = "Updates the name and description of an existing privilege.")
    public ResponseEntity<PrivilegeDto> updatePrivilege(@PathVariable UUID privilegeId,
                                                        @Valid @RequestBody PrivilegeRequestDto requestDto) {
        return ResponseEntity.ok(rbacService.updatePrivilege(privilegeId, requestDto));
    }

    @DeleteMapping("/privileges/{privilegeId}")
    @Operation(summary = "Delete a privilege", description = "Deletes a privilege and removes it from any roles that currently reference it.")
    public ResponseEntity<Void> deletePrivilege(@PathVariable UUID privilegeId) {
        rbacService.deletePrivilege(privilegeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/roles")
    @Operation(summary = "Create a role", description = "Creates a new role and optionally attaches an initial list of privileges.")
    public ResponseEntity<AppRoleDto> createRole(@Valid @RequestBody AppRoleRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rbacService.createRole(requestDto));
    }

    @GetMapping("/roles")
    @PreAuthorize("permitAll()")
    @Operation(summary = "List roles", description = "Returns all RBAC roles with their assigned privileges.")
    public ResponseEntity<List<AppRoleDto>> getRoles() {
        return ResponseEntity.ok(rbacService.getRoles());
    }

    @GetMapping("/roles/{roleId}")
    @Operation(summary = "Get a role", description = "Returns a single role by UUID including its attached privileges.")
    public ResponseEntity<AppRoleDto> getRole(@PathVariable UUID roleId) {
        return ResponseEntity.ok(rbacService.getRole(roleId));
    }

    @PutMapping("/roles/{roleId}")
    @Operation(summary = "Update a role", description = "Updates the role name, description, and full privilege set.")
    public ResponseEntity<AppRoleDto> updateRole(@PathVariable UUID roleId,
                                                 @Valid @RequestBody AppRoleRequestDto requestDto) {
        return ResponseEntity.ok(rbacService.updateRole(roleId, requestDto));
    }

    @DeleteMapping("/roles/{roleId}")
    @Operation(summary = "Delete a role", description = "Deletes a role and removes it from every user currently assigned to it.")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID roleId) {
        rbacService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/roles/{roleId}/privileges")
    @Operation(summary = "Assign privileges to a role", description = "Adds one or more privileges to the specified role using a list of privilege UUIDs.")
    public ResponseEntity<AppRoleDto> assignPrivilegesToRole(@PathVariable UUID roleId,
                                                             @Valid @RequestBody PrivilegeIdsRequestDto requestDto) {
        return ResponseEntity.ok(rbacService.assignPrivilegesToRole(roleId, requestDto.getPrivilegeIds()));
    }

    @DeleteMapping("/roles/{roleId}/privileges")
    @Operation(summary = "Remove privileges from a role", description = "Removes one or more privileges from the specified role using a list of privilege UUIDs.")
    public ResponseEntity<AppRoleDto> revokePrivilegesFromRole(@PathVariable UUID roleId,
                                                               @Valid @RequestBody PrivilegeIdsRequestDto requestDto) {
        return ResponseEntity.ok(rbacService.revokePrivilegesFromRole(roleId, requestDto.getPrivilegeIds()));
    }

    @GetMapping("/users")
    @Operation(summary = "List users for RBAC management", description = "Returns users with their UUIDs, legacy roles, assigned RBAC roles, and resolved authorities.")
    public ResponseEntity<List<RbacUserDto>> getUsers() {
        return ResponseEntity.ok(rbacService.getUsers());
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get one user for RBAC management", description = "Returns one user by UUID with assigned roles and resolved authorities.")
    public ResponseEntity<RbacUserDto> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(rbacService.getUser(userId));
    }

    @PostMapping("/users/{userId}/roles")
    @Operation(summary = "Assign roles to a user", description = "Assigns one or more RBAC roles to a user using a list of role UUIDs.")
    public ResponseEntity<RbacUserDto> assignRolesToUser(@PathVariable UUID userId,
                                                         @Valid @RequestBody RoleIdsRequestDto requestDto) {
        return ResponseEntity.ok(rbacService.assignRolesToUser(userId, requestDto.getRoleIds()));
    }

    @DeleteMapping("/users/{userId}/roles")
    @Operation(summary = "Remove roles from a user", description = "Removes one or more RBAC roles from a user using a list of role UUIDs.")
    public ResponseEntity<RbacUserDto> revokeRolesFromUser(@PathVariable UUID userId,
                                                           @Valid @RequestBody RoleIdsRequestDto requestDto) {
        return ResponseEntity.ok(rbacService.revokeRolesFromUser(userId, requestDto.getRoleIds()));
    }

    @GetMapping("/users/{userId}/authorities")
    @Operation(summary = "Get user authorities", description = "Returns the final resolved authority names for a user after combining all assigned roles and privileges.")
    public ResponseEntity<Map<String, Set<String>>> getUserAuthorities(@PathVariable UUID userId) {
        return ResponseEntity.ok(Map.of("authorities", rbacService.getUserAuthorities(userId)));
    }
}
