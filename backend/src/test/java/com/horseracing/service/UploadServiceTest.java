package com.horseracing.service;

import com.horseracing.repository.TepTinRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

/**
 * Kiểm tra M6: chặn file quá 5MB và chặn định dạng không phải ảnh/PDF ở tầng service
 * (không phụ thuộc vào việc FE có validate hay không).
 */
@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock private TepTinRepository tepTinRepository;

    @InjectMocks private UploadService uploadService;

    @Test
    void upload_rejectsFileLargerThan5MB() {
        byte[] content = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", content);

        assertThatThrownBy(() -> uploadService.upload(file, "HORSE_PHOTO", "HORSE", "N1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5MB");

        verify(tepTinRepository, never()).save(any());
    }

    @Test
    void upload_rejectsUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile("file", "script.exe", "application/x-msdownload", "content".getBytes());

        assertThatThrownBy(() -> uploadService.upload(file, "HORSE_PHOTO", "HORSE", "N1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Định dạng file không được hỗ trợ");

        verify(tepTinRepository, never()).save(any());
    }

    @Test
    void upload_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> uploadService.upload(file, "HORSE_PHOTO", "HORSE", "N1"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
