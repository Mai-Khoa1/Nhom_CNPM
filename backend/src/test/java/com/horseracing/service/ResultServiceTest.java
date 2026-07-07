package com.horseracing.service;

import com.horseracing.dto.result.ResultDetailRequestDTO;
import com.horseracing.dto.result.ResultEntryRequestDTO;
import com.horseracing.entity.*;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Kiểm tra M5: điểm phải được tính từ LuatDiem và LƯU vào Result (không còn luôn = 0),
 * công bố kết quả tính lại điểm chính thức, thời gian/thứ hạng phải nhất quán,
 * và sửa kết quả đã công bố phải có xác nhận rõ ràng.
 */
@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock private ResultRepository resultRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private NguaRepository nguaRepository;
    @Mock private NaiNguaRepository naiNguaRepository;
    @Mock private DangKyThiDauRepository dangKyThiDauRepository;
    @Mock private LuatDiemRepository luatDiemRepository;
    @Mock private ChuNguaRepository chuNguaRepository;
    @Mock private NhatKyHoatDongService nhatKyHoatDongService;
    @Mock private NotificationService notificationService;

    @InjectMocks private ResultService resultService;

    private Schedule race;
    private DangKyThiDau registration;

    @BeforeEach
    void setUp() {
        race = Schedule.builder().maChangDua("R1").maMuaGiai("MG1").tenChangDua("Chặng 1").build();
        registration = DangKyThiDau.builder().maDangKy("DK1").maChangDua("R1").maNgua("N1").maNaiNgua("J1")
                .trangThai(DangKyThiDau.TRANG_THAI_DA_DUYET).build();
    }

    @Test
    void submitResults_computesAndPersistsPointsFromPointRule() {
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(race));
        when(resultRepository.findByMaChangDua("R1")).thenReturn(List.of());
        when(dangKyThiDauRepository.findById("DK1")).thenReturn(Optional.of(registration));
        when(resultRepository.findByMaChangDuaAndMaNgua("R1", "N1")).thenReturn(Optional.empty());
        when(luatDiemRepository.findByMaMuaGiaiAndHang("MG1", 1))
                .thenReturn(Optional.of(LuatDiem.builder().hang(1).diem(10.0).build()));
        ArgumentCaptor<Result> captor = ArgumentCaptor.forClass(Result.class);
        when(resultRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        ResultDetailRequestDTO detail = new ResultDetailRequestDTO();
        detail.setRegistrationId("DK1");
        detail.setFinishPosition(1);
        detail.setFinishTime("2:15.0");
        ResultEntryRequestDTO request = new ResultEntryRequestDTO();
        request.setRaceId("R1");
        request.setDetails(List.of(detail));

        resultService.submitResults(request, "TK_ORG");

        assertThat(captor.getValue().getDiem()).isEqualTo(10.0);
    }

    @Test
    void submitResults_rejectsInconsistentRankAndTime() {
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(race));
        when(resultRepository.findByMaChangDua("R1")).thenReturn(List.of());

        ResultDetailRequestDTO first = new ResultDetailRequestDTO();
        first.setRegistrationId("DK1");
        first.setFinishPosition(1);
        first.setFinishTime("3:00.0"); // hạng 1 nhưng thời gian dài hơn hạng 2 -> không hợp lệ

        ResultDetailRequestDTO second = new ResultDetailRequestDTO();
        second.setRegistrationId("DK2");
        second.setFinishPosition(2);
        second.setFinishTime("2:00.0");

        ResultEntryRequestDTO request = new ResultEntryRequestDTO();
        request.setRaceId("R1");
        request.setDetails(List.of(first, second));

        assertThatThrownBy(() -> resultService.submitResults(request, "TK_ORG"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("không nhất quán");

        verify(resultRepository, never()).save(any());
    }

    @Test
    void submitResults_blocksEditingPublishedResultsWithoutConfirmation() {
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(race));
        Result published = Result.builder().maKetQua("K1").maChangDua("R1").maNgua("N1")
                .trangThaiCongBo(Result.TRANG_THAI_DA_CONG_BO).build();
        when(resultRepository.findByMaChangDua("R1")).thenReturn(List.of(published));

        ResultDetailRequestDTO detail = new ResultDetailRequestDTO();
        detail.setRegistrationId("DK1");
        detail.setFinishPosition(1);
        ResultEntryRequestDTO request = new ResultEntryRequestDTO();
        request.setRaceId("R1");
        request.setDetails(List.of(detail));
        request.setConfirmEditPublished(false);

        assertThatThrownBy(() -> resultService.submitResults(request, "TK_ORG"))
                .isInstanceOf(ResourceInUseException.class);

        verify(resultRepository, never()).save(any());
    }

    @Test
    void submitResults_allowsEditingPublishedResultsWithConfirmation() {
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(race));
        Result published = Result.builder().maKetQua("K1").maChangDua("R1").maNgua("N1")
                .trangThaiCongBo(Result.TRANG_THAI_DA_CONG_BO).build();
        when(resultRepository.findByMaChangDua("R1")).thenReturn(List.of(published));
        when(dangKyThiDauRepository.findById("DK1")).thenReturn(Optional.of(registration));
        when(resultRepository.findByMaChangDuaAndMaNgua("R1", "N1")).thenReturn(Optional.of(published));
        when(resultRepository.save(any(Result.class))).thenAnswer(inv -> inv.getArgument(0));

        ResultDetailRequestDTO detail = new ResultDetailRequestDTO();
        detail.setRegistrationId("DK1");
        detail.setFinishPosition(1);
        ResultEntryRequestDTO request = new ResultEntryRequestDTO();
        request.setRaceId("R1");
        request.setDetails(List.of(detail));
        request.setConfirmEditPublished(true);

        resultService.submitResults(request, "TK_ORG");

        verify(resultRepository).save(any(Result.class));
    }

    @Test
    void publishResults_blocksWhenNoResultsExist() {
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(race));
        when(resultRepository.findByMaChangDua("R1")).thenReturn(List.of());

        assertThatThrownBy(() -> resultService.publishResults("R1", "TK_ORG"))
                .isInstanceOf(ResourceInUseException.class);
    }

    @Test
    void publishResults_recomputesOfficialPointsFromCurrentPointRules() {
        when(scheduleRepository.findById("R1")).thenReturn(Optional.of(race));
        Result draft = Result.builder().maKetQua("K1").maChangDua("R1").maNgua("N1").hang(1).diem(0.0).build();
        when(resultRepository.findByMaChangDua("R1")).thenReturn(List.of(draft));
        when(dangKyThiDauRepository.findByMaChangDua("R1")).thenReturn(List.of(registration));
        when(luatDiemRepository.findByMaMuaGiaiAndHang("MG1", 1))
                .thenReturn(Optional.of(LuatDiem.builder().hang(1).diem(10.0).build()));
        when(nguaRepository.findById("N1")).thenReturn(Optional.empty());

        resultService.publishResults("R1", "TK_ORG");

        assertThat(draft.getDiem()).isEqualTo(10.0);
        assertThat(draft.getTrangThaiCongBo()).isEqualTo(Result.TRANG_THAI_DA_CONG_BO);
        verify(resultRepository).saveAll(List.of(draft));
    }
}
