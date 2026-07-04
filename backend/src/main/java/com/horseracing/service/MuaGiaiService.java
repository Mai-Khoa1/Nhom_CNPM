package com.horseracing.service;

import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.SeasonStatus;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.season.PointRuleRequestDTO;
import com.horseracing.dto.season.PointRuleResponseDTO;
import com.horseracing.dto.season.SeasonRequestDTO;
import com.horseracing.dto.season.SeasonResponseDTO;
import com.horseracing.entity.LuatDiem;
import com.horseracing.entity.MuaGiai;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.LuatDiemRepository;
import com.horseracing.repository.MuaGiaiRepository;
import com.horseracing.repository.ScheduleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MuaGiaiService - quản lý mùa giải (Season) và luật tính điểm (PointRule).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MuaGiaiService {

    private final MuaGiaiRepository muaGiaiRepository;
    private final LuatDiemRepository luatDiemRepository;
    private final ScheduleRepository scheduleRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;

    /** Luật tính điểm mặc định cho mùa giải mới: hạng 1 = 10đ, hạng 2 = 7đ, hạng 3 = 5đ, hạng 4 = 3đ, hạng 5 = 2đ, hạng 6 = 1đ. */
    private static final double[] DEFAULT_POINTS_BY_RANK = {10, 7, 5, 3, 2, 1};

    public SeasonResponseDTO createSeason(SeasonRequestDTO dto, String staffId) {
        MuaGiai muaGiai = MuaGiai.builder()
                .maMuaGiai(generateMaMuaGiai())
                .tenMuaGiai(dto.getName())
                .ngayBatDau(parseDate(dto.getStartDate()))
                .ngayKetThuc(parseDate(dto.getEndDate()))
                .moTa(dto.getDescription())
                .build();

        MuaGiai saved = muaGiaiRepository.save(muaGiai);
        seedDefaultPointRules(saved.getMaMuaGiai());
        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_SEASON", "Season:" + saved.getMaMuaGiai(),
                "Tạo mùa giải mới: " + saved.getTenMuaGiai());

        return mapToResponseDTO(saved);
    }

    private void seedDefaultPointRules(String maMuaGiai) {
        for (int i = 0; i < DEFAULT_POINTS_BY_RANK.length; i++) {
            luatDiemRepository.save(LuatDiem.builder()
                    .maLuatDiem("LD" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase())
                    .maMuaGiai(maMuaGiai)
                    .hang(i + 1)
                    .diem(DEFAULT_POINTS_BY_RANK[i])
                    .build());
        }
    }

    public SeasonResponseDTO updateSeason(String maMuaGiai, SeasonRequestDTO dto, String staffId) {
        MuaGiai muaGiai = muaGiaiRepository.findById(maMuaGiai)
                .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "id", maMuaGiai));

        muaGiai.setTenMuaGiai(dto.getName());
        muaGiai.setNgayBatDau(parseDate(dto.getStartDate()));
        muaGiai.setNgayKetThuc(parseDate(dto.getEndDate()));
        muaGiai.setMoTa(dto.getDescription());

        MuaGiai updated = muaGiaiRepository.save(muaGiai);
        nhatKyHoatDongService.writeAuditLog(staffId, "UPDATE_SEASON", "Season:" + maMuaGiai,
                "Cập nhật mùa giải: " + updated.getTenMuaGiai());

        return mapToResponseDTO(updated);
    }

    public void deleteSeason(String maMuaGiai, String staffId) {
        MuaGiai muaGiai = muaGiaiRepository.findById(maMuaGiai)
                .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "id", maMuaGiai));

        if (scheduleRepository.countByMaMuaGiai(maMuaGiai) > 0) {
            throw new ResourceInUseException(
                    "Không thể xóa mùa giải '" + muaGiai.getTenMuaGiai() + "' vì đã có chặng đua thuộc mùa giải này.");
        }

        luatDiemRepository.deleteByMaMuaGiai(maMuaGiai);
        muaGiaiRepository.delete(muaGiai);

        nhatKyHoatDongService.writeAuditLog(staffId, "DELETE_SEASON", "Season:" + maMuaGiai,
                "Đã xóa mùa giải: " + muaGiai.getTenMuaGiai());
    }

    public void closeSeason(String maMuaGiai, String staffId) {
        MuaGiai muaGiai = muaGiaiRepository.findById(maMuaGiai)
                .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "id", maMuaGiai));

        long openRaces = scheduleRepository.countByMaMuaGiaiAndTrangThai(
                maMuaGiai, StatusMapper.toTrangThaiChangDua(com.horseracing.dto.common.RaceStatus.OPEN));
        if (openRaces > 0) {
            throw new ResourceInUseException(
                    "Không thể đóng mùa giải '" + muaGiai.getTenMuaGiai() + "' vì còn " + openRaces
                            + " chặng đua đang ở trạng thái Mở đăng ký. Hãy đóng/hoàn thành các chặng đua này trước.");
        }

        muaGiai.setTrangThai(StatusMapper.toTrangThaiMuaGiai(SeasonStatus.CLOSED));
        muaGiaiRepository.save(muaGiai);

        nhatKyHoatDongService.writeAuditLog(staffId, "CLOSE_SEASON", "Season:" + maMuaGiai,
                "Đã đóng mùa giải: " + muaGiai.getTenMuaGiai());
    }

    @Transactional(readOnly = true)
    public SeasonResponseDTO getSeasonById(String maMuaGiai) {
        MuaGiai muaGiai = muaGiaiRepository.findById(maMuaGiai)
                .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "id", maMuaGiai));
        return mapToResponseDTO(muaGiai);
    }

    @Transactional(readOnly = true)
    public PageResponse<SeasonResponseDTO> getAllSeasons(Pageable pageable, String keyword, SeasonStatus status) {
        Specification<MuaGiai> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tenMuaGiai")), "%" + keyword.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("trangThai"), StatusMapper.toTrangThaiMuaGiai(status)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(muaGiaiRepository.findAll(spec, pageable), this::mapToResponseDTO);
    }

    // ----- Point rules -----

    @Transactional(readOnly = true)
    public List<PointRuleResponseDTO> getPointRules(String maMuaGiai) {
        muaGiaiRepository.findById(maMuaGiai)
                .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "id", maMuaGiai));
        return luatDiemRepository.findByMaMuaGiaiOrderByHangAsc(maMuaGiai).stream()
                .map(this::mapToPointRuleDTO)
                .collect(Collectors.toList());
    }

    public List<PointRuleResponseDTO> setPointRules(String maMuaGiai, List<PointRuleRequestDTO> rules, String staffId) {
        muaGiaiRepository.findById(maMuaGiai)
                .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "id", maMuaGiai));

        luatDiemRepository.deleteByMaMuaGiai(maMuaGiai);
        List<LuatDiem> saved = rules.stream()
                .map(r -> luatDiemRepository.save(LuatDiem.builder()
                        .maLuatDiem("LD" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase())
                        .maMuaGiai(maMuaGiai)
                        .hang(r.getPosition())
                        .diem(r.getPoint())
                        .build()))
                .collect(Collectors.toList());

        nhatKyHoatDongService.writeAuditLog(staffId, "UPDATE_POINT_RULES", "Season:" + maMuaGiai,
                "Cập nhật luật tính điểm cho mùa giải " + maMuaGiai);

        return saved.stream().map(this::mapToPointRuleDTO).collect(Collectors.toList());
    }

    private PointRuleResponseDTO mapToPointRuleDTO(LuatDiem rule) {
        return PointRuleResponseDTO.builder()
                .id(rule.getMaLuatDiem())
                .seasonId(rule.getMaMuaGiai())
                .position(rule.getHang())
                .point(rule.getDiem())
                .build();
    }

    private SeasonResponseDTO mapToResponseDTO(MuaGiai muaGiai) {
        return SeasonResponseDTO.builder()
                .id(muaGiai.getMaMuaGiai())
                .name(muaGiai.getTenMuaGiai())
                .startDate(muaGiai.getNgayBatDau())
                .endDate(muaGiai.getNgayKetThuc())
                .status(StatusMapper.toSeasonStatus(muaGiai.getTrangThai()))
                .description(muaGiai.getMoTa())
                .createdAt(muaGiai.getNgayTao())
                .build();
    }

    private String generateMaMuaGiai() {
        return "MG" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr);
    }
}
