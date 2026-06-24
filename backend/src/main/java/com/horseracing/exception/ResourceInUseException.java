package com.horseracing.exception;

/**
 * Ném ra khi không thể xóa/sửa resource vì đang được tham chiếu/sử dụng ở nơi khác.
 */
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }
}
