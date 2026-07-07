package com.horseracing.dto.common;

public enum RegistrationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    /** Chủ ngựa tự hủy đăng ký đã APPROVED (có lý do), Ban tổ chức nhận thông báo - dữ liệu vẫn được giữ lại. */
    CANCELLED,
    /** Ban tổ chức loại đăng ký (trước/trong/sau khi race diễn ra) - không tính điểm, ẩn khỏi bảng xếp hạng. */
    DISQUALIFIED
}
