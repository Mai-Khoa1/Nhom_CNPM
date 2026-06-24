package com.horseracing.exception;

/**
 * Ném ra khi dữ liệu bị trùng lặp (vi phạm unique: username, email, số giấy phép...).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
