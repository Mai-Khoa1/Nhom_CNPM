package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.Role;
import com.horseracing.dto.user.UserCreateRequestDTO;
import com.horseracing.dto.user.UserResponseDTO;
import com.horseracing.dto.user.UserUpdateRequestDTO;
import com.horseracing.service.AuthService;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * UserController - quản lý tài khoản (dành cho Admin) trên bảng TaiKhoan.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Quản lý người dùng", description = "API quản lý tài khoản (Admin)")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDTO>>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(pageable)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getMe(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser(authentication.getName())));
    }

    @GetMapping("/{maTK}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable String maTK) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(maTK)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @Valid @RequestBody UserCreateRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        UserResponseDTO created = userService.createUser(dto, staffId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Tạo tài khoản thành công"));
    }

    @PutMapping("/{maTK}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable String maTK, @Valid @RequestBody UserUpdateRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(maTK, dto, staffId)));
    }

    @PatchMapping("/{maTK}/lock")
    public ResponseEntity<ApiResponse<UserResponseDTO>> lockUser(@PathVariable String maTK, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(userService.setLocked(maTK, true, staffId), "Đã khóa tài khoản"));
    }

    @PatchMapping("/{maTK}/unlock")
    public ResponseEntity<ApiResponse<UserResponseDTO>> unlockUser(@PathVariable String maTK, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(userService.setLocked(maTK, false, staffId), "Đã mở khóa tài khoản"));
    }

    @PatchMapping("/{maTK}/role")
    public ResponseEntity<ApiResponse<UserResponseDTO>> changeRole(
            @PathVariable String maTK, @RequestBody Map<String, String> body, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        Role role = Role.valueOf(body.get("role"));
        return ResponseEntity.ok(ApiResponse.success(userService.changeRole(maTK, role, staffId), "Đã đổi vai trò"));
    }

    @DeleteMapping("/{maTK}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String maTK, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        userService.deleteUser(maTK, staffId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa tài khoản"));
    }
}
