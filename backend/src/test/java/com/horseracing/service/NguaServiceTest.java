package com.horseracing.service;

import com.horseracing.dto.ngua.NguaRequestDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.Ngua;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.DangKyThiDauRepository;
import com.horseracing.repository.NguaRepository;
import com.horseracing.repository.ResultRepository;
import com.horseracing.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Kiểm tra nhóm lỗi phân quyền dữ liệu giữa các chủ ngựa (NguaService):
 * chủ ngựa A không được sửa/xóa ngựa của chủ ngựa B, và mã ngựa từng bị TỪ CHỐI được phép tạo lại.
 */
@ExtendWith(MockitoExtension.class)
class NguaServiceTest {

    @Mock private NguaRepository nguaRepository;
    @Mock private ChuNguaRepository chuNguaRepository;
    @Mock private ResultRepository resultRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private DangKyThiDauRepository dangKyThiDauRepository;
    @Mock private NhatKyHoatDongService nhatKyHoatDongService;
    @Mock private UpdateRequestService updateRequestService;

    @InjectMocks private NguaService nguaService;

    private ChuNgua ownerA;
    private ChuNgua ownerB;
    private Ngua horseOfOwnerA;

    @BeforeEach
    void setUp() {
        ownerA = ChuNgua.builder().maChuNgua("CNA").maTK("TK_A").hoTen("Chủ A").build();
        ownerB = ChuNgua.builder().maChuNgua("CNB").maTK("TK_B").hoTen("Chủ B").build();
        horseOfOwnerA = Ngua.builder()
                .maNgua("N001").maChuNgua("CNA").tenNgua("Ngựa A")
                .build();
    }

    @Test
    void updateHorse_deniesEditingAnotherOwnersHorse() {
        when(nguaRepository.findById("N001")).thenReturn(Optional.of(horseOfOwnerA));
        when(chuNguaRepository.findById("CNA")).thenReturn(Optional.of(ownerA));

        NguaRequestDTO dto = NguaRequestDTO.builder().name("Tên mới").build();

        assertThatThrownBy(() -> nguaService.updateHorse("N001", dto, "TK_B", false))
                .isInstanceOf(AccessDeniedException.class);

        verify(nguaRepository, never()).save(any());
    }

    @Test
    void updateHorse_allowsOwnerToEditOwnHorse() {
        when(nguaRepository.findById("N001")).thenReturn(Optional.of(horseOfOwnerA));
        when(chuNguaRepository.findById("CNA")).thenReturn(Optional.of(ownerA));

        NguaRequestDTO dto = NguaRequestDTO.builder().name("Tên mới").build();

        nguaService.updateHorse("N001", dto, "TK_A", false);

        verify(nguaRepository).save(any(Ngua.class));
    }

    @Test
    void updateHorse_privilegedRoleBypassesOwnershipCheck() {
        when(nguaRepository.findById("N001")).thenReturn(Optional.of(horseOfOwnerA));
        when(chuNguaRepository.findById("CNA")).thenReturn(Optional.of(ownerA));

        NguaRequestDTO dto = NguaRequestDTO.builder().name("Tên mới").build();

        // ADMIN/ORGANIZER (privileged=true) sửa được ngựa của chủ khác
        nguaService.updateHorse("N001", dto, "TK_ADMIN", true);

        verify(nguaRepository).save(any(Ngua.class));
    }

    @Test
    void deleteHorse_deniesDeletingAnotherOwnersHorse() {
        when(nguaRepository.findById("N001")).thenReturn(Optional.of(horseOfOwnerA));
        when(chuNguaRepository.findById("CNA")).thenReturn(Optional.of(ownerA));

        assertThatThrownBy(() -> nguaService.deleteHorse("N001", "TK_B", false))
                .isInstanceOf(AccessDeniedException.class);

        verify(nguaRepository, never()).delete(any(Ngua.class));
    }

    @Test
    void createHorse_blocksDuplicateCode() {
        when(chuNguaRepository.findByMaTK("TK_A")).thenReturn(Optional.of(ownerA));
        when(nguaRepository.existsById("N999")).thenReturn(true);

        NguaRequestDTO dto = NguaRequestDTO.builder().code("N999").name("Ngựa mới").build();

        assertThatThrownBy(() -> nguaService.createHorse(dto, "TK_A", "TK_A"))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
