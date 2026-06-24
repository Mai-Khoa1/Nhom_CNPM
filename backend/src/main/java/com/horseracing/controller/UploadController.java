package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.upload.FileUploadResponseDTO;
import com.horseracing.entity.TepTin;
import com.horseracing.service.UploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * UploadController - tải lên và tải xuống file. Base path /upload khớp frontend.
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Tag(name = "Tải file", description = "API upload/download file đính kèm")
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponseDTO>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId) {
        FileUploadResponseDTO result = uploadService.upload(file, fileType, targetType, targetId);
        return ResponseEntity.ok(ApiResponse.success(result, "Tải file lên thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        TepTin tepTin = uploadService.getFileMeta(id);
        byte[] content = uploadService.readFileBytes(tepTin);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + tepTin.getTenFile() + "\"")
                .body(content);
    }
}
