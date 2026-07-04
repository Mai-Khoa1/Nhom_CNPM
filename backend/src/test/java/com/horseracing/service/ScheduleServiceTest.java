package com.horseracing.service;

import com.horseracing.dto.common.RaceStatus;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.entity.Schedule;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Kiểm tra M9: chỉ được xóa chặng đua khi ở trạng thái Mở đăng ký (OPEN), không cho xóa khi ONGOING.
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

        assertThatThrownBy(() -> scheduleService.deleteRace("R1", "TK_ORG"))
                .isInstanceOf(ResourceInUseException.class);

        verify(scheduleRepository, never()).delete(any(Schedule.class));
    }

    @Test
    void deleteRace_allowsWhenRaceIsOpenAndHasNoResults() {
        Schedule open = Schedule.builder().maChangDua("R1").tenChangDua("Chặng 1")
                .trangThai(StatusMapper.toTrangThaiChangDua(RaceStatus.OPEN)).build();
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(open));
        when(resultRepository.findByMaChangDua("R1")).thenReturn(List.of());

        scheduleService.deleteRace("R1", "TK_ORG");

        verify((org.springframework.data.repository.CrudRepository<Schedule, String>) scheduleRepository).delete(open);
    }
}
