package com.horseracing.service;

import com.horseracing.dto.common.NotificationType;
import com.horseracing.dto.result.*;
import com.horseracing.entity.*;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ChuNguaRepository chuNguaRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;
    private final NotificationService notificationService;

    public ResultResponseDTO submitResults(ResultEntryRequestDTO request, String staffId) {
        Schedule schedule = scheduleRepository.findById(request.getRaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "raceId", request.getRaceId()));

        boolean alreadyPublished = resultRepository.findByMaChangDua(request.getRaceId()).stream()
                .anyMatch(r -> Result.TRANG_THAI_DA_CONG_BO.equals(r.getTrangThaiCongBo()));
        if (alreadyPublished && !request.isConfirmEditPublished()) {
            throw new ResourceInUseException(
                    "Kết quả chặng đua '" + schedule.getTenChangDua() + "' đã được công bố. "
                    + "Xác nhận rõ ràng (confirmEditPublished=true) nếu vẫn muốn sửa.");
        }

        validateRankTimeConsistency(request.getDetails());

        for (ResultDetailRequestDTO detail : request.getDetails()) {
            DangKyThiDau registration = dangKyThiDauRepository.findById(detail.getRegistrationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "registrationId", detail.getRegistrationId()));

            Result result = resultRepository.findByMaChangDuaAndMaNgua(request.getRaceId(), registration.getMaNgua())
                    .orElseGet(() -> Result.builder()
                            .maKetQua(generateMaKetQua())
                            .maChangDua(request.getRaceId())
                            .maNgua(registration.getMaNgua())
                            .build());

            result.setHang(detail.getFinishPosition());
            result.setThoiGianHoanThanh(detail.getFinishTime());
            result.setGhiChuChuyenMon(detail.getNotes());
            result.setDiem(resolvePoints(schedule.getMaMuaGiai(), detail.getFinishPosition()));
            if (alreadyPublished) {
                // Vẫn giữ trạng thái đã công bố (không "hoàn tác" công bố ngầm), chỉ cập nhật số liệu.
                result.setTrangThaiCongBo(Result.TRANG_THAI_DA_CONG_BO);
            }

            resultRepository.save(result);
        }

        nhatKyHoatDongService.writeAuditLog(staffId,
                alreadyPublished ? "EDIT_PUBLISHED_RESULT" : "SUBMIT_RESULT", "Race:" + request.getRaceId(),
                "Ghi nhận kết quả cho chặng đua " + schedule.getTenChangDua()
                        + (alreadyPublished ? " (đã công bố, sửa có xác nhận)" : ""));

        return getResultsByRace(request.getRaceId());
    }

    public ResultResponseDTO publishResults(String raceId, String staffId) {
        Schedule schedule = scheduleRepository.findById(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "raceId", raceId));

        List<Result> results = resultRepository.findByMaChangDua(raceId);
        if (results.isEmpty()) {
            throw new ResourceInUseException("Chặng đua '" + schedule.getTenChangDua() + "' chưa có kết quả để công bố.");
        }

        LocalDateTime now = LocalDateTime.now();
        results.forEach(r -> {
            // Tính lại điểm theo luật điểm hiện hành của mùa giải tại thời điểm công bố (điểm chính thức).
            r.setDiem(resolvePoints(schedule.getMaMuaGiai(), r.getHang()));
            r.setTrangThaiCongBo(Result.TRANG_THAI_DA_CONG_BO);
            r.setNgayCongBo(now);
        });
        resultRepository.saveAll(results);

        nhatKyHoatDongService.writeAuditLog(staffId, "PUBLISH_RESULT", "Race:" + raceId,
                "Công bố kết quả chặng đua: " + schedule.getTenChangDua());

        notifyOwnersOnPublish(raceId, schedule, results);

        return getResultsByRace(raceId);
    }

    /** Điểm theo luật điểm (LuatDiem) của mùa giải cho 1 hạng; không có luật -> 0 điểm. */
    private Double resolvePoints(String maMuaGiai, Integer hang) {
        if (hang == null) return 0.0;
        return luatDiemRepository.findByMaMuaGiaiAndHang(maMuaGiai, hang)
                .map(LuatDiem::getDiem).orElse(0.0);
    }

    /** Thời gian về đích và thứ hạng phải nhất quán: hạng tốt hơn (số nhỏ hơn) phải có thời gian không lớn hơn hạng kém hơn. */
    private void validateRankTimeConsistency(List<ResultDetailRequestDTO> details) {
        List<ResultDetailRequestDTO> withTime = details.stream()
                .filter(d -> parseFinishTimeSeconds(d.getFinishTime()) != null)
                .sorted(Comparator.comparing(ResultDetailRequestDTO::getFinishPosition))
                .collect(Collectors.toList());

        for (int i = 0; i < withTime.size() - 1; i++) {
            ResultDetailRequestDTO current = withTime.get(i);
            ResultDetailRequestDTO next = withTime.get(i + 1);
            double t1 = parseFinishTimeSeconds(current.getFinishTime());
            double t2 = parseFinishTimeSeconds(next.getFinishTime());
            if (t1 > t2) {
                throw new IllegalArgumentException(
                        "Thời gian về đích không nhất quán với thứ hạng: hạng " + current.getFinishPosition()
                        + " (" + current.getFinishTime() + ") phải nhanh hơn hoặc bằng hạng "
                        + next.getFinishPosition() + " (" + next.getFinishTime() + ")");
            }
        }
    }

    /** Hỗ trợ định dạng "giây", "phút:giây" hoặc "giờ:phút:giây" (VD "2:15.3" = 2 phút 15.3 giây). */
    private Double parseFinishTimeSeconds(String time) {
        if (time == null || time.isBlank()) return null;
        String t = time.trim();
        try {
            String[] parts = t.split(":");
            double seconds = 0;
            for (String part : parts) {
                seconds = seconds * 60 + Double.parseDouble(part.trim());
            }
            return seconds;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void notifyOwnersOnPublish(String raceId, Schedule schedule, List<Result> results) {
        Set<String> notifiedOwners = new HashSet<>();

        for (Result result : results) {
            Ngua ngua = nguaRepository.findById(result.getMaNgua()).orElse(null);
            if (ngua == null) continue;
            ChuNgua chuNgua = chuNguaRepository.findById(ngua.getMaChuNgua()).orElse(null);
            if (chuNgua == null || !notifiedOwners.add(chuNgua.getMaTK())) continue;

            notificationService.notify(chuNgua.getMaTK(),
                    "Kết quả chặng đua đã được công bố",
                    "Kết quả chặng đua '" + schedule.getTenChangDua() + "' đã được công bố. "
                            + "Ngựa " + ngua.getTenNgua() + " về hạng " + result.getHang() + ", đạt "
                            + result.getDiem() + " điểm.",
                    NotificationType.SYSTEM, "RACE", raceId);
        }
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
