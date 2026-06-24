package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity LuatDiem (PointRule) - luật tính điểm theo hạng trong một mùa giải.
 */
@Entity
@Table(name = "LuatDiem")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LuatDiem {

    @Id
    @Column(name = "maLuatDiem", length = 50)
    private String maLuatDiem;

    @Column(name = "maMuaGiai", nullable = false, length = 50)
    private String maMuaGiai;

    @Column(name = "hang", nullable = false)
    private Integer hang;

    @Column(name = "diem", nullable = false)
    private Double diem;
}
