-- Dữ liệu mẫu khởi tạo tài khoản hệ thống công việc đua ngựa
-- Mật khẩu của mọi tài khoản mẫu bên dưới đều là "123456" (cùng một bcrypt hash).
INSERT IGNORE INTO TaiKhoan (maTK, tenDangNhap, matKhau, hoTen, email, soDienThoai, vaiTro, trangThai) VALUES
('TK_ADM01', 'admin', '$2a$10$D6li137rDVWspfJjvuCOl.F6BToe4PrZBZQjob9noRZl62nV7Rckq', 'admin', 'admin123@gmail.com', NULL, 'Admin', 'Hoạt động'),
('TK_OWN01', 'chungua_anbinh', '$2a$10$D6li137rDVWspfJjvuCOl.F6BToe4PrZBZQjob9noRZl62nV7Rckq', 'Nguyễn Văn An', 'anbinhstable@gmail.com', '0901234567', 'Chủ ngựa', 'Hoạt động'),
('TK_ORG01', 'btc_lananh', '$2a$10$D6li137rDVWspfJjvuCOl.F6BToe4PrZBZQjob9noRZl62nV7Rckq', 'Lê Thị Lan Anh', 'lananh.btc@gmail.com', '0912345678', 'Ban tổ chức', 'Hoạt động'),
('TK_ORG02', 'btc_minhtuan', '$2a$10$D6li137rDVWspfJjvuCOl.F6BToe4PrZBZQjob9noRZl62nV7Rckq', 'Nguyễn Minh Tuấn', 'minhtuan.btc@gmail.com', '0933112233', 'Ban tổ chức', 'Hoạt động'),
('TK_OWN02', 'chungua_binhminh', '$2a$10$D6li137rDVWspfJjvuCOl.F6BToe4PrZBZQjob9noRZl62nV7Rckq', 'Trần Thị Bình Minh', 'binhminhstable@gmail.com', '0987654321', 'Chủ ngựa', 'Hoạt động'),
('TK_VIEW01', 'nguoixem_hoa', '$2a$10$D6li137rDVWspfJjvuCOl.F6BToe4PrZBZQjob9noRZl62nV7Rckq', 'Phạm Thị Hoa', 'hoa.xem@gmail.com', '0977123456', 'Người xem', 'Hoạt động');

-- Hồ sơ Ban tổ chức (mở rộng 1-1 từ tài khoản vaiTro = 'Ban tổ chức') - 2 BTC độc lập để test multi-tenancy
-- (dữ liệu mùa giải/cuộc đua/đăng ký của BTC1 và BTC2 không được lẫn vào nhau).
INSERT IGNORE INTO BanToChuc (maBTC, maTK, hoTen, boPhan, chucVu) VALUES
('BTC001', 'TK_ORG01', 'Lê Thị Lan Anh', 'Điều phối', 'Trưởng ban tổ chức'),
('BTC002', 'TK_ORG02', 'Nguyễn Minh Tuấn', 'Điều phối', 'Trưởng ban tổ chức');

-- Mùa giải: mùa hiện tại (đang diễn ra, dùng làm mặc định khi tạo ChangDua) + một mùa đã kết thúc để có dữ liệu lịch sử
-- (kết quả/bảng xếp hạng) cho việc demo. MG_DEFAULT/MG_2025 thuộc BTC001; MG_ORG02 thuộc BTC002 (dữ liệu test tách biệt).
INSERT IGNORE INTO MuaGiai (maMuaGiai, maBTC, tenMuaGiai, ngayBatDau, ngayKetThuc, moTa, trangThai) VALUES
('MG_DEFAULT', 'BTC001', 'Mùa giải mặc định', '2026-01-01', '2026-12-31', 'Mùa giải mặc định dùng cho các chặng đua chưa gán mùa giải cụ thể', 'Đang diễn ra'),
('MG_2025', 'BTC001', 'Mùa giải 2025', '2025-01-01', '2025-12-31', 'Mùa giải 2025 đã kết thúc, dùng làm dữ liệu lịch sử (kết quả, bảng xếp hạng)', 'Đã kết thúc'),
('MG_ORG02', 'BTC002', 'Mùa giải BTC Minh Tuấn 2026', '2026-01-01', '2026-12-31', 'Mùa giải riêng của Ban tổ chức thứ 2, dùng để test tách biệt dữ liệu multi-tenancy', 'Đang diễn ra');

