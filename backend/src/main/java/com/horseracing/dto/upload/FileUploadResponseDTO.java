package com.horseracing.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponseDTO {
    private String fileId;
    private String url;
    private String fileName;
    private String fileType;
    private String fileCategory;
    private long fileSize;
    private String targetType;
    private String targetId;
    private String ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
}
