package com.horseracing.service;

import com.horseracing.dto.common.HorseStatus;
import com.horseracing.dto.common.JockeyStatus;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.lane.LaneRequestDTO;
import com.horseracing.dto.registration.RegistrationRequestDTO;
import com.horseracing.entity.*;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Kiểm tra: đăng ký thi đấu phải cùng chủ sở hữu + cả 2 phải ĐÃ DUYỆT (M4/M7),
 * đếm số đăng ký tối đa bỏ qua bản ghi TỪ CHỐI (M4-4.12), và mỗi làn chỉ gán cho 1 ngựa (M4-4.10).
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private DangKyThiDauRepository dangKyThiDauRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private NguaRepository nguaRepository;
    @Mock private NaiNguaRepository naiNguaRepository;
    @Mock private ChuNguaRepository chuNguaRepository;
    @Mock private NhatKyHoatDongService nhatKyHoatDongService;
    @Mock private NotificationService notificationService;

    @InjectMocks private RegistrationService registrationService;

    private ChuNgua ownerA;
    private ChuNgua ownerB;
    private Schedule race;
    private Ngua approvedHorseOfA;
    private NaiNgua approvedJockeyOfA;
    private NaiNgua jockeyOfB;

    @BeforeEach
    void setUp() {
        ownerA = ChuNgua.builder().maChuNgua("CNA").maTK("TK_A").hoTen("Chủ A").build();
        ownerB = ChuNgua.builder().maChuNgua("CNB").maTK("TK_B").hoTen("Chủ B").build();
        race = Schedule.builder().maChangDua("R1").tenChangDua("Chặng 1").soNguaToiDa(10).build();
        approvedHorseOfA = Ngua.builder().maNgua("N1").maChuNgua("CNA").tenNgua("Ngựa A")
                .trangThai(StatusMapper.toTrangThaiNgua(HorseStatus.APPROVED)).build();
        approvedJockeyOfA = NaiNgua.builder().maNaiNgua("J1").maChuNgua("CNA").hoTen("Nài A")
                .trangThai(StatusMapper.toTrangThaiJockey(JockeyStatus.APPROVED)).build();
        jockeyOfB = NaiNgua.builder().maNaiNgua("J2").maChuNgua("CNB").hoTen("Nài B")
                .trangThai(StatusMapper.toTrangThaiJockey(JockeyStatus.APPROVED)).build();
    }

    private RegistrationRequestDTO dto(String horseId, String jockeyId) {
        RegistrationRequestDTO d = new RegistrationRequestDTO();
        d.setRaceId("R1");
        d.setHorseId(horseId);
        d.setJockeyId(jockeyId);
        return d;
    }

    @Test
    void createRegistration_deniesMixingHorseAndJockeyFromDifferentOwners() {
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(race));
        when(nguaRepository.findById("N1")).thenReturn(Optional.of(approvedHorseOfA));
        when(naiNguaRepository.findById("J2")).thenReturn(Optional.of(jockeyOfB));
        when(chuNguaRepository.findByMaTK("TK_A")).thenReturn(Optional.of(ownerA));

        assertThatThrownBy(() -> registrationService.createRegistration(dto("N1", "J2"), "TK_A", "TK_A"))
                .isInstanceOf(AccessDeniedException.class);

        verify(dangKyThiDauRepository, never()).save(any());
    }

    @Test
    void createRegistration_deniesRegisteringAnotherOwnersHorse() {
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(race));
        Ngua horseOfB = Ngua.builder().maNgua("N2").maChuNgua("CNB").tenNgua("Ngựa B")
                .trangThai(StatusMapper.toTrangThaiNgua(HorseStatus.APPROVED)).build();
        when(nguaRepository.findById("N2")).thenReturn(Optional.of(horseOfB));
        when(naiNguaRepository.findById("J1")).thenReturn(Optional.of(approvedJockeyOfA));
        when(chuNguaRepository.findByMaTK("TK_A")).thenReturn(Optional.of(ownerA));

        assertThatThrownBy(() -> registrationService.createRegistration(dto("N2", "J1"), "TK_A", "TK_A"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createRegistration_blocksWhenHorseNotApproved() {
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(race));
        Ngua pendingHorse = Ngua.builder().maNgua("N1").maChuNgua("CNA").tenNgua("Ngựa A")
                .trangThai(StatusMapper.toTrangThaiNgua(HorseStatus.PENDING)).build();
        when(nguaRepository.findById("N1")).thenReturn(Optional.of(pendingHorse));
        when(naiNguaRepository.findById("J1")).thenReturn(Optional.of(approvedJockeyOfA));
        when(chuNguaRepository.findByMaTK("TK_A")).thenReturn(Optional.of(ownerA));

        assertThatThrownBy(() -> registrationService.createRegistration(dto("N1", "J1"), "TK_A", "TK_A"))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessageContaining("chưa được duyệt");
    }

    @Test
    void createRegistration_succeedsWhenSameOwnerAndBothApproved() {
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(race));
        when(nguaRepository.findById("N1")).thenReturn(Optional.of(approvedHorseOfA));
        when(naiNguaRepository.findById("J1")).thenReturn(Optional.of(approvedJockeyOfA));
        when(chuNguaRepository.findByMaTK("TK_A")).thenReturn(Optional.of(ownerA));
        when(dangKyThiDauRepository.save(any(DangKyThiDau.class))).thenAnswer(inv -> inv.getArgument(0));

        registrationService.createRegistration(dto("N1", "J1"), "TK_A", "TK_A");

        verify(dangKyThiDauRepository).save(any(DangKyThiDau.class));
    }

    @Test
    void createRegistration_maxHorsesCountIgnoresRejectedRegistrations() {
        Schedule fullRace = Schedule.builder().maChangDua("R1").tenChangDua("Chặng 1").soNguaToiDa(1).build();
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(fullRace));
        when(nguaRepository.findById("N1")).thenReturn(Optional.of(approvedHorseOfA));
        when(naiNguaRepository.findById("J1")).thenReturn(Optional.of(approvedJockeyOfA));
        when(chuNguaRepository.findByMaTK("TK_A")).thenReturn(Optional.of(ownerA));
        // Đã có 1 đăng ký nhưng bị TỪ CHỐI -> không được tính vào số lượng tối đa
        when(dangKyThiDauRepository.countByMaChangDuaAndTrangThaiNot("R1", DangKyThiDau.TRANG_THAI_TU_CHOI))
                .thenReturn(0L);
        when(dangKyThiDauRepository.save(any(DangKyThiDau.class))).thenAnswer(inv -> inv.getArgument(0));

        registrationService.createRegistration(dto("N1", "J1"), "TK_A", "TK_A");

        verify(dangKyThiDauRepository).save(any(DangKyThiDau.class));
    }

    @Test
    void createRegistration_blocksWhenApprovedRegistrationsReachMax() {
        Schedule fullRace = Schedule.builder().maChangDua("R1").tenChangDua("Chặng 1").soNguaToiDa(1).build();
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(fullRace));
        when(nguaRepository.findById("N1")).thenReturn(Optional.of(approvedHorseOfA));
        when(naiNguaRepository.findById("J1")).thenReturn(Optional.of(approvedJockeyOfA));
        when(chuNguaRepository.findByMaTK("TK_A")).thenReturn(Optional.of(ownerA));
        when(dangKyThiDauRepository.countByMaChangDuaAndTrangThaiNot("R1", DangKyThiDau.TRANG_THAI_TU_CHOI))
                .thenReturn(1L);

        assertThatThrownBy(() -> registrationService.createRegistration(dto("N1", "J1"), "TK_A", "TK_A"))
                .isInstanceOf(ResourceInUseException.class);
    }

    @Test
    void assignLane_blocksDuplicateLaneInSameRace() {
        DangKyThiDau registration = DangKyThiDau.builder().maDangKy("DK1").maChangDua("R1").build();
        when(dangKyThiDauRepository.findById("DK1")).thenReturn(Optional.of(registration));
        when(dangKyThiDauRepository.existsByMaChangDuaAndSoLanAndMaDangKyNot("R1", 3, "DK1")).thenReturn(true);

        LaneRequestDTO dto = new LaneRequestDTO("DK1", 3);

        assertThatThrownBy(() -> registrationService.assignLane(dto, "TK_ORG"))
                .isInstanceOf(DuplicateResourceException.class);

        verify(dangKyThiDauRepository, never()).save(any());
    }

    @Test
    void assignLane_allowsWhenLaneIsFree() {
        DangKyThiDau registration = DangKyThiDau.builder().maDangKy("DK1").maChangDua("R1").build();
        when(dangKyThiDauRepository.findById("DK1")).thenReturn(Optional.of(registration));
        when(dangKyThiDauRepository.existsByMaChangDuaAndSoLanAndMaDangKyNot("R1", 3, "DK1")).thenReturn(false);
        when(dangKyThiDauRepository.save(any(DangKyThiDau.class))).thenAnswer(inv -> inv.getArgument(0));

        LaneRequestDTO dto = new LaneRequestDTO("DK1", 3);
        registrationService.assignLane(dto, "TK_ORG");

        verify(dangKyThiDauRepository).save(any(DangKyThiDau.class));
    }
}
