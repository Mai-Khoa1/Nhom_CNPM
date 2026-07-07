package com.horseracing.service;

import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.Role;
import com.horseracing.dto.common.RoleMapper;
import com.horseracing.dto.user.UserCreateRequestDTO;
import com.horseracing.dto.user.UserMapper;
import com.horseracing.dto.user.UserResponseDTO;
import com.horseracing.dto.user.UserUpdateRequestDTO;
import com.horseracing.entity.BanToChuc;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.User;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.BanToChucRepository;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * UserService - quản lý tài khoản (dành cho Admin) trên bảng TaiKhoan.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final BanToChucRepository banToChucRepository;
    private final PasswordEncoder passwordEncoder;
    private final NhatKyHoatDongService nhatKyHoatDongService;

    public UserResponseDTO createUser(UserCreateRequestDTO dto, String staffId) {
        if (userRepository.existsByTenDangNhap(dto.getUsername())) {
            throw new DuplicateResourceException("Tên đăng nhập '" + dto.getUsername() + "' đã được sử dụng");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email '" + dto.getEmail() + "' đã được sử dụng");
        }

        User user = User.builder()
                .maTK(generateMaTK())
                .tenDangNhap(dto.getUsername())
                .matKhau(passwordEncoder.encode(dto.getPassword()))
                .hoTen(dto.getFullName())
                .email(dto.getEmail())
                .soDienThoai(dto.getPhone())
                .vaiTro(RoleMapper.toVaiTro(dto.getRole()))
                .build();

        User saved = userRepository.save(user);
        provisionChuNguaIfNeeded(saved);
        provisionBanToChucIfNeeded(saved);

        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_USER", "User:" + saved.getMaTK(),
                "Tạo tài khoản mới: " + saved.getTenDangNhap() + " (" + saved.getVaiTro() + ")");

        return UserMapper.toResponseDTO(saved);
    }

    public UserResponseDTO changeRole(String maTK, Role role, String staffId) {
        User user = userRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", "maTK", maTK));

        user.setVaiTro(RoleMapper.toVaiTro(role));
        User updated = userRepository.save(user);
        provisionChuNguaIfNeeded(updated);
        provisionBanToChucIfNeeded(updated);

        nhatKyHoatDongService.writeAuditLog(staffId, "CHANGE_ROLE", "User:" + maTK,
                "Đổi vai trò tài khoản " + updated.getTenDangNhap() + " -> " + role);

        return UserMapper.toResponseDTO(updated);
    }

    /** Tự động tạo hồ sơ ChuNgua khi tài khoản được gán vai trò Chủ ngựa (frontend không có API riêng cho ChuNgua). */
    private void provisionChuNguaIfNeeded(User user) {
        if (User.VAI_TRO_CHU_NGUA.equals(user.getVaiTro()) && chuNguaRepository.findByMaTK(user.getMaTK()).isEmpty()) {
            ChuNgua chuNgua = ChuNgua.builder()
                    .maChuNgua("CN" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase())
                    .maTK(user.getMaTK())
                    .hoTen(user.getHoTen())
                    .soDienThoai(user.getSoDienThoai())
                    .email(user.getEmail())
                    .build();
            chuNguaRepository.save(chuNgua);
        }
    }

    /** Tự động tạo hồ sơ BanToChuc khi tài khoản được gán vai trò Ban tổ chức (mỗi BTC là 1 đơn vị tổ chức đua độc lập). */
    private void provisionBanToChucIfNeeded(User user) {
        if (User.VAI_TRO_BAN_TO_CHUC.equals(user.getVaiTro()) && banToChucRepository.findByMaTK(user.getMaTK()).isEmpty()) {
            BanToChuc banToChuc = BanToChuc.builder()
                    .maBTC("BTC" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase())
                    .maTK(user.getMaTK())
                    .hoTen(user.getHoTen())
                    .boPhan(BanToChuc.BO_PHAN_DIEU_PHOI)
                    .build();
            banToChucRepository.save(banToChuc);
        }
    }

    public UserResponseDTO updateUser(String maTK, UserUpdateRequestDTO dto, String staffId) {
        User user = userRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", "maTK", maTK));

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email '" + dto.getEmail() + "' đã được sử dụng");
        }

        if (dto.getFullName() != null) user.setHoTen(dto.getFullName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setSoDienThoai(dto.getPhone());

        User updated = userRepository.save(user);
        nhatKyHoatDongService.writeAuditLog(staffId, "UPDATE_USER", "User:" + maTK,
                "Cập nhật thông tin tài khoản: " + updated.getTenDangNhap());

        return UserMapper.toResponseDTO(updated);
    }

    public UserResponseDTO setLocked(String maTK, boolean locked, String staffId) {
        User user = userRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", "maTK", maTK));

        user.setTrangThai(locked ? User.TRANG_THAI_BI_KHOA : User.TRANG_THAI_HOAT_DONG);
        User updated = userRepository.save(user);

        nhatKyHoatDongService.writeAuditLog(staffId, locked ? "LOCK_USER" : "UNLOCK_USER", "User:" + maTK,
                (locked ? "Khóa" : "Mở khóa") + " tài khoản: " + updated.getTenDangNhap());

        return UserMapper.toResponseDTO(updated);
    }

    public void deleteUser(String maTK, String staffId) {
        User user = userRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", "maTK", maTK));

        userRepository.delete(user);
        nhatKyHoatDongService.writeAuditLog(staffId, "DELETE_USER", "User:" + maTK,
                "Đã xóa tài khoản: " + user.getTenDangNhap());
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponseDTO> getAllUsers(Pageable pageable) {
        return PageResponse.of(userRepository.findAll(pageable), UserMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(String maTK) {
        User user = userRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", "maTK", maTK));
        return UserMapper.toResponseDTO(user);
    }

    private String generateMaTK() {
        return "TK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
