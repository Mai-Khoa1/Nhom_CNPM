package com.horseracing.service;

import com.horseracing.dto.common.RaceStatus;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.entity.DangKyThiDau;
import com.horseracing.entity.Schedule;
import com.horseracing.exception.PendingRegistrationsExistException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.repository.DangKyThiDauRepository;
import com.horseracing.repository.MuaGiaiRepository;
import com.horseracing.repository.ResultRepository;
import com.horseracing.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Kiểm tra M9: chỉ được xóa chặng đua khi ở trạng thái Mở đăng ký (OPEN), không cho xóa khi ONGOING.
 * Kiểm tra publishRace: chặn chuyển OPEN -> ONGOING khi còn đăng ký PENDING (không tự động reject
 * thay Ban tổ chức), và dùng findByIdForUpdate (khóa ghi) để chống race condition với createRegistration.
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock private ScheduleRepository scheduleRepository;
    @Mock private MuaGiaiRepository muaGiaiRepository;
    @Mock private ResultRepository resultRepository;
    @Mock private DangKyThiDauRepository dangKyThiDauRepository;
    @Mock private NhatKyHoatDongService nhatKyHoatDongService;

    @InjectMocks private ScheduleService scheduleService;

    @Test
    void deleteRace_blocksWhenRaceIsOngoing() {
        Schedule ongoing = Schedule.builder().maChangDua("R1").tenChangDua("Chặng 1")
                .trangThai(StatusMapper.toTrangThaiChangDua(RaceStatus.ONGOING)).build();
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(ongoing));

        assertThatThrownBy(() -> scheduleService.deleteRace("R1", "TK_ORG", null))
                .isInstanceOf(ResourceInUseException.class);

        verify(scheduleRepository, never()).delete(any(Schedule.class));
    }

    @Test
    void deleteRace_allowsWhenRaceIsOpenAndHasNoResults() {
        Schedule open = Schedule.builder().maChangDua("R1").tenChangDua("Chặng 1")
                .trangThai(StatusMapper.toTrangThaiChangDua(RaceStatus.OPEN)).build();
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(open));
        when(resultRepository.findByMaChangDua("R1")).thenReturn(List.of());

        scheduleService.deleteRace("R1", "TK_ORG", null);

        verify((org.springframework.data.repository.CrudRepository<Schedule, String>) scheduleRepository).delete(open);
    }

    @Test
    void publishRace_blocksWhenPendingRegistrationsExist() {
        Schedule open = Schedule.builder().maChangDua("R1").tenChangDua("Chặng 1")
                .trangThai(StatusMapper.toTrangThaiChangDua(RaceStatus.OPEN))
                .soLuongToiThieu(2).build();
        when(scheduleRepository.findByIdForUpdate("R1")).thenReturn(Optional.of(open));
        when(dangKyThiDauRepository.countByMaChangDuaAndTrangThai("R1", DangKyThiDau.TRANG_THAI_CHO_DUYET))
                .thenReturn(3L);

        assertThatThrownBy(() -> scheduleService.publishRace("R1", "TK_ORG", null))
                .isInstanceOf(PendingRegistrationsExistException.class)
                .satisfies(ex -> assertThat(((PendingRegistrationsExistException) ex).getPendingCount()).isEqualTo(3L))
                .hasMessageContaining("3")
                .hasMessageContaining("chưa duyệt");

        // Không được đi tới bước lưu trạng thái mới - vẫn phải OPEN, không tự ý bỏ qua đăng ký PENDING.
        verify(scheduleRepository, never()).save(any(Schedule.class));
        // Đếm/khóa đúng dòng ChangDua cần chuyển trạng thái, không dùng findById thường (không có khóa).
        verify(scheduleRepository).findByIdForUpdate("R1");
        verify(scheduleRepository, never()).findById("R1");
    }

    @Test
    void publishRace_succeedsWhenNoPendingAndEnoughApprovedWithLanesAssigned() {
        Schedule open = Schedule.builder().maChangDua("R1").tenChangDua("Chặng 1")
                .trangThai(StatusMapper.toTrangThaiChangDua(RaceStatus.OPEN))
                .soLuongToiThieu(2).build();
        when(scheduleRepository.findByIdForUpdate("R1")).thenReturn(Optional.of(open));
        when(dangKyThiDauRepository.countByMaChangDuaAndTrangThai("R1", DangKyThiDau.TRANG_THAI_CHO_DUYET))
                .thenReturn(0L);
        DangKyThiDau reg1 = DangKyThiDau.builder().maDangKy("DK1").maChangDua("R1").soLan(1).build();
        DangKyThiDau reg2 = DangKyThiDau.builder().maDangKy("DK2").maChangDua("R1").soLan(2).build();
        when(dangKyThiDauRepository.findByMaChangDuaAndTrangThai("R1", DangKyThiDau.TRANG_THAI_DA_DUYET))
                .thenReturn(List.of(reg1, reg2));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduleService.publishRace("R1", "TK_ORG", null);

        verify(scheduleRepository).save(argThat(s -> StatusMapper.toRaceStatus(s.getTrangThai()) == RaceStatus.ONGOING));
    }
}
