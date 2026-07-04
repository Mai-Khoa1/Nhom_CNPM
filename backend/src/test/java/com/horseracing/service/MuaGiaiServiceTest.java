package com.horseracing.service;

import com.horseracing.dto.common.RaceStatus;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.season.SeasonRequestDTO;
import com.horseracing.entity.LuatDiem;
import com.horseracing.entity.MuaGiai;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.repository.LuatDiemRepository;
import com.horseracing.repository.MuaGiaiRepository;
import com.horseracing.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Kiểm tra M9 (đóng mùa giải khi còn chặng đua Mở đăng ký) và M5 (luật điểm mặc định khi tạo mùa giải mới).
 */
@ExtendWith(MockitoExtension.class)
class MuaGiaiServiceTest {

    @Mock private MuaGiaiRepository muaGiaiRepository;
    @Mock private LuatDiemRepository luatDiemRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private NhatKyHoatDongService nhatKyHoatDongService;

    @InjectMocks private MuaGiaiService muaGiaiService;

    @Test
    void closeSeason_blocksWhenOpenRacesExist() {
        MuaGiai season = MuaGiai.builder().maMuaGiai("MG1").tenMuaGiai("Mùa 1").build();
        when(muaGiaiRepository.findById("MG1")).thenReturn(Optional.of(season));
        when(scheduleRepository.countByMaMuaGiaiAndTrangThai("MG1", StatusMapper.toTrangThaiChangDua(RaceStatus.OPEN)))
                .thenReturn(2L);

        assertThatThrownBy(() -> muaGiaiService.closeSeason("MG1", "TK_ORG"))
                .isInstanceOf(ResourceInUseException.class);

        verify(muaGiaiRepository, never()).save(any());
    }

    @Test
    void closeSeason_allowsWhenNoOpenRaces() {
        MuaGiai season = MuaGiai.builder().maMuaGiai("MG1").tenMuaGiai("Mùa 1").build();
        when(muaGiaiRepository.findById("MG1")).thenReturn(Optional.of(season));
        when(scheduleRepository.countByMaMuaGiaiAndTrangThai("MG1", StatusMapper.toTrangThaiChangDua(RaceStatus.OPEN)))
                .thenReturn(0L);

        muaGiaiService.closeSeason("MG1", "TK_ORG");

        verify(muaGiaiRepository).save(season);
    }

    @Test
    void createSeason_seedsDefaultPointRules() {
        when(muaGiaiRepository.save(any(MuaGiai.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<LuatDiem> captor = ArgumentCaptor.forClass(LuatDiem.class);
        when(luatDiemRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        SeasonRequestDTO dto = new SeasonRequestDTO();
        dto.setName("Mùa mới");

        muaGiaiService.createSeason(dto, "TK_ORG");

        assertThat(captor.getAllValues()).hasSize(6);
        assertThat(captor.getAllValues().get(0).getHang()).isEqualTo(1);
        assertThat(captor.getAllValues().get(0).getDiem()).isEqualTo(10.0);
        assertThat(captor.getAllValues().get(1).getDiem()).isEqualTo(7.0);
        assertThat(captor.getAllValues().get(2).getDiem()).isEqualTo(5.0);
    }
}
