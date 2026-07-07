package com.horseracing.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Kiểm tra định dạng email và trả về thông báo lỗi cụ thể theo từng nguyên nhân sai
 * (thiếu @, nhiều @, phần trước @ rỗng, tên miền thiếu dấu chấm/ký tự không hợp lệ, TLD không tồn tại...).
 */
public class EmailFormatValidator implements ConstraintValidator<ValidEmailFormat, String> {

    private static final Pattern LOCAL_PART = Pattern.compile("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+$");
    private static final Pattern DOMAIN_LABEL = Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$");

    /** Danh sách TLD phổ biến, thực sự tồn tại - loại domain "giả" kiểu jshhs.ncbbc hoặc xxx.xxxxx. */
    private static final Set<String> COMMON_TLDS = Set.of(
            "com", "org", "net", "edu", "gov", "mil", "int", "info", "biz", "name", "pro",
            "io", "co", "me", "tv", "cc", "ai", "app", "dev", "xyz", "online", "site", "tech", "store", "shop", "blog",
            "vn", "us", "uk", "ca", "au", "de", "fr", "jp", "cn", "kr", "in", "ru", "br", "es", "it", "nl", "se", "no",
            "dk", "fi", "pl", "ch", "at", "be", "nz", "sg", "hk", "tw", "th", "id", "my", "ph",
            "asia", "mobi", "travel", "jobs", "museum", "coop", "cat");

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
            if (isLastLabel) {
                if (!COMMON_TLDS.contains(label.toLowerCase())) {
                    return "Email không hợp lệ: tên miền (TLD) không tồn tại";
                }
            } else if (!DOMAIN_LABEL.matcher(label).matches()) {
                return "Email không hợp lệ: định dạng tên miền không đúng";
            }
        }
        return null;
    }
}
