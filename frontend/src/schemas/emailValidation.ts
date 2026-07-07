import { z } from 'zod';

const LOCAL_PART = /^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+$/;
const DOMAIN_LABEL = /^[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$/;

/**
 * Danh sách các TLD phổ biến, thực sự tồn tại. Dùng để loại các domain "giả"
 * kiểu aaaahn@jshhs.ncbbc hoặc abc@xxx.xxxxx (đúng cấu trúc nhưng TLD không có thật).
 */
const COMMON_TLDS = new Set([
  'com', 'org', 'net', 'edu', 'gov', 'mil', 'int', 'info', 'biz', 'name', 'pro',
  'io', 'co', 'me', 'tv', 'cc', 'ai', 'app', 'dev', 'xyz', 'online', 'site', 'tech', 'store', 'shop', 'blog',
  'vn', 'us', 'uk', 'ca', 'au', 'de', 'fr', 'jp', 'cn', 'kr', 'in', 'ru', 'br', 'es', 'it', 'nl', 'se', 'no',
  'dk', 'fi', 'pl', 'ch', 'at', 'be', 'nz', 'sg', 'hk', 'tw', 'th', 'id', 'my', 'ph',
  'asia', 'mobi', 'travel', 'jobs', 'museum', 'coop', 'cat',
]);

/**
 * Trả về thông báo lỗi cụ thể theo từng nguyên nhân sai định dạng email
 * (thiếu @, nhiều @, tên miền thiếu dấu chấm, TLD không tồn tại...), hoặc null nếu hợp lệ.
 */
export function findEmailFormatError(email: string): string | null {
  const atCount = (email.match(/@/g) ?? []).length;
  if (atCount === 0) return 'Email không hợp lệ: thiếu ký tự @';
  if (atCount > 1) return 'Email không hợp lệ: chỉ được chứa 1 ký tự @';

  const [localPart, domainPart] = email.split('@');
  if (!localPart) return 'Email không hợp lệ: thiếu phần trước ký tự @';
  if (!LOCAL_PART.test(localPart)) return 'Email không hợp lệ: phần trước @ chứa ký tự không hợp lệ';
  if (!domainPart || !domainPart.includes('.')) {
    return 'Email không hợp lệ: tên miền thiếu dấu chấm (VD: ten@gmail.com)';
  }

  const labels = domainPart.split('.');
  for (let i = 0; i < labels.length; i++) {
    const label = labels[i];
    if (!label) return 'Email không hợp lệ: tên miền không được có dấu chấm liên tiếp hoặc ở đầu/cuối';
    const isLastLabel = i === labels.length - 1;
    const valid = isLastLabel ? COMMON_TLDS.has(label.toLowerCase()) : DOMAIN_LABEL.test(label);
    if (!valid) return isLastLabel
      ? 'Email không hợp lệ: tên miền (TLD) không tồn tại'
      : 'Email không hợp lệ: định dạng tên miền không đúng';
  }
  return null;
}

export const emailField = z.string().superRefine((email, ctx) => {
  const error = findEmailFormatError(email);
  if (error) {
    ctx.addIssue({ code: z.ZodIssueCode.custom, message: error });
  }
});
