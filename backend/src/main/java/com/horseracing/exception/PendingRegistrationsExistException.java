package com.horseracing.exception;

import lombok.Getter;

/**
 * Ném ra khi cố chuyển 1 cuộc đua sang "Đang đua" (ONGOING) nhưng vẫn còn đăng ký ở trạng thái
 * "Chờ duyệt" (PENDING) - phải duyệt/từ chối hết trước, không tự động reject thay Ban tổ chức.
 * Mang kèm pendingCount để Frontend hiển thị số lượng chính xác nếu cần.
 */
@Getter
public class PendingRegistrationsExistException extends RuntimeException {

    private final long pendingCount;

    public PendingRegistrationsExistException(String message, long pendingCount) {
        super(message);
        this.pendingCount = pendingCount;
    }
}
