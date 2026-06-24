package com.horseracing.security;

import com.horseracing.dto.common.RoleMapper;
import com.horseracing.entity.User;
import com.horseracing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Tải thông tin người dùng theo tenDangNhap (username) để Spring Security xác thực.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

        boolean enabled = !User.TRANG_THAI_BI_KHOA.equals(user.getTrangThai());

        return new org.springframework.security.core.userdetails.User(
                user.getTenDangNhap(),
                user.getMatKhau(),
                enabled,
                true, true, true,
                List.of(new SimpleGrantedAuthority(RoleMapper.toAuthority(RoleMapper.toRole(user.getVaiTro()))))
        );
    }
}
