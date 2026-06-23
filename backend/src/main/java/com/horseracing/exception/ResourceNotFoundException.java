package com.horseracing.exception;

/**
 * Ném ra khi không tìm thấy resource theo id/mã.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s không tồn tại với %s = '%s'", resourceName, fieldName, fieldValue));
    }
}
