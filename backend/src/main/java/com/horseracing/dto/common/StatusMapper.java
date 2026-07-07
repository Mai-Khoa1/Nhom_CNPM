package com.horseracing.dto.common;

/**
 * Chuyển đổi 2 chiều giữa các giá trị trạng thái/giới tính tiếng Việt lưu trong DB
 * và các enum tiếng Anh dùng ở tầng API (khớp frontend).
 */
public final class StatusMapper {

    private StatusMapper() {
    }

    // ----- Gender (Ngua.gioiTinh: Đực/Cái) -----

    public static Gender toGender(String gioiTinh) {
        if (gioiTinh == null) return null;
        return switch (gioiTinh) {
            case "Đực" -> Gender.MALE;
            case "Cái" -> Gender.FEMALE;
            default -> throw new IllegalArgumentException("Giới tính không hợp lệ: " + gioiTinh);
        };
    }

    public static String toGioiTinh(Gender gender) {
        if (gender == null) return null;
        return switch (gender) {
            case MALE -> "Đực";
            case FEMALE -> "Cái";
        };
    }

    // ----- RaceStatus (ChangDua.trangThai) -----

    public static RaceStatus toRaceStatus(String trangThai) {
        if (trangThai == null) return RaceStatus.OPEN;
        return switch (trangThai) {
            case "Mở đăng ký" -> RaceStatus.OPEN;
            case "Đã đóng đăng ký" -> RaceStatus.CLOSED;
            case "Đang đua" -> RaceStatus.ONGOING;
            case "Hoàn thành" -> RaceStatus.COMPLETED;
            case "Đã hủy" -> RaceStatus.CANCELLED;
            default -> throw new IllegalArgumentException("Trạng thái chặng đua không hợp lệ: " + trangThai);
        };
    }

    public static String toTrangThaiChangDua(RaceStatus status) {
        if (status == null) return "Mở đăng ký";
        return switch (status) {
            case OPEN -> "Mở đăng ký";
            case CLOSED -> "Đã đóng đăng ký";
            case ONGOING -> "Đang đua";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
        };
    }

    // ----- RegistrationStatus (DangKyThiDau.trangThai) -----

    public static RegistrationStatus toRegistrationStatus(String trangThai) {
        if (trangThai == null) return RegistrationStatus.PENDING;
        return switch (trangThai) {
            case "Chờ duyệt" -> RegistrationStatus.PENDING;
            case "Đã duyệt" -> RegistrationStatus.APPROVED;
            case "Từ chối" -> RegistrationStatus.REJECTED;
            case "Đã hủy" -> RegistrationStatus.CANCELLED;
            case "Bị loại" -> RegistrationStatus.DISQUALIFIED;
            default -> throw new IllegalArgumentException("Trạng thái đăng ký không hợp lệ: " + trangThai);
        };
    }

    public static String toTrangThaiDangKy(RegistrationStatus status) {
        if (status == null) return "Chờ duyệt";
        return switch (status) {
            case PENDING -> "Chờ duyệt";
            case APPROVED -> "Đã duyệt";
            case REJECTED -> "Từ chối";
            case CANCELLED -> "Đã hủy";
            case DISQUALIFIED -> "Bị loại";
        };
    }

    // ----- SeasonStatus (MuaGiai.trangThai) -----

    public static SeasonStatus toSeasonStatus(String trangThai) {
        if (trangThai == null) return SeasonStatus.UPCOMING;
        return switch (trangThai) {
            case "Mở đăng ký" -> SeasonStatus.UPCOMING;
            case "Đang diễn ra" -> SeasonStatus.ONGOING;
            case "Đã kết thúc", "Đã hủy" -> SeasonStatus.CLOSED;
            default -> throw new IllegalArgumentException("Trạng thái mùa giải không hợp lệ: " + trangThai);
        };
    }

    public static String toTrangThaiMuaGiai(SeasonStatus status) {
        if (status == null) return "Mở đăng ký";
        return switch (status) {
            case UPCOMING -> "Mở đăng ký";
            case ONGOING -> "Đang diễn ra";
            case CLOSED -> "Đã kết thúc";
        };
    }

    // ----- UpdateRequestStatus (YeuCauCapNhat.trangThai) -----

    public static UpdateRequestStatus toUpdateRequestStatus(String trangThai) {
        if (trangThai == null) return UpdateRequestStatus.PENDING;
        return switch (trangThai) {
            case "Chờ duyệt" -> UpdateRequestStatus.PENDING;
            case "Đã duyệt" -> UpdateRequestStatus.APPROVED;
            case "Từ chối" -> UpdateRequestStatus.REJECTED;
            default -> throw new IllegalArgumentException("Trạng thái yêu cầu cập nhật không hợp lệ: " + trangThai);
        };
    }

    // ----- NotificationStatus (ThongBao.trangThai: Chưa đọc/Đã đọc) -----

    public static boolean isRead(String trangThai) {
        return "Đã đọc".equals(trangThai);
    }

    public static String toTrangThaiThongBao(boolean isRead) {
        return isRead ? "Đã đọc" : "Chưa đọc";
    }
}