-- Backfill maBTC cho DB đã tồn tại từ trước khi có cột này (lần đầu ALTER TABLE ADD COLUMN chạy xong,
-- các dòng MuaGiai cũ sẽ có maBTC = NULL vì INSERT IGNORE ở trên không chèn lại - gán về BTC001 mặc định).
UPDATE MuaGiai SET maBTC = 'BTC001' WHERE maBTC IS NULL;

-- Dữ liệu mẫu cho thông tin mở rộng chủ ngựa (2 chủ ngựa để có dữ liệu phân quyền theo owner - tránh IDOR)
INSERT IGNORE INTO ChuNgua (maChuNgua, maTK, hoTen, diaChi, soDienThoai, email) VALUES
('CN001', 'TK_OWN01', 'Nguyễn Văn An', 'Gò Vấp, Ho Chi Minh City', '0901234567', 'anbinhstable@gmail.com'),
('CN002', 'TK_OWN02', 'Trần Thị Bình Minh', 'Thủ Đức, Ho Chi Minh City', '0987654321', 'binhminhstable@gmail.com');

-- Dữ liệu mẫu cho nài ngựa (Jockey) - hồ sơ riêng của chủ ngựa, không còn trạng thái duyệt ở cấp hồ sơ
-- (việc duyệt chỉ diễn ra ở cấp đăng ký thi đấu - xem DangKyThiDau).
INSERT IGNORE INTO Jockey (maNaiNgua, maChuNgua, hoTen, ngaySinh, gioiTinh, quocTich, kinhNghiem, soGiayPhep, canNang, bmi, tyLeThang) VALUES
('NN001', 'CN001', 'Lê Văn Hùng', '1995-03-12', 'Đực', 'Việt Nam', 8, 'GP-2025-001', 54.5, 19.2, 32.5),
('NN002', 'CN001', 'Phạm Minh Tuấn', '1999-11-20', 'Đực', 'Việt Nam', 2, 'GP-2025-002', 56.0, 20.1, 0),
('NN003', 'CN002', 'Nguyễn Thị Lan', '1997-06-05', 'Cái', 'Việt Nam', 5, 'GP-2025-003', 50.0, 18.5, 41.0);

-- Dữ liệu mẫu cho ngựa đua - hồ sơ riêng của chủ ngựa, không còn trạng thái duyệt ở cấp hồ sơ.
INSERT IGNORE INTO Ngua (maNgua, maChuNgua, tenNgua, giongNgua, ngaySinh, gioiTinh, mauLong, troiLuong) VALUES
('N001', 'CN001', 'Xích Thố Vương', 'Thuần Chủng Anh', '2021-04-10', 'Đực', 'Đỏ hồng', 450.5),
('N002', 'CN001', 'Hắc Phong', 'Thuần Chủng Ả Rập', '2022-02-18', 'Đực', 'Đen tuyền', 430.0),
('N003', 'CN002', 'Bạch Mã', 'Thuần Chủng Anh', '2021-09-25', 'Cái', 'Trắng', 415.0),
('N004', 'CN002', 'Kim Long', 'Thuần Chủng Úc', '2020-12-01', 'Đực', 'Vàng kim', 460.0);

