package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity RefreshToken - lưu trữ refresh token để hỗ trợ cơ chế làm mới access token (JWT).
 * Đây là entity hạ tầng phục vụ Auth, không nằm trong 7 thực thể nghiệp vụ chính.
 */
@Entity
@Table(name = "RefreshToken")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "maTK", nullable = false, length = 50)
    private String maTK;

    @Column(name = "ngayHetHan", nullable = false)
    private LocalDateTime ngayHetHan;

    @Column(name = "daThuHoi")
    private Boolean daThuHoi;

    @Column(name = "ngayTao", updatable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
        if (daThuHoi == null) {
            daThuHoi = false;
        }
    }
}
