import { z } from 'zod';

const LOCAL_PART = /^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+$/;
const DOMAIN_LABEL = /^[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$/;
const TLD = /^[A-Za-z]{2,}$/;

/**
 * Trả về thông báo lỗi cụ thể theo từng nguyên nhân sai định dạng email
 * (thiếu @, nhiều @, tên miền thiếu dấu chấm...), hoặc null nếu hợp lệ.
 * Không yêu cầu tên miền phải khớp danh sách TLD cụ thể (.com/.vn...), chỉ yêu cầu đúng cấu trúc email.
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
    const valid = isLastLabel ? TLD.test(label) : DOMAIN_LABEL.test(label);
    if (!valid) return 'Email không hợp lệ: định dạng tên miền không đúng';
  }
  return null;
}

export const emailField = z.string().superRefine((email, ctx) => {
  const error = findEmailFormatError(email);
  if (error) {
    ctx.addIssue({ code: z.ZodIssueCode.custom, message: error });
  }
});
