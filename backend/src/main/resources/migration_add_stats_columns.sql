-- ============================================================
-- Migration: Thêm cột chỉ số sức khỏe vào bảng Jockey
-- Chạy một lần sau khi schema ban đầu đã được tạo
-- ============================================================

-- Thêm cột cân nặng (kg)
ALTER TABLE Jockey
    ADD COLUMN IF NOT EXISTS canNang DOUBLE NULL COMMENT 'Cân nặng hiện tại (kg)';

-- Thêm cột BMI
ALTER TABLE Jockey
    ADD COLUMN IF NOT EXISTS bmi DOUBLE NULL COMMENT 'Chỉ số BMI';

-- Thêm cột tỷ lệ thắng (%)
ALTER TABLE Jockey
    ADD COLUMN IF NOT EXISTS tyLeThang DOUBLE NULL COMMENT 'Tỷ lệ thắng (%)';

-- Thêm cột ghi chú sức khỏe
ALTER TABLE Jockey
    ADD COLUMN IF NOT EXISTS ghiChu VARCHAR(500) NULL COMMENT 'Ghi chú sức khỏe';

-- Thêm cột ngày tạo / ngày cập nhật
ALTER TABLE Jockey
    ADD COLUMN IF NOT EXISTS ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo bản ghi';

ALTER TABLE Jockey
    ADD COLUMN IF NOT EXISTS ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật gần nhất';

-- Thêm cột ngày tạo / ngày cập nhật vào bảng Ngua
ALTER TABLE Ngua
    ADD COLUMN IF NOT EXISTS ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo bản ghi';

ALTER TABLE Ngua
    ADD COLUMN IF NOT EXISTS ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật gần nhất';
