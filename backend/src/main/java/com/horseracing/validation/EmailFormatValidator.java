package com.horseracing.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Kiểm tra định dạng email và trả về thông báo lỗi cụ thể theo từng nguyên nhân sai
 * (thiếu @, nhiều @, phần trước @ rỗng, tên miền thiếu dấu chấm/ký tự không hợp lệ...).
 * Không yêu cầu tên miền phải khớp danh sách TLD cụ thể (.com/.vn...), chỉ yêu cầu đúng cấu trúc email.
 */
public class EmailFormatValidator implements ConstraintValidator<ValidEmailFormat, String> {

    private static final Pattern LOCAL_PART = Pattern.compile("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+$");
    private static final Pattern DOMAIN_LABEL = Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$");
    private static final Pattern TLD = Pattern.compile("^[A-Za-z]{2,}$");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true; // để @NotBlank xử lý trường hợp rỗng
        }

        String message = findErrorMessage(email);
        if (message == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }

    private String findErrorMessage(String email) {
        long atCount = email.chars().filter(c -> c == '@').count();
        if (atCount == 0) {
            return "Email không hợp lệ: thiếu ký tự @";
        }
        if (atCount > 1) {
            return "Email không hợp lệ: chỉ được chứa 1 ký tự @";
        }

        String[] parts = email.split("@", -1);
        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.isEmpty()) {
            return "Email không hợp lệ: thiếu phần trước ký tự @";
        }
        if (!LOCAL_PART.matcher(localPart).matches()) {
            return "Email không hợp lệ: phần trước @ chứa ký tự không hợp lệ";
        }
        if (domainPart.isEmpty() || !domainPart.contains(".")) {
            return "Email không hợp lệ: tên miền thiếu dấu chấm (VD: ten@gmail.com)";
        }

        String[] labels = domainPart.split("\\.", -1);
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            if (label.isEmpty()) {
                return "Email không hợp lệ: tên miền không được có dấu chấm liên tiếp hoặc ở đầu/cuối";
            }
            boolean isLastLabel = i == labels.length - 1;
            if (isLastLabel ? !TLD.matcher(label).matches() : !DOMAIN_LABEL.matcher(label).matches()) {
                return "Email không hợp lệ: định dạng tên miền không đúng";
            }
        }
        return null;
    }
}
