package com.horseracing.service;

import com.horseracing.dto.upload.FileUploadResponseDTO;
import com.horseracing.entity.TepTin;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.TepTinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * UploadService - lưu file upload (ảnh ngựa/jockey, hồ sơ sức khỏe...) ra đĩa local.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UploadService {

    private final TepTinRepository tepTinRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public FileUploadResponseDTO upload(MultipartFile file, String fileType, String targetType, String targetId) {
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String maTepTin = "FT" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
            String storedName = maTepTin + ext;
            Path destination = dir.resolve(storedName);
            file.transferTo(destination);

            TepTin tepTin = TepTin.builder()
                    .maTepTin(maTepTin)
                    .tenFile(originalName)
                    .duongDan(destination.toString())
                    .loaiFile(fileType)
                    .loaiDoiTuong(targetType)
                    .maDoiTuong(targetId)
                    .kichThuoc(file.getSize())
                    .build();
            tepTinRepository.save(tepTin);

            return FileUploadResponseDTO.builder()
                    .fileId(maTepTin)
                    .url("/upload/" + maTepTin)
                    .fileName(originalName)
                    .fileType(fileType)
                    .fileCategory(fileType)
                    .fileSize(file.getSize())
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException("Không thể lưu file: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public TepTin getFileMeta(String maTepTin) {
        return tepTinRepository.findById(maTepTin)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", maTepTin));
    }

    @Transactional(readOnly = true)
    public byte[] readFileBytes(TepTin tepTin) {
        try {
            return Files.readAllBytes(Paths.get(tepTin.getDuongDan()));
        } catch (IOException e) {
            throw new UncheckedIOException("Không thể đọc file: " + e.getMessage(), e);
        }
    }
}
