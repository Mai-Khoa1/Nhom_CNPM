package com.horseracing.service;

import com.horseracing.dto.common.NotificationType;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.notification.NotificationResponseDTO;
import com.horseracing.entity.ThongBao;
import com.horseracing.entity.User;
import com.horseracing.repository.ThongBaoRepository;
import com.horseracing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * NotificationService - quản lý thông báo (ThongBao) gửi tới người dùng.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final ThongBaoRepository thongBaoRepository;
    private final UserRepository userRepository;

    /** Gửi thông báo tới một tài khoản - dùng nội bộ bởi các service khác (duyệt/từ chối, công bố kết quả...). */
    public void notify(String maTK, String title, String message, NotificationType type, String targetType, String targetId) {
        if (maTK == null) return;
        ThongBao thongBao = ThongBao.builder()
                .maThongBao("TB" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                .maTK(maTK)
                .tieuDe(title)
                .noiDung(message)
                .loai(type.name())
                .loaiDoiTuong(targetType)
                .maDoiTuong(targetId)
                .build();
        thongBaoRepository.save(thongBao);
    }

    /** Gửi thông báo tới toàn bộ Admin và Ban tổ chức - dùng khi có hồ sơ mới cần duyệt (ngựa, jockey, đăng ký thi đấu...). */
    public void notifyAdmins(String title, String message, NotificationType type, String targetType, String targetId) {
        List<User> admins = userRepository.findByVaiTroIn(List.of(User.VAI_TRO_ADMIN, User.VAI_TRO_BAN_TO_CHUC));
        for (User admin : admins) {
            notify(admin.getMaTK(), title, message, type, targetType, targetId);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponseDTO> getNotifications(String maTK, Boolean isRead, Pageable pageable) {
        if (isRead == null) {
            return PageResponse.of(thongBaoRepository.findByMaTK(maTK, pageable), this::mapToResponseDTO);
        }
        String trangThai = isRead ? ThongBao.DA_DOC : ThongBao.CHUA_DOC;
        return PageResponse.of(thongBaoRepository.findByMaTKAndTrangThai(maTK, trangThai, pageable), this::mapToResponseDTO);
    }

    public void markRead(String maThongBao, String requesterMaTK) {
        thongBaoRepository.findById(maThongBao).ifPresent(tb -> {
            if (!tb.getMaTK().equals(requesterMaTK)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Bạn không có quyền thao tác trên thông báo của người khác");
            }
            tb.setTrangThai(ThongBao.DA_DOC);
            thongBaoRepository.save(tb);
        });
    }

    public void markAllRead(String maTK) {
        thongBaoRepository.findByMaTKAndTrangThai(maTK, ThongBao.CHUA_DOC)
                .forEach(tb -> tb.setTrangThai(ThongBao.DA_DOC));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String maTK) {
        return thongBaoRepository.countByMaTKAndTrangThai(maTK, ThongBao.CHUA_DOC);
    }

    private NotificationResponseDTO mapToResponseDTO(ThongBao tb) {
        return NotificationResponseDTO.builder()
                .id(tb.getMaThongBao())
                .userId(tb.getMaTK())
                .title(tb.getTieuDe())
                .message(tb.getNoiDung())
                .type(parseType(tb.getLoai()))
                .isRead(ThongBao.DA_DOC.equals(tb.getTrangThai()))
                .targetType(tb.getLoaiDoiTuong())
                .targetId(tb.getMaDoiTuong())
                .createdAt(tb.getNgayGui())
                .build();
    }

    private NotificationType parseType(String loai) {
        try {
            return loai != null ? NotificationType.valueOf(loai) : NotificationType.SYSTEM;
        } catch (IllegalArgumentException e) {
            return NotificationType.SYSTEM;
        }
    }
}
