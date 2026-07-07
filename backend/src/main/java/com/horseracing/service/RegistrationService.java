package com.horseracing.service;

import com.horseracing.dto.common.NotificationType;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.RaceStatus;
import com.horseracing.dto.common.RegistrationStatus;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.lane.LaneRequestDTO;
import com.horseracing.dto.lane.LaneResponseDTO;
import com.horseracing.dto.registration.RegistrationRequestDTO;
import com.horseracing.dto.registration.RegistrationResponseDTO;
import com.horseracing.entity.*;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RegistrationService - quản lý đăng ký thi đấu (DangKyThiDau) và gán làn đua (Lane, dùng chung entity).
 * Đây cũng là nơi duy nhất lưu trạng thái duyệt/loại (PENDING/APPROVED/REJECTED/CANCELLED/DISQUALIFIED) -
 * hồ sơ Ngựa/Nài (Ngua/NaiNgua) không còn trạng thái duyệt riêng (xem NguaService/NaiNguaService).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {

    /** Trạng thái còn "active" cho 1 (race, ngựa/nài) - chặn đăng ký trùng khi đang ở 1 trong các trạng thái này. */
    private static final List<String> ACTIVE_STATUSES = List.of(
            DangKyThiDau.TRANG_THAI_CHO_DUYET, DangKyThiDau.TRANG_THAI_DA_DUYET, DangKyThiDau.TRANG_THAI_BI_LOAI);
    /** Trạng thái giải phóng slot đăng ký của race (không tính vào soNguaToiDa). */
    private static final List<String> FREED_SLOT_STATUSES = List.of(
            DangKyThiDau.TRANG_THAI_TU_CHOI, DangKyThiDau.TRANG_THAI_DA_HUY);

    private final DangKyThiDauRepository dangKyThiDauRepository;
    private final ScheduleRepository scheduleRepository;
    private final MuaGiaiRepository muaGiaiRepository;
    private final BanToChucRepository banToChucRepository;
    private final NguaRepository nguaRepository;
    private final NaiNguaRepository naiNguaRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final ResultRepository resultRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;
    private final NotificationService notificationService;

    /**
     * Khóa ghi (SELECT ... FOR UPDATE) trên dòng ChangDua ngay khi tạo đăng ký mới - cùng cơ chế khóa
     * với ScheduleService.publishRace, để 2 giao dịch không thể xen kẽ nhau: nếu publishRace đang đếm
     * PENDING/chuyển ONGOING, giao dịch tạo đăng ký này phải đợi tới khi publishRace xong mới được tiếp
     * tục - lúc đó sẽ thấy đúng trạng thái mới nhất của race (và bị chặn bởi check OPEN ngay dưới đây
     * nếu race đã chuyển ONGOING/COMPLETED trong lúc chờ).
     */
    public RegistrationResponseDTO createRegistration(RegistrationRequestDTO dto, String ownerMaTK, String staffId) {
        Schedule schedule = scheduleRepository.findByIdForUpdate(dto.getRaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "raceId", dto.getRaceId()));
        RaceStatus raceStatus = StatusMapper.toRaceStatus(schedule.getTrangThai());
        if (raceStatus != RaceStatus.OPEN) {
            throw new ResourceInUseException(
                    "Chặng đua '" + schedule.getTenChangDua() + "' không còn ở trạng thái Mở đăng ký (hiện tại: "
                            + raceStatus + "), không thể đăng ký thi đấu.");
        }
        Ngua ngua = nguaRepository.findById(dto.getHorseId())
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "horseId", dto.getHorseId()));
        NaiNgua naiNgua = naiNguaRepository.findById(dto.getJockeyId())
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "jockeyId", dto.getJockeyId()));

        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));

        // Ngựa và jockey đăng ký cùng 1 chặng đua phải thuộc cùng 1 chủ sở hữu là người đang đăng ký.
        if (!chuNgua.getMaChuNgua().equals(ngua.getMaChuNgua())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Ngựa '" + ngua.getTenNgua() + "' không thuộc quyền sở hữu của bạn");
        }
        if (!chuNgua.getMaChuNgua().equals(naiNgua.getMaChuNgua())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Jockey '" + naiNgua.getHoTen() + "' không thuộc quyền sở hữu của bạn");
        }

        // Lỗi 6: hồ sơ đã "Ngừng hoạt động" (xóa mềm) không được chọn để tạo đăng ký mới.
        if (Ngua.TRANG_THAI_NGUNG_HOAT_DONG.equals(ngua.getTrangThai())) {
            throw new ResourceInUseException("Ngựa '" + ngua.getTenNgua() + "' đã ngừng hoạt động, không thể đăng ký thi đấu.");
        }
        if (NaiNgua.TRANG_THAI_NGUNG_HOAT_DONG.equals(naiNgua.getTrangThai())) {
            throw new ResourceInUseException("Jockey '" + naiNgua.getHoTen() + "' đã ngừng hoạt động, không thể đăng ký thi đấu.");
        }

        // Chỉ chặn trùng khi bản ghi cũ đang ACTIVE (chờ duyệt/đã duyệt/bị loại) - đã Từ chối/Đã hủy thì được đăng ký lại.
        if (dangKyThiDauRepository.existsByMaChangDuaAndMaNguaAndTrangThaiIn(dto.getRaceId(), dto.getHorseId(), ACTIVE_STATUSES)) {
            throw new DuplicateResourceException("Ngựa '" + ngua.getTenNgua() + "' đã được đăng ký trong chặng đua này.");
        }
        if (dangKyThiDauRepository.existsByMaChangDuaAndMaNaiNguaAndTrangThaiIn(dto.getRaceId(), dto.getJockeyId(), ACTIVE_STATUSES)) {
            throw new DuplicateResourceException("Jockey '" + naiNgua.getHoTen() + "' đã được đăng ký trong chặng đua này.");
        }
        // Từ chối/Đã hủy giải phóng slot, không tính vào số lượng tối đa.
        if (schedule.getSoNguaToiDa() != null
                && dangKyThiDauRepository.countByMaChangDuaAndTrangThaiNotIn(
                        dto.getRaceId(), FREED_SLOT_STATUSES) >= schedule.getSoNguaToiDa()) {
            throw new ResourceInUseException("Chặng đua '" + schedule.getTenChangDua() + "' đã đủ số lượng đăng ký tối đa.");
        }

        DangKyThiDau registration = DangKyThiDau.builder()
                .maDangKy(generateMaDangKy())
                .maChangDua(dto.getRaceId())
                .maNgua(dto.getHorseId())
                .maNaiNgua(dto.getJockeyId())
                .build();

        DangKyThiDau saved = dangKyThiDauRepository.save(registration);
        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_REGISTRATION", "Registration:" + saved.getMaDangKy(),
                "Đăng ký thi đấu: ngựa " + ngua.getTenNgua() + " - jockey " + naiNgua.getHoTen());

        String organizerMaTK = resolveOrganizerMaTK(schedule);
        if (organizerMaTK != null) {
            notificationService.notify(organizerMaTK,
                    "Đăng ký thi đấu mới chờ duyệt",
                    "Ngựa " + ngua.getTenNgua() + " (jockey " + naiNgua.getHoTen() + ") đã đăng ký thi đấu chặng đua '"
                            + schedule.getTenChangDua() + "', cần duyệt.",
                    NotificationType.SYSTEM, "REGISTRATION", saved.getMaDangKy());
        }

        return mapToResponseDTO(saved, schedule, ngua, naiNgua);
    }

    public RegistrationResponseDTO approveRegistration(String maDangKy, String staffId, String organizerScopeId) {
        return changeStatus(maDangKy, RegistrationStatus.APPROVED, null, staffId, organizerScopeId);
    }

    public RegistrationResponseDTO rejectRegistration(String maDangKy, String reason, String staffId, String organizerScopeId) {
        return changeStatus(maDangKy, RegistrationStatus.REJECTED, reason, staffId, organizerScopeId);
    }

    private RegistrationResponseDTO changeStatus(String maDangKy, RegistrationStatus status, String reason, String staffId, String organizerScopeId) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(maDangKy)
                .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "id", maDangKy));
        assertRegistrationScope(registration, organizerScopeId);

        // Lỗi 5: chỉ duyệt/từ chối được khi cuộc đua liên quan còn ở trạng thái Mở đăng ký - đã bắt đầu
        // đua hoặc đã hoàn thành thì không còn ý nghĩa (danh sách thi đấu/làn đua đã chốt).
        Schedule schedule = scheduleRepository.findById(registration.getMaChangDua()).orElse(null);
        if (schedule != null) {
            RaceStatus raceStatus = StatusMapper.toRaceStatus(schedule.getTrangThai());
            if (raceStatus != RaceStatus.OPEN) {
                throw new ResourceInUseException(
                        "Không thể duyệt/từ chối đăng ký vì chặng đua '" + schedule.getTenChangDua()
                                + "' đã ở trạng thái " + raceStatus + " - chỉ được xử lý khi cuộc đua còn Mở đăng ký.");
            }
        }

        registration.setTrangThai(status == RegistrationStatus.APPROVED
                ? DangKyThiDau.TRANG_THAI_DA_DUYET : DangKyThiDau.TRANG_THAI_TU_CHOI);
        if (reason != null) {
            registration.setLyDo(reason);
        }
        DangKyThiDau updated = dangKyThiDauRepository.save(registration);

        nhatKyHoatDongService.writeAuditLog(staffId,
                status == RegistrationStatus.APPROVED ? "APPROVE_REGISTRATION" : "REJECT_REGISTRATION",
                "Registration:" + maDangKy, "Cập nhật trạng thái đăng ký -> " + status);

        RegistrationResponseDTO responseDTO = mapToFullResponseDTO(updated);
        boolean approved = status == RegistrationStatus.APPROVED;
        notificationService.notify(responseDTO.getOwnerId(),
                approved ? "Đăng ký thi đấu được duyệt" : "Đăng ký thi đấu bị từ chối",
                "Đăng ký cho ngựa " + responseDTO.getHorseName() + " tại chặng đua " + responseDTO.getRaceName()
                        + (approved ? " đã được duyệt." : " đã bị từ chối" + (reason != null ? ": " + reason : ".")),
                approved ? NotificationType.APPROVAL : NotificationType.REJECTION,
                "REGISTRATION", maDangKy);

        return responseDTO;
    }

    /**
     * Chủ ngựa tự hủy đăng ký của mình khi còn PENDING hoặc APPROVED và cuộc đua chưa diễn ra.
     * Bắt buộc nhập lý do; đăng ký chuyển sang CANCELLED (không xóa) để giữ lịch sử, Ban tổ chức phụ
     * trách race đó nhận thông báo.
     */
    public void cancelRegistration(String maDangKy, String reason, String ownerMaTK, String staffId) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy đăng ký.");
        }

        DangKyThiDau registration = dangKyThiDauRepository.findById(maDangKy)
                .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "id", maDangKy));

        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));
        Ngua ngua = nguaRepository.findById(registration.getMaNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "horseId", registration.getMaNgua()));
        if (!chuNgua.getMaChuNgua().equals(ngua.getMaChuNgua())) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền hủy đăng ký này");
        }

        RegistrationStatus status = StatusMapper.toRegistrationStatus(registration.getTrangThai());
        if (status != RegistrationStatus.PENDING && status != RegistrationStatus.APPROVED) {
            throw new ResourceInUseException("Chỉ có thể hủy đăng ký đang chờ duyệt hoặc đã được duyệt.");
        }

        Schedule schedule = scheduleRepository.findById(registration.getMaChangDua()).orElse(null);
        if (schedule != null && (Schedule.TRANG_THAI_DANG_DUA.equals(schedule.getTrangThai())
                || Schedule.TRANG_THAI_HOAN_THANH.equals(schedule.getTrangThai())
                || Schedule.TRANG_THAI_DA_HUY.equals(schedule.getTrangThai()))) {
            throw new ResourceInUseException("Cuộc đua đã diễn ra hoặc kết thúc, không thể hủy đăng ký.");
        }

        registration.setTrangThai(DangKyThiDau.TRANG_THAI_DA_HUY);
        registration.setLyDo(reason);
        dangKyThiDauRepository.save(registration);

        nhatKyHoatDongService.writeAuditLog(staffId, "CANCEL_REGISTRATION", "Registration:" + maDangKy,
                "Chủ ngựa hủy đăng ký thi đấu ngựa " + ngua.getTenNgua() + " (Lý do: " + reason + ")");

        String organizerMaTK = schedule != null ? resolveOrganizerMaTK(schedule) : null;
        if (organizerMaTK != null) {
            notificationService.notify(organizerMaTK,
                    "Đăng ký thi đấu đã bị hủy",
                    "Chủ ngựa " + chuNgua.getHoTen() + " đã hủy đăng ký ngựa " + ngua.getTenNgua()
                            + " tại chặng đua " + schedule.getTenChangDua() + ". Lý do: " + reason,
                    NotificationType.SYSTEM, "REGISTRATION", maDangKy);
        }
    }

    /**
     * Ban tổ chức loại 1 đăng ký (trước/trong/sau khi race diễn ra) - không tính điểm, ẩn khỏi bảng xếp
     * hạng kể cả khi kết quả đã publish trước đó (RankingService lọc theo trạng thái đăng ký, tính động).
     * Lỗi 4: nếu đăng ký này ĐÃ có kết quả ở trạng thái "Đã công bố", loại đăng ký sẽ THU HỒI công bố
     * (chuyển Result về "Chưa công bố") để Ban tổ chức có thể sửa lại bảng kết quả rồi công bố lại -
     * chỉ đụng tới đúng 1 dòng Result của đăng ký này, không ảnh hưởng các đăng ký khác cùng race.
     * Bắt buộc xác nhận rõ ràng (confirmRevokePublish=true) trước khi thu hồi, tương tự cơ chế
     * confirmEditPublished ở ResultService.submitResults.
     */
    public RegistrationResponseDTO disqualifyRegistration(String maDangKy, String reason, boolean confirmRevokePublish,
                                                            String staffId, String organizerScopeId) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(maDangKy)
                .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "id", maDangKy));
        assertRegistrationScope(registration, organizerScopeId);

        RegistrationStatus status = StatusMapper.toRegistrationStatus(registration.getTrangThai());
        if (status != RegistrationStatus.APPROVED) {
            throw new ResourceInUseException("Chỉ có thể loại đăng ký đang ở trạng thái đã duyệt.");
        }

        Result result = resultRepository.findByMaChangDuaAndMaNgua(registration.getMaChangDua(), registration.getMaNgua())
                .orElse(null);
        boolean wasPublished = result != null && Result.TRANG_THAI_DA_CONG_BO.equals(result.getTrangThaiCongBo());
        if (wasPublished) {
            if (!confirmRevokePublish) {
                throw new ResourceInUseException(
                        "Đăng ký này đã có kết quả được công bố. Tiếp tục sẽ thu hồi công bố và yêu cầu công bố lại "
                                + "sau khi cập nhật. Xác nhận rõ ràng (confirmRevokePublish=true) nếu vẫn muốn tiếp tục.");
            }
            result.setTrangThaiCongBo(Result.TRANG_THAI_CHUA_CONG_BO);
            resultRepository.save(result);
        }

        registration.setTrangThai(DangKyThiDau.TRANG_THAI_BI_LOAI);
        registration.setLyDo(reason);
        DangKyThiDau updated = dangKyThiDauRepository.save(registration);

        nhatKyHoatDongService.writeAuditLog(staffId, "DISQUALIFY_REGISTRATION", "Registration:" + maDangKy,
                "Loại đăng ký thi đấu" + (reason != null && !reason.isBlank() ? " (Lý do: " + reason + ")" : "")
                        + (wasPublished ? " - đã thu hồi công bố kết quả liên quan" : ""));

        RegistrationResponseDTO responseDTO = mapToFullResponseDTO(updated);
        notificationService.notify(responseDTO.getOwnerId(),
                "Đăng ký thi đấu bị loại",
                "Đăng ký cho ngựa " + responseDTO.getHorseName() + " tại chặng đua " + responseDTO.getRaceName()
                        + " đã bị Ban tổ chức loại" + (reason != null && !reason.isBlank() ? ": " + reason : ".")
                        + " Kết quả/điểm của lần đăng ký này sẽ không được tính vào bảng xếp hạng.",
                NotificationType.REJECTION, "REGISTRATION", maDangKy);

        return responseDTO;
    }

    /** Ban tổ chức (maBTC) phụ trách race này, suy ra qua Race -> Season -> BanToChuc. */
    private String resolveOrganizerId(Schedule schedule) {
        return muaGiaiRepository.findById(schedule.getMaMuaGiai()).map(MuaGiai::getMaBTC).orElse(null);
    }

    /** Ban tổ chức (maTK tài khoản) phụ trách race này, suy ra qua Race -> Season -> BanToChuc. */
    private String resolveOrganizerMaTK(Schedule schedule) {
        String maBTC = resolveOrganizerId(schedule);
        return maBTC != null ? banToChucRepository.findById(maBTC).map(BanToChuc::getMaTK).orElse(null) : null;
    }

    /** Chặn Ban tổ chức A xem/duyệt/loại đăng ký thuộc race của Ban tổ chức B. ADMIN (organizerScopeId == null) xem được tất cả. */
    private void assertRegistrationScope(DangKyThiDau registration, String organizerScopeId) {
        if (organizerScopeId == null) {
            return;
        }
        Schedule schedule = scheduleRepository.findById(registration.getMaChangDua()).orElse(null);
        String organizerId = schedule != null ? resolveOrganizerId(schedule) : null;
        if (organizerId == null || !organizerScopeId.equals(organizerId)) {
            throw new AccessDeniedException("Bạn không có quyền thao tác trên đăng ký thi đấu của Ban tổ chức khác");
        }
    }

    /** Danh sách maChangDua thuộc các mùa giải của 1 Ban tổ chức - dùng để lọc đăng ký theo BTC. */
    private List<String> raceIdsOfOrganizer(String organizerScopeId) {
        List<String> seasonIds = muaGiaiRepository.findAll((root, query, cb) -> cb.equal(root.get("maBTC"), organizerScopeId))
                .stream().map(MuaGiai::getMaMuaGiai).collect(Collectors.toList());
        if (seasonIds.isEmpty()) {
            return List.of();
        }
        return scheduleRepository.findAll((root, query, cb) -> root.get("maMuaGiai").in(seasonIds))
                .stream().map(Schedule::getMaChangDua).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RegistrationResponseDTO getRegistrationById(String maDangKy, String organizerScopeId) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(maDangKy)
                .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "id", maDangKy));
        assertRegistrationScope(registration, organizerScopeId);
        return mapToFullResponseDTO(registration);
    }

    @Transactional(readOnly = true)
    public PageResponse<RegistrationResponseDTO> getAllRegistrations(Pageable pageable, String raceId, RegistrationStatus status, String organizerScopeId) {
        List<String> organizerRaceIds = organizerScopeId != null ? raceIdsOfOrganizer(organizerScopeId) : null;

        Specification<DangKyThiDau> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (raceId != null && !raceId.isBlank()) {
                predicates.add(cb.equal(root.get("maChangDua"), raceId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("trangThai"), toTrangThai(status)));
            }
            if (organizerRaceIds != null) {
                predicates.add(organizerRaceIds.isEmpty() ? cb.disjunction() : root.get("maChangDua").in(organizerRaceIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(dangKyThiDauRepository.findAll(spec, pageable), this::mapToFullResponseDTO);
    }

    @Transactional(readOnly = true)
    public PageResponse<RegistrationResponseDTO> getMyRegistrations(String ownerMaTK, Pageable pageable) {
        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));
        List<String> horseIds = nguaRepository.findByMaChuNgua(chuNgua.getMaChuNgua()).stream()
                .map(Ngua::getMaNgua).collect(Collectors.toList());

        if (horseIds.isEmpty()) {
            return PageResponse.<RegistrationResponseDTO>builder()
                    .content(List.of()).page(pageable.getPageNumber()).size(pageable.getPageSize())
                    .totalElements(0).totalPages(0).last(true).build();
        }

        Specification<DangKyThiDau> spec = (root, query, cb) -> root.get("maNgua").in(horseIds);
        return PageResponse.of(dangKyThiDauRepository.findAll(spec, pageable), this::mapToFullResponseDTO);
    }

    // ----- Lane assignment (dùng chung entity DangKyThiDau) -----

    public LaneResponseDTO assignLane(LaneRequestDTO dto, String staffId) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(dto.getRegistrationId())
                .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "id", dto.getRegistrationId()));

        assertRaceOpenForLaneEdit(registration.getMaChangDua());
        assertLaneNotTaken(registration.getMaChangDua(), dto.getLaneNumber(), registration.getMaDangKy());
        registration.setSoLan(dto.getLaneNumber());
        registration.setNgayGanLan(LocalDateTime.now());
        DangKyThiDau updated = dangKyThiDauRepository.save(registration);

        nhatKyHoatDongService.writeAuditLog(staffId, "ASSIGN_LANE", "Registration:" + updated.getMaDangKy(),
                "Gán làn đua số " + dto.getLaneNumber());

        return mapToLaneResponseDTO(updated);
    }

    public LaneResponseDTO updateLane(String maDangKy, LaneRequestDTO dto, String staffId) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(maDangKy)
                .orElseThrow(() -> new ResourceNotFoundException("Làn đua", "id", maDangKy));

        assertRaceOpenForLaneEdit(registration.getMaChangDua());
        assertLaneNotTaken(registration.getMaChangDua(), dto.getLaneNumber(), maDangKy);
        registration.setSoLan(dto.getLaneNumber());
        registration.setNgayGanLan(LocalDateTime.now());
        DangKyThiDau updated = dangKyThiDauRepository.save(registration);

        nhatKyHoatDongService.writeAuditLog(staffId, "UPDATE_LANE", "Registration:" + maDangKy,
                "Cập nhật làn đua số " + dto.getLaneNumber());

        return mapToLaneResponseDTO(updated);
    }

    public void removeLane(String maDangKy, String staffId) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(maDangKy)
                .orElseThrow(() -> new ResourceNotFoundException("Làn đua", "id", maDangKy));

        assertRaceOpenForLaneEdit(registration.getMaChangDua());
        registration.setSoLan(null);
        registration.setNgayGanLan(null);
        dangKyThiDauRepository.save(registration);

        nhatKyHoatDongService.writeAuditLog(staffId, "REMOVE_LANE", "Registration:" + maDangKy, "Bỏ gán làn đua");
    }

    @Transactional(readOnly = true)
    public List<LaneResponseDTO> getLanesByRace(String maChangDua) {
        return dangKyThiDauRepository.findByMaChangDua(maChangDua).stream()
                .filter(r -> r.getSoLan() != null)
                .map(this::mapToLaneResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<RegistrationResponseDTO> getRegistrationsByRace(String maChangDua, Pageable pageable, String organizerScopeId) {
        if (organizerScopeId != null) {
            Schedule schedule = scheduleRepository.findById(maChangDua).orElse(null);
            String organizerId = schedule != null ? resolveOrganizerId(schedule) : null;
            if (organizerId == null || !organizerScopeId.equals(organizerId)) {
                throw new AccessDeniedException("Bạn không có quyền truy cập chặng đua của Ban tổ chức khác");
            }
        }
        return PageResponse.of(dangKyThiDauRepository.findByMaChangDua(maChangDua, pageable), this::mapToFullResponseDTO);
    }

    /** Lỗi 3: không cho gán/sửa/bỏ làn đua khi chặng đua đã bắt đầu đua (ONGOING) hoặc đã hoàn thành. */
    private void assertRaceOpenForLaneEdit(String maChangDua) {
        Schedule schedule = scheduleRepository.findById(maChangDua).orElse(null);
        if (schedule == null) {
            return;
        }
        RaceStatus raceStatus = StatusMapper.toRaceStatus(schedule.getTrangThai());
        if (raceStatus != RaceStatus.OPEN) {
            throw new ResourceInUseException(
                    "Không thể sửa làn đua vì chặng đua '" + schedule.getTenChangDua() + "' đã ở trạng thái "
                            + raceStatus + " - chỉ được phân làn khi cuộc đua còn Mở đăng ký.");
        }
    }

    /** Mỗi làn đua chỉ được gán cho 1 ngựa trong cùng 1 chặng đua. */
    private void assertLaneNotTaken(String maChangDua, Integer laneNumber, String excludeMaDangKy) {
        if (dangKyThiDauRepository.existsByMaChangDuaAndSoLanAndMaDangKyNot(maChangDua, laneNumber, excludeMaDangKy)) {
            throw new DuplicateResourceException("Làn đua số " + laneNumber + " đã được gán cho ngựa khác trong chặng đua này.");
        }
    }

    // ----- Helper -----

    private RegistrationResponseDTO mapToFullResponseDTO(DangKyThiDau registration) {
        Schedule schedule = scheduleRepository.findById(registration.getMaChangDua()).orElse(null);
        Ngua ngua = nguaRepository.findById(registration.getMaNgua()).orElse(null);
        NaiNgua naiNgua = naiNguaRepository.findById(registration.getMaNaiNgua()).orElse(null);
        return mapToResponseDTO(registration, schedule, ngua, naiNgua);
    }

    private RegistrationResponseDTO mapToResponseDTO(DangKyThiDau registration, Schedule schedule, Ngua ngua, NaiNgua naiNgua) {
        ChuNgua chuNgua = ngua != null ? chuNguaRepository.findById(ngua.getMaChuNgua()).orElse(null) : null;
        LocalDateTime raceDateTime = (schedule != null && schedule.getNgayThiDau() != null)
                ? LocalDateTime.of(schedule.getNgayThiDau(), schedule.getGioBatDau() != null ? schedule.getGioBatDau() : LocalTime.MIDNIGHT)
                : null;

        return RegistrationResponseDTO.builder()
                .id(registration.getMaDangKy())
                .raceId(registration.getMaChangDua())
                .raceName(schedule != null ? schedule.getTenChangDua() : null)
                .raceDate(raceDateTime)
                .horseId(registration.getMaNgua())
                .horseName(ngua != null ? ngua.getTenNgua() : null)
                .horseCode(registration.getMaNgua())
                .jockeyId(registration.getMaNaiNgua())
                .jockeyName(naiNgua != null ? naiNgua.getHoTen() : null)
                .ownerId(chuNgua != null ? chuNgua.getMaTK() : null)
                .ownerName(chuNgua != null ? chuNgua.getHoTen() : null)
                .laneNumber(registration.getSoLan())
                .status(StatusMapper.toRegistrationStatus(registration.getTrangThai()))
                .reason(registration.getLyDo())
                .registeredAt(registration.getNgayDangKy())
                .build();
    }

    private LaneResponseDTO mapToLaneResponseDTO(DangKyThiDau registration) {
        Ngua ngua = nguaRepository.findById(registration.getMaNgua()).orElse(null);
        NaiNgua naiNgua = naiNguaRepository.findById(registration.getMaNaiNgua()).orElse(null);
        return LaneResponseDTO.builder()
                .id(registration.getMaDangKy())
                .raceId(registration.getMaChangDua())
                .registrationId(registration.getMaDangKy())
                .horseName(ngua != null ? ngua.getTenNgua() : null)
                .jockeyName(naiNgua != null ? naiNgua.getHoTen() : null)
                .laneNumber(registration.getSoLan())
                .assignedAt(registration.getNgayGanLan())
                .build();
    }

    private String toTrangThai(RegistrationStatus status) {
        return StatusMapper.toTrangThaiDangKy(status);
    }

    private String generateMaDangKy() {
        return "DK" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