-- Dữ liệu mẫu cho chặng đua: một chặng sắp diễn ra (mùa giải hiện tại) và một chặng đã hoàn thành (mùa giải 2025)
-- để có đủ dữ liệu cho Đăng ký/Kết quả/Bảng xếp hạng.
INSERT IGNORE INTO ChangDua (maChangDua, maMuaGiai, tenChangDua, ngayThiDau, gioBatDau, diaDiem, cuLy, loaiMatSan, soLanDua, soNguaToiDa, trangThai, moTa) VALUES
('CD001', 'MG_DEFAULT', 'Chặng đua mùa xuân 2026', '2026-08-15', '08:00:00', 'Trường đua Phú Thọ', 1600, 'Đất', 1, 12, 'Mở đăng ký', 'Chặng đua mở màn mùa giải 2026'),
('CD002', 'MG_2025', 'Chặng đua chung kết 2025', '2025-12-15', '09:00:00', 'Trường đua Phú Thọ', 2000, 'Cỏ', 1, 12, 'Hoàn thành', 'Chặng đua chung kết khép lại mùa giải 2025'),
('CD003', 'MG_ORG02', 'Chặng đua BTC 2 - Vòng loại', '2026-09-20', '08:00:00', 'Trường đua Đại Nam', 1400, 'Đất', 1, 10, 'Mở đăng ký', 'Chặng đua của Ban tổ chức thứ 2, dùng để test tách biệt dữ liệu multi-tenancy');

-- Dữ liệu mẫu cho đăng ký thi đấu
INSERT IGNORE INTO DangKyThiDau (maDangKy, maChangDua, maNgua, maNaiNgua, lanChay, soLan, trangThai) VALUES
('DK001', 'CD002', 'N001', 'NN001', 1, 3, 'Đã duyệt'),
('DK002', 'CD001', 'N003', 'NN003', 1, NULL, 'Chờ duyệt');

-- Dữ liệu mẫu cho kết quả thi đấu (đã công bố) của chặng đua đã hoàn thành
INSERT IGNORE INTO KetQuaThiDau (maKetQua, maChangDua, maNgua, hang, thoiGianHoanThanh, diem, trangThaiCongBo, ngayCongBo) VALUES
('KQ001', 'CD002', 'N001', 1, '2:05.32', 100, 'Đã công bố', '2025-12-15 11:30:00');

-- Luật tính điểm theo hạng cho mùa giải 2025 (dùng khi tính bảng xếp hạng)
INSERT IGNORE INTO LuatDiem (maLuatDiem, maMuaGiai, hang, diem) VALUES
('LD2025_1', 'MG_2025', 1, 100),
('LD2025_2', 'MG_2025', 2, 70),
('LD2025_3', 'MG_2025', 3, 50);

-- Dọn dữ liệu TepTin/YeuCauCapNhat cũ gắn thẳng vào Ngựa/Nài (model trước Bước 4) trên DB đã tồn tại
-- từ trước - INSERT IGNORE bên dưới sẽ không tự sửa được các dòng đã có sẵn vì trùng khóa chính.
DELETE FROM YeuCauCapNhat WHERE loaiDoiTuong = 'TEP_TIN' AND maDoiTuong IN (SELECT maTepTin FROM TepTin WHERE loaiDoiTuong IN ('HORSE', 'JOCKEY'));
DELETE FROM TepTin WHERE loaiDoiTuong IN ('HORSE', 'JOCKEY');

-- Dữ liệu mẫu cho Tệp tin - gắn với LẦN ĐĂNG KÝ đã duyệt DK001 (không gắn thẳng vào Ngựa/Nài nữa).
-- Lưu ý: duongDan chỉ là đường dẫn mẫu, không có file vật lý tương ứng trên đĩa nên chức năng tải xuống
-- sẽ báo lỗi với các file này - phù hợp để demo danh sách nhưng không dùng để test tải xuống thật.
INSERT IGNORE INTO TepTin (maTepTin, tenFile, duongDan, loaiFile, contentType, loaiDoiTuong, maDoiTuong, kichThuoc, maTK) VALUES
('FT_SAMPLE01', 'ngua-n001-anh-dep.jpg', 'uploads/sample/ft-sample01.jpg', 'HORSE_PHOTO', 'image/jpeg', 'DANG_KY', 'DK001', 245678, 'TK_OWN01'),
('FT_SAMPLE02', 'ngua-n001-giay-kham.pdf', 'uploads/sample/ft-sample02.pdf', 'HEALTH_CERTIFICATE', 'application/pdf', 'DANG_KY', 'DK001', 512340, 'TK_OWN01');

