package com.horseracing.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Kiểm tra M1: email sai định dạng phải bị từ chối (thiếu @, tên miền thiếu dấu chấm,
 * TLD không tồn tại trong danh sách phổ biến...).
 */
class EmailFormatValidatorTest {

    private final EmailFormatValidator validator = new EmailFormatValidator();

    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        context = mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);
    }

    @Test
    void rejectsEmailMissingAtSign() {
        assertThat(validator.isValid("abcdef", context)).isFalse();
    }

    @Test
    void rejectsEmailWithMultipleAtSigns() {
        assertThat(validator.isValid("a@b@gmail.com", context)).isFalse();
    }

    @Test
    void rejectsDomainMissingDot() {
        assertThat(validator.isValid("user@localhost", context)).isFalse();
    }

    @Test
    void rejectsUnknownTld() {
        // Đúng cấu trúc (label.label) nhưng TLD không có thật -> vẫn bị từ chối.
        assertThat(validator.isValid("aaaahn@jshhs.ncbbc", context)).isFalse();
        assertThat(validator.isValid("abc@xxx.xxxxx", context)).isFalse();
    }

    @Test
    void acceptsCommonEmailFormats() {
        assertThat(validator.isValid("owner@gmail.com", context)).isTrue();
        assertThat(validator.isValid("ten.nguoidung@example.co.uk", context)).isTrue();
    }

    @Test
    void treatsBlankAsValid_letsNotBlankHandleIt() {
        assertThat(validator.isValid("", context)).isTrue();
        assertThat(validator.isValid(null, context)).isTrue();
    }
}
