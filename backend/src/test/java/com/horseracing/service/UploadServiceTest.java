package com.horseracing.service;

import com.horseracing.dto.upload.TepTinRequestDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.NaiNguaRepository;
import com.horseracing.repository.NguaRepository;
import com.horseracing.repository.TepTinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Kiểm tra M6: chặn file quá 5MB và chặn định dạng không phải ảnh/PDF ở tầng service
 * (không phụ thuộc vào việc FE có validate hay không).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UploadServiceTest {

    @Mock private TepTinRepository tepTinRepository;
    @Mock private ChuNguaRepository chuNguaRepository;
    @Mock private NguaRepository nguaRepository;
    @Mock private NaiNguaRepository naiNguaRepository;
    @Mock private NotificationService notificationService;
    @Mock private NhatKyHoatDongService nhatKyHoatDongService;
    @Mock private UpdateRequestService updateRequestService;

    @InjectMocks private UploadService uploadService;

    private static final String OWNER_MA_TK = "TK1";

    @BeforeEach
    void setUp() {
        ChuNgua chuNgua = ChuNgua.builder().maChuNgua("CN1").maTK(OWNER_MA_TK).hoTen("Chủ ngựa test").build();
        lenient().when(chuNguaRepository.findByMaTK(OWNER_MA_TK)).thenReturn(Optional.of(chuNgua));
    }

    private TepTinRequestDTO dto() {
        // targetType/targetId để trống - test này chỉ tập trung kiểm tra validate file (kích thước/định dạng),
        // không liên quan tới việc gắn tệp vào ngựa/nài ngựa nào (xem assertTargetOwnership).
        return TepTinRequestDTO.builder().tenFile("test").loaiFile("HORSE_PHOTO").build();
    }

    @Test
    void upload_rejectsFileLargerThan5MB() {
        byte[] content = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", content);

        assertThatThrownBy(() -> uploadService.createFile(file, dto(), OWNER_MA_TK, OWNER_MA_TK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5MB");

        verify(tepTinRepository, never()).save(any());
    }

    @Test
    void upload_rejectsUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile("file", "script.exe", "application/x-msdownload", "content".getBytes());

        assertThatThrownBy(() -> uploadService.createFile(file, dto(), OWNER_MA_TK, OWNER_MA_TK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Định dạng file không được hỗ trợ");

        verify(tepTinRepository, never()).save(any());
    }

    @Test
    void upload_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> uploadService.createFile(file, dto(), OWNER_MA_TK, OWNER_MA_TK))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