-- Dữ liệu mẫu cho Yêu cầu cập nhật (YeuCauCapNhat) - 1 yêu cầu sửa thông tin ngựa N001 gửi tới BTC001
-- (N001 đang có đăng ký APPROVED là DK001 thuộc BTC001) đang chờ duyệt (N001 vẫn giữ dữ liệu gốc cho
-- tới khi được duyệt).
INSERT IGNORE INTO YeuCauCapNhat (maYeuCau, loaiDoiTuong, maDoiTuong, maTK, maBTC, duLieuCu, duLieuMoi, trangThai, hanhDong) VALUES
('YC_SAMPLE01', 'NGUA', 'N001', 'TK_OWN01', 'BTC001',
 '{"code":"N001","name":"Xích Thố Vương","breed":"Thuần Chủng Anh","dateOfBirth":"2021-04-10","gender":"MALE","color":"Đỏ hồng","weight":450.5}',
 '{"code":"N001","name":"Xích Thố Vương","breed":"Thuần Chủng Anh","dateOfBirth":"2021-04-10","gender":"MALE","color":"Đỏ hồng","weight":462.0}',
 'Chờ duyệt', 'CAP_NHAT');

-- Dữ liệu mẫu cho Thông báo (ThongBao) - gửi cho chủ ngựa và ban tổ chức, minh họa các loại thông báo khác nhau
INSERT IGNORE INTO ThongBao (maThongBao, maTK, tieuDe, noiDung, loai, loaiDoiTuong, maDoiTuong, trangThai) VALUES
('TB_SAMPLE01', 'TK_OWN01', 'Ngựa mới', 'Ngựa Hắc Phong đã được ghi nhận vào hồ sơ của bạn.', 'SYSTEM', 'HORSE', 'N002', 'Chưa đọc'),
('TB_SAMPLE03', 'TK_OWN01', 'Đăng ký thi đấu được duyệt', 'Đăng ký cho ngựa Xích Thố Vương tại Chặng đua chung kết 2025 đã được duyệt.', 'APPROVAL', 'REGISTRATION', 'DK001', 'Đã đọc'),
('TB_SAMPLE04', 'TK_ORG01', 'Yêu cầu cập nhật thông tin ngựa', 'Chủ ngựa Nguyễn Văn An yêu cầu cập nhật thông tin ngựa Xích Thố Vương, cần duyệt.', 'SYSTEM', 'HORSE_UPDATE_REQUEST', 'YC_SAMPLE01', 'Chưa đọc');

-- Dữ liệu mẫu cho Nhật ký hoạt động (audit log) - minh họa các hành động của Admin/Ban tổ chức
INSERT IGNORE INTO NhatKyHoatDong (maNhatKy, maTK, loaiHanhDong, moTa, doiTuongTacDong) VALUES
('NK_SAMPLE01', 'TK_OWN01', 'CREATE_HORSE', 'Tạo ngựa mới: Hắc Phong', 'Horse:N002'),
('NK_SAMPLE02', 'TK_ORG01', 'REJECT_HORSE', 'Cập nhật trạng thái ngựa Kim Long -> REJECTED (Lý do: Không đạt tiêu chuẩn sức khỏe)', 'Horse:N004'),
('NK_SAMPLE03', 'TK_ORG01', 'APPROVE_REGISTRATION', 'Duyệt đăng ký thi đấu DK001', 'Registration:DK001');