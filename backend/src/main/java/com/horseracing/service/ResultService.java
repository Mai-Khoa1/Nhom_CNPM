package com.horseracing.service;

import com.horseracing.dto.result.*;
import com.horseracing.entity.*;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ResultService - quản lý kết quả thi đấu theo cấp độ chặng đua (race-level aggregate, khớp resultApi.ts).
 * Mỗi chặng đua có nhiều dòng Result (KetQuaThiDua), một dòng / ngựa.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ResultService {

    private final ResultRepository resultRepository;
    private final ScheduleRepository scheduleRepository;
    private final NguaRepository nguaRepository;
    private final NaiNguaRepository naiNguaRepository;
    private final DangKyThiDauRepository dangKyThiDauRepository;
    private final LuatDiemRepository luatDiemRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;

    public ResultResponseDTO submitResults(ResultEntryRequestDTO request, String staffId) {
        Schedule schedule = scheduleRepository.findById(request.getRaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "raceId", request.getRaceId()));

        for (ResultDetailRequestDTO detail : request.getDetails()) {
            DangKyThiDau registration = dangKyThiDauRepository.findById(detail.getRegistrationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "registrationId", detail.getRegistrationId()));

            Double points = luatDiemRepository.findByMaMuaGiaiAndHang(schedule.getMaMuaGiai(), detail.getFinishPosition())
                    .map(LuatDiem::getDiem).orElse(0.0);

            Result result = resultRepository.findByMaChangDuaAndMaNgua(request.getRaceId(), registration.getMaNgua())
                    .orElseGet(() -> Result.builder()
                            .maKetQua(generateMaKetQua())
                            .maChangDua(request.getRaceId())
                            .maNgua(registration.getMaNgua())
                            .build());

            result.setHang(detail.getFinishPosition());
            result.setThoiGianHoanThanh(detail.getFinishTime());
            result.setGhiChuChuyenMon(detail.getNotes());
            result.setDiem(points);

            resultRepository.save(result);
        }

        nhatKyHoatDongService.writeAuditLog(staffId, "SUBMIT_RESULT", "Race:" + request.getRaceId(),
                "Ghi nhận kết quả cho chặng đua " + schedule.getTenChangDua());

        return getResultsByRace(request.getRaceId());
    }

    public ResultResponseDTO publishResults(String raceId, String staffId) {
        Schedule schedule = scheduleRepository.findById(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "raceId", raceId));

        List<Result> results = resultRepository.findByMaChangDua(raceId);
        LocalDateTime now = LocalDateTime.now();
        results.forEach(r -> {
            r.setTrangThaiCongBo(Result.TRANG_THAI_DA_CONG_BO);
            r.setNgayCongBo(now);
        });
        resultRepository.saveAll(results);

        nhatKyHoatDongService.writeAuditLog(staffId, "PUBLISH_RESULT", "Race:" + raceId,
                "Công bố kết quả chặng đua: " + schedule.getTenChangDua());

        return getResultsByRace(raceId);
    }

    @Transactional(readOnly = true)
    public ResultResponseDTO getResultsByRace(String raceId) {
        Schedule schedule = scheduleRepository.findById(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "raceId", raceId));

        List<Result> results = resultRepository.findByMaChangDua(raceId);
        Map<String, DangKyThiDau> registrationByHorse = dangKyThiDauRepository.findByMaChangDua(raceId).stream()
                .collect(Collectors.toMap(DangKyThiDau::getMaNgua, r -> r, (a, b) -> a));

        boolean isPublished = !results.isEmpty() && results.stream()
                .allMatch(r -> Result.TRANG_THAI_DA_CONG_BO.equals(r.getTrangThaiCongBo()));
        LocalDateTime publishedAt = results.stream().map(Result::getNgayCongBo)
                .filter(java.util.Objects::nonNull).findFirst().orElse(null);

        List<ResultDetailResponseDTO> details = results.stream()
                .sorted((a, b) -> a.getHang() != null && b.getHang() != null ? a.getHang() - b.getHang() : 0)
                .map(r -> mapToDetailDTO(r, registrationByHorse.get(r.getMaNgua())))
                .collect(Collectors.toList());

        return ResultResponseDTO.builder()
                .id(raceId)
                .raceId(raceId)
                .raceName(schedule.getTenChangDua())
                .isPublished(isPublished)
                .publishedAt(publishedAt)
                .details(details)
                .build();
    }

    private ResultDetailResponseDTO mapToDetailDTO(Result result, DangKyThiDau registration) {
        Ngua ngua = nguaRepository.findById(result.getMaNgua()).orElse(null);
        NaiNgua naiNgua = registration != null ? naiNguaRepository.findById(registration.getMaNaiNgua()).orElse(null) : null;

        return ResultDetailResponseDTO.builder()
                .finishPosition(result.getHang())
                .horseId(result.getMaNgua())
                .horseName(ngua != null ? ngua.getTenNgua() : null)
                .horseCode(result.getMaNgua())
                .jockeyId(registration != null ? registration.getMaNaiNgua() : null)
                .jockeyName(naiNgua != null ? naiNgua.getHoTen() : null)
                .laneNumber(registration != null ? registration.getSoLan() : null)
                .finishTime(result.getThoiGianHoanThanh())
                .pointsEarned(result.getDiem())
                .notes(result.getGhiChuChuyenMon())
                .build();
    }

    private String generateMaKetQua() {
        return "KQ" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
