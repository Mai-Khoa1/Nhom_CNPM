-- Dữ liệu mẫu khởi tạo tài khoản hệ thống công việc đua ngựa
INSERT IGNORE INTO TaiKhoan (maTK, tenDangNhap, matKhau, hoTen, email, vaiTro, trangThai) VALUES
('TK_ADM01', 'admin', '$2a$10$D6li137rDVWspfJjvuCOl.F6BToe4PrZBZQjob9noRZl62nV7Rckq', 'admin', 'admin123@gmail.com', 'admin', 'Hoạt động'),
('TK_OWN01', 'chungua_anbinh', '$2a$10$D6li137rDVWspfJjvuCOl.F6BToe4PrZBZQjob9noRZl62nV7Rckq', 'Nguyễn Văn An', 'anbinhstable@gmail.com', 'Chủ ngựa', 'Hoạt động');

-- Mùa giải mặc định - dùng làm maMuaGiai mặc định khi tạo ChangDua (Schedule) vì MuaGiai chưa có module quản lý riêng
INSERT IGNORE INTO MuaGiai (maMuaGiai, tenMuaGiai, ngayBatDau, ngayKetThuc, moTa, trangThai) VALUES
('MG_DEFAULT', 'Mùa giải mặc định', '2026-01-01', '2026-12-31', 'Mùa giải mặc định dùng cho các chặng đua chưa gán mùa giải cụ thể', 'Đang diễn ra');

-- Dữ liệu mẫu cho thông tin mở rộng chủ ngựa
INSERT IGNORE INTO ChuNgua (maChuNgua, maTK, hoTen, diaChi, soDienThoai, email) VALUES 
('CN001', 'TK_OWN01', 'Nguyễn Văn An', 'Gò Vấp, Ho Chi Minh City', '0901234567', 'anbinhstable@gmail.com');

-- Dữ liệu mẫu cho ngựa đua
INSERT IGNORE INTO Ngua (maNgua, maChuNgua, tenNgua, giongNgua, gioiTinh, troiLuong, trangThai) VALUES 
('N001', 'CN001', 'Xích Thố Vương', 'Thuần Chủng Anh', 'Đực', 450.5, 'Đủ điều kiện');