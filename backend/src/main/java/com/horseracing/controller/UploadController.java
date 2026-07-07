package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.upload.FileUploadResponseDTO;
import com.horseracing.dto.upload.TepTinRequestDTO;
import com.horseracing.entity.TepTin;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.UploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * UploadController - Chủ ngựa (HORSE_OWNER) quản lý tệp tin của chính mình; Ban tổ chức (ORGANIZER)
 * duyệt/từ chối tệp mới tải lên. Base path /upload khớp frontend. Admin không có quyền trên API này
 * (xem SecurityConfig - /upload/** chỉ cho HORSE_OWNER và ORGANIZER theo từng method).
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Tag(name = "Tải file", description = "API quản lý tệp tin của chủ ngựa, kèm luồng duyệt PENDING/APPROVED/REJECTED")
public class UploadController {

    private final UploadService uploadService;
    private final CurrentUserService currentUserService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponseDTO>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            Authentication authentication) {
        String ownerMaTK = currentUserService.resolveMaTK(authentication);
        TepTinRequestDTO dto = TepTinRequestDTO.builder()
                .tenFile(fileName)
                .loaiFile(fileType)
                .targetType(targetType)
                .targetId(targetId)
                .build();
        FileUploadResponseDTO result = uploadService.createFile(file, dto, ownerMaTK, ownerMaTK);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Tải file lên thành công, đang chờ duyệt"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FileUploadResponseDTO>>> listFiles(
            Pageable pageable,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            Authentication authentication) {
        String effectiveOwnerId = null;
        if (currentUserService.hasRole(authentication, "HORSE_OWNER")) {
            // Chủ ngựa chỉ được xem tệp tin của chính mình.
            effectiveOwnerId = currentUserService.resolveMaTK(authentication);
        }
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                uploadService.listFiles(pageable, fileType, targetType, targetId, effectiveOwnerId, organizerScopeId)));
    }

    @GetMapping("/{id}/meta")
    public ResponseEntity<ApiResponse<FileUploadResponseDTO>> getMeta(@PathVariable String id, Authentication authentication) {
        String requesterMaTK = currentUserService.resolveMaTK(authentication);
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(uploadService.getFileById(id, requesterMaTK, organizerScopeId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@PathVariable String id, Authentication authentication) {
        String requesterMaTK = currentUserService.resolveMaTK(authentication);
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        TepTin tepTin = uploadService.getFileForAccess(id, requesterMaTK, organizerScopeId);
        byte[] content = uploadService.readFileBytes(tepTin);
        // Trả đúng Content-Type gốc (image/jpeg, application/pdf...) để trình duyệt hiển thị/preview
        // đúng thay vì mặc định application/octet-stream (nguyên nhân chính của bug ảnh không hiển thị).
        MediaType mediaType = tepTin.getContentType() != null
                ? MediaType.parseMediaType(tepTin.getContentType()) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Content-Disposition", "inline; filename=\"" + tepTin.getTenFile() + "\"")
                .body(content);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FileUploadResponseDTO>> update(
            @PathVariable String id, @Valid @RequestBody TepTinRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(uploadService.updateFile(id, dto, staffId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        uploadService.deleteFile(id, staffId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa file"));
    }
}
