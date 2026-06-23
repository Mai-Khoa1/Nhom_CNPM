package com.horseracing.service;

import com.horseracing.dto.auth.LoginRequest;
import com.horseracing.dto.auth.LoginResponse;
import com.horseracing.dto.auth.RegisterRequest;
import com.horseracing.dto.auth.TokenRefreshResponse;
import com.horseracing.dto.user.UserMapper;
import com.horseracing.dto.user.UserResponseDTO;
import com.horseracing.entity.RefreshToken;
import com.horseracing.entity.User;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.RefreshTokenRepository;
import com.horseracing.repository.UserRepository;
import com.horseracing.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuthService - đăng ký, đăng nhập, làm mới access token, đăng xuất.
 * Access token là JWT (stateless); refresh token là chuỗi ngẫu nhiên lưu DB (revocable).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final NhatKyHoatDongService nhatKyHoatDongService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    public UserResponseDTO register(RegisterRequest request) {
        if (userRepository.existsByTenDangNhap(request.getUsername())) {
            throw new DuplicateResourceException("Tên đăng nhập '" + request.getUsername() + "' đã được sử dụng");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' đã được sử dụng");
        }

        User user = User.builder()
                .maTK(generateMaTK())
                .tenDangNhap(request.getUsername())
                .matKhau(passwordEncoder.encode(request.getPassword()))
                .hoTen(request.getFullName())
                .email(request.getEmail())
                .soDienThoai(request.getPhone())
                .vaiTro(User.VAI_TRO_NGUOI_XEM)
                .build();

        User saved = userRepository.save(user);
        nhatKyHoatDongService.writeAuditLog(saved.getMaTK(), "REGISTER", "User:" + saved.getMaTK(),
                "Đăng ký tài khoản mới: " + saved.getTenDangNhap());

        return UserMapper.toResponseDTO(saved);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByTenDangNhap(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + request.getUsername()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getTenDangNhap());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = createRefreshToken(user.getMaTK());

        nhatKyHoatDongService.writeAuditLog(user.getMaTK(), "LOGIN", "User:" + user.getMaTK(),
                "Đăng nhập thành công: " + user.getTenDangNhap());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .user(UserMapper.toResponseDTO(user))
                .build();
    }

    public TokenRefreshResponse refreshToken(String requestRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token không hợp lệ"));

        if (Boolean.TRUE.equals(token.getDaThuHoi()) || token.getNgayHetHan().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new ResourceNotFoundException("Refresh token đã hết hạn hoặc đã bị thu hồi, vui lòng đăng nhập lại");
        }

        User user = userRepository.findById(token.getMaTK())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getTenDangNhap());
        String newAccessToken = jwtService.generateToken(userDetails);

        // Xoay refresh token: hủy token cũ, phát hành token mới
        refreshTokenRepository.delete(token);
        String newRefreshToken = createRefreshToken(user.getMaTK());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .build();
    }

    public void logout(String requestRefreshToken) {
        refreshTokenRepository.findByToken(requestRefreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser(String tenDangNhap) {
        User user = userRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + tenDangNhap));
        return UserMapper.toResponseDTO(user);
    }

    private String createRefreshToken(String maTK) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .maTK(maTK)
                .ngayHetHan(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();
        return refreshTokenRepository.save(refreshToken).getToken();
    }

    private String generateMaTK() {
        return "TK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
