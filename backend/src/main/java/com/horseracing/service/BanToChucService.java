package com.horseracing.service;

import com.horseracing.dto.organizer.OrganizerResponseDTO;
import com.horseracing.entity.BanToChuc;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.BanToChucRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BanToChucService - quản lý hồ sơ Ban tổ chức (mở rộng 1-1 từ tài khoản TaiKhoan có vaiTro = "Ban tổ chức").
 * Hồ sơ được tự động tạo khi tài khoản được gán vai trò ORGANIZER (xem UserService.provisionBanToChucIfNeeded).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BanToChucService {

    private final BanToChucRepository banToChucRepository;

    /** Danh sách Ban tổ chức để chủ ngựa chọn khi tạo ngựa/nài/đăng ký thi đấu. */
    public List<OrganizerResponseDTO> getAllOrganizers() {
        return banToChucRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .sorted(Comparator.comparing(OrganizerResponseDTO::getName))
                .collect(Collectors.toList());
    }

    public BanToChuc getByMaTK(String maTK) {
        return banToChucRepository.findByMaTK(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Ban tổ chức của tài khoản", "maTK", maTK));
    }

    public BanToChuc getById(String maBTC) {
        return banToChucRepository.findById(maBTC)
                .orElseThrow(() -> new ResourceNotFoundException("Ban tổ chức", "maBTC", maBTC));
    }

    private OrganizerResponseDTO mapToResponseDTO(BanToChuc banToChuc) {
        return OrganizerResponseDTO.builder()
                .id(banToChuc.getMaBTC())
                .name(banToChuc.getHoTen())
                .build();
    }
}
