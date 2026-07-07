-- Bảng TaiKhoan
CREATE TABLE IF NOT EXISTS TaiKhoan (
    maTK VARCHAR(50) PRIMARY KEY,
    tenDangNhap VARCHAR(50) NOT NULL UNIQUE,
    matKhau VARCHAR(255) NOT NULL,
    hoTen VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    soDienThoai VARCHAR(15),
    vaiTro ENUM('Người xem', 'Chủ ngựa', 'Ban tổ chức', 'Admin') NOT NULL,
    trangThai ENUM('Hoạt động', 'Bị khóa') DEFAULT 'Hoạt động',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng MuaGiai
CREATE TABLE IF NOT EXISTS MuaGiai (
    maMuaGiai VARCHAR(50) PRIMARY KEY,
    tenMuaGiai VARCHAR(100) NOT NULL,
    ngayBatDau DATE,
    ngayKetThuc DATE,
    moTa TEXT,
    trangThai ENUM('Mở đăng ký', 'Đang diễn ra', 'Đã kết thúc', 'Đã hủy') DEFAULT 'Mở đăng ký',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng ChuNgua
CREATE TABLE IF NOT EXISTS ChuNgua (
    maChuNgua VARCHAR(50) PRIMARY KEY,
    maTK VARCHAR(50) NOT NULL UNIQUE,
    hoTen VARCHAR(100) NOT NULL,
    diaChi VARCHAR(255),
    soDienThoai VARCHAR(15),
    email VARCHAR(100),
    FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) ON DELETE CASCADE
);

-- Bảng BanToChuc
CREATE TABLE IF NOT EXISTS BanToChuc (
    maBTC VARCHAR(50) PRIMARY KEY,
    maTK VARCHAR(50) NOT NULL UNIQUE,
    hoTen VARCHAR(100) NOT NULL,
    boPhan ENUM('Điều phối', 'Trọng tài', 'Y tế') NOT NULL,
    chucVu VARCHAR(100),
    FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) ON DELETE CASCADE
);

-- Bảng ThongBao
CREATE TABLE IF NOT EXISTS ThongBao (
    maThongBao VARCHAR(50) PRIMARY KEY,
    maTK VARCHAR(50) NOT NULL,
    tieuDe VARCHAR(150) NOT NULL,
    noiDung TEXT NOT NULL,
    loai VARCHAR(50),
    loaiDoiTuong VARCHAR(50) COMMENT 'Loại đối tượng liên quan (targetType)',
    maDoiTuong VARCHAR(50) COMMENT 'Mã đối tượng liên quan (targetId)',
    ngayGui DATETIME DEFAULT CURRENT_TIMESTAMP,
    trangThai ENUM('Chưa đọc', 'Đã đọc') DEFAULT 'Chưa đọc',
    FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) ON DELETE CASCADE
);

-- Bảng NhatKyHoatDong
CREATE TABLE IF NOT EXISTS NhatKyHoatDong (
    maNhatKy VARCHAR(50) PRIMARY KEY,
    maTK VARCHAR(50),
    loaiHanhDong VARCHAR(50),
    moTa TEXT,
    thoiGian DATETIME DEFAULT CURRENT_TIMESTAMP,
    diaChiIP VARCHAR(45),
    doiTuongTacDong VARCHAR(100),
    FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) ON DELETE SET NULL
);

-- Bảng RefreshToken - phục vụ cơ chế access token + refresh token của JWT
CREATE TABLE IF NOT EXISTS RefreshToken (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    maTK VARCHAR(50) NOT NULL,
    ngayHetHan DATETIME NOT NULL,
    daThuHoi BOOLEAN DEFAULT FALSE,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) ON DELETE CASCADE
);

-- Bảng ChangDua
CREATE TABLE IF NOT EXISTS ChangDua (
    maChangDua VARCHAR(50) PRIMARY KEY,
    maMuaGiai VARCHAR(50) NOT NULL,
    tenChangDua VARCHAR(100) NOT NULL,
    ngayThiDau DATE,
    gioBatDau TIME,
    diaDiem VARCHAR(255),
    cuLy INT,
    loaiMatSan VARCHAR(50),
    soLanDua INT DEFAULT 1,
    soNguaToiDa INT COMMENT 'Số ngựa tối đa được đăng ký (maxHorses)',
    trangThai ENUM('Mở đăng ký', 'Đã đóng đăng ký', 'Đang đua', 'Hoàn thành', 'Đã hủy') DEFAULT 'Mở đăng ký',
    moTa TEXT,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maMuaGiai) REFERENCES MuaGiai(maMuaGiai) ON DELETE RESTRICT
);

-- Bảng Jockey
CREATE TABLE IF NOT EXISTS Jockey (
    maNaiNgua VARCHAR(50) PRIMARY KEY,
    maChuNgua VARCHAR(50) NOT NULL,
    hoTen VARCHAR(100) NOT NULL,
    ngaySinh DATE,
    gioiTinh ENUM('Đực', 'Cái'),
    quocTich VARCHAR(50),
    kinhNghiem INT,
    soGiayPhep VARCHAR(50) UNIQUE,
    canNang DOUBLE COMMENT 'Cân nặng hiện tại (kg)',
    bmi DOUBLE COMMENT 'Chỉ số BMI',
    tyLeThang DOUBLE COMMENT 'Tỷ lệ thắng (%)',
    ghiChu VARCHAR(500) COMMENT 'Ghi chú sức khỏe',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maChuNgua) REFERENCES ChuNgua(maChuNgua) ON DELETE RESTRICT
);

-- Bảng Ngua
CREATE TABLE IF NOT EXISTS Ngua (
    maNgua VARCHAR(50) PRIMARY KEY,
    maChuNgua VARCHAR(50) NOT NULL,
    tenNgua VARCHAR(100) NOT NULL,
    giongNgua VARCHAR(50),
    ngaySinh DATE,
    gioiTinh ENUM('Đực', 'Cái'),
    mauLong VARCHAR(30),
    troiLuong DOUBLE,
    trangThaiSucKhoe VARCHAR(255),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (maChuNgua) REFERENCES ChuNgua(maChuNgua) ON DELETE RESTRICT
);

-- Bảng HoSoSucKhoe
CREATE TABLE IF NOT EXISTS HoSoSucKhoe (
    maHoSo VARCHAR(50) PRIMARY KEY,
    maNgua VARCHAR(50) NOT NULL,
    ngayKham DATETIME DEFAULT CURRENT_TIMESTAMP,
    troiLuong DOUBLE,
    tinhTrangSucKhoe VARCHAR(255),
    ketQuaXetNghiem VARCHAR(255),
    ketQuaDoping ENUM('Âm tính', 'Dương tính') DEFAULT 'Âm tính',
    ghiChu TEXT,
    bacSiKham VARCHAR(100),
    FOREIGN KEY (maNgua) REFERENCES Ngua(maNgua) ON DELETE CASCADE
);

-- Bảng DangKyThiDau
CREATE TABLE IF NOT EXISTS DangKyThiDau (
    maDangKy VARCHAR(50) PRIMARY KEY,
    maChangDua VARCHAR(50) NOT NULL,
    maNgua VARCHAR(50) NOT NULL,
    maNaiNgua VARCHAR(50) NOT NULL,
    ngayDangKy DATETIME DEFAULT CURRENT_TIMESTAMP,
    lanChay INT,
    soLan INT COMMENT 'Số làn đua được phân (lane number)',
    ngayGanLan DATETIME COMMENT 'Thời điểm gán làn đua',
    trangThai ENUM('Chờ duyệt', 'Đã duyệt', 'Từ chối', 'Đã hủy', 'Bị loại') DEFAULT 'Chờ duyệt',
    lyDo VARCHAR(255) COMMENT 'Lý do từ chối/hủy/loại - dùng chung, tùy theo trangThai hiện tại',
    ghiChu VARCHAR(255),
    FOREIGN KEY (maChangDua) REFERENCES ChangDua(maChangDua) ON DELETE CASCADE,
    FOREIGN KEY (maNgua) REFERENCES Ngua(maNgua) ON DELETE RESTRICT,
    FOREIGN KEY (maNaiNgua) REFERENCES Jockey(maNaiNgua) ON DELETE RESTRICT
    -- Không còn UNIQUE KEY cứng (maChangDua, maNgua)/(maChangDua, maNaiNgua): 1 lần đăng ký có thể bị
    -- Từ chối/Đã hủy rồi đăng ký lại cho CÙNG race đó - ràng buộc "không trùng khi đang active" được
    -- kiểm tra ở tầng service (RegistrationService.ACTIVE_STATUSES).
);

-- Bảng KetQuaThiDau
CREATE TABLE IF NOT EXISTS KetQuaThiDau (
    maKetQua VARCHAR(50) PRIMARY KEY,
    maChangDua VARCHAR(50) NOT NULL,
    maNgua VARCHAR(50) NOT NULL,
    hang INT NOT NULL,
    thoiGianHoanThanh VARCHAR(50),
    diem DOUBLE DEFAULT 0,
    ghiChuChuyenMon TEXT,
    trangThaiCongBo ENUM('Chưa công bố', 'Đã công bố') DEFAULT 'Chưa công bố',
    ngayCongBo DATETIME,
    FOREIGN KEY (maChangDua) REFERENCES ChangDua(maChangDua) ON DELETE CASCADE,
    FOREIGN KEY (maNgua) REFERENCES Ngua(maNgua) ON DELETE RESTRICT
);

-- Bảng BangXepHang
CREATE TABLE IF NOT EXISTS BangXepHang (
    maBXH VARCHAR(50) PRIMARY KEY,
    maMuaGiai VARCHAR(50) NOT NULL,
    doiTuongId VARCHAR(50) NOT NULL,
    loaiBXH ENUM('Ngựa đua', 'Nài ngựa') NOT NULL,
    hang INT,
    tongDiem DOUBLE DEFAULT 0,
    soLanThang INT DEFAULT 0,
    soLanThamGia INT DEFAULT 0,
    FOREIGN KEY (maMuaGiai) REFERENCES MuaGiai(maMuaGiai) ON DELETE CASCADE,
    UNIQUE KEY unique_bxh_doi_tuong (maMuaGiai, doiTuongId, loaiBXH)
);

-- Bảng LuatDiem - luật tính điểm theo hạng trong từng mùa giải
CREATE TABLE IF NOT EXISTS LuatDiem (
    maLuatDiem VARCHAR(50) PRIMARY KEY,
    maMuaGiai VARCHAR(50) NOT NULL,
    hang INT NOT NULL,
    diem DOUBLE NOT NULL DEFAULT 0,
    FOREIGN KEY (maMuaGiai) REFERENCES MuaGiai(maMuaGiai) ON DELETE CASCADE,
    UNIQUE KEY unique_luatdiem_hang (maMuaGiai, hang)
);

-- Bảng TepTin - lưu trữ file đính kèm theo TỪNG LẦN ĐĂNG KÝ thi đấu (ảnh ngựa/nài, hồ sơ sức khỏe,
-- giấy phép nài...). maTK: chủ sở hữu tệp (chủ ngựa đã tải lên). Không còn duyệt file riêng lẻ - Ban
-- tổ chức duyệt cả bộ hồ sơ đăng ký (DangKyThiDau) chứa các file này.
CREATE TABLE IF NOT EXISTS TepTin (
    maTepTin VARCHAR(50) PRIMARY KEY,
    tenFile VARCHAR(255) NOT NULL,
    duongDan VARCHAR(500) NOT NULL,
    loaiFile VARCHAR(50) COMMENT 'FileType: HORSE_PHOTO, JOCKEY_AVATAR, HEALTH_CERTIFICATE, JOCKEY_LICENSE...',
    contentType VARCHAR(100) COMMENT 'MIME type gốc (image/jpeg, application/pdf...) - dùng để trả đúng header khi phục vụ file',
    loaiDoiTuong VARCHAR(50) COMMENT 'DANG_KY - tệp tin gắn với 1 lần đăng ký thi đấu cụ thể',
    maDoiTuong VARCHAR(50) COMMENT 'maDangKy tương ứng',
    kichThuoc BIGINT,
    maTK VARCHAR(50) COMMENT 'Tài khoản chủ ngựa đã tải file lên',
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Thêm cột maTK cho DB đã tồn tại từ trước (idempotent qua continue-on-error=true như mọi ALTER khác
-- trong file này). trangThai (duyệt file riêng lẻ) không còn cần thiết - bỏ.
ALTER TABLE TepTin ADD COLUMN maTK VARCHAR(50) COMMENT 'Tài khoản chủ ngựa đã tải file lên';
ALTER TABLE TepTin ADD COLUMN contentType VARCHAR(100) COMMENT 'MIME type gốc';
ALTER TABLE TepTin DROP COLUMN trangThai;

-- Bảng YeuCauCapNhat - lưu yêu cầu chủ ngựa sửa/xóa thông tin Ngựa/Nài/Tệp tin đã được duyệt trước đó,
-- chờ Ban tổ chức duyệt lại. Dữ liệu cũ/mới lưu dạng JSON để hiển thị so sánh; dữ liệu gốc
-- trong bảng Ngua/Jockey/TepTin chỉ bị ghi đè (hoặc xóa) khi yêu cầu được DUYỆT.
CREATE TABLE IF NOT EXISTS YeuCauCapNhat (
    maYeuCau VARCHAR(50) PRIMARY KEY,
    loaiDoiTuong ENUM('NGUA', 'NAI_NGUA', 'TEP_TIN') NOT NULL,
    maDoiTuong VARCHAR(50) NOT NULL COMMENT 'maNgua/maNaiNgua/maTepTin tương ứng',
    maTK VARCHAR(50) NOT NULL COMMENT 'Tài khoản chủ ngựa gửi yêu cầu',
    duLieuCu TEXT NOT NULL COMMENT 'Snapshot JSON dữ liệu trước khi sửa',
    duLieuMoi TEXT NOT NULL COMMENT 'Snapshot JSON dữ liệu đề xuất sửa',
    trangThai ENUM('Chờ duyệt', 'Đã duyệt', 'Từ chối') DEFAULT 'Chờ duyệt',
    hanhDong ENUM('CAP_NHAT', 'XOA') DEFAULT 'CAP_NHAT' COMMENT 'Loại thao tác áp dụng khi được duyệt',
    lyDoTuChoi VARCHAR(255),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayXuLy DATETIME,
    FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) ON DELETE CASCADE
);

-- Thêm cột hanhDong và mở rộng ENUM loaiDoiTuong cho DB đã tồn tại từ trước (MODIFY COLUMN tự idempotent;
-- ADD COLUMN dựa vào continue-on-error=true như trên).
ALTER TABLE YeuCauCapNhat MODIFY COLUMN loaiDoiTuong ENUM('NGUA', 'NAI_NGUA', 'TEP_TIN') NOT NULL;
ALTER TABLE YeuCauCapNhat ADD COLUMN hanhDong ENUM('CAP_NHAT', 'XOA') DEFAULT 'CAP_NHAT' COMMENT 'Loại thao tác áp dụng khi được duyệt';
ALTER TABLE YeuCauCapNhat ADD COLUMN maBTC VARCHAR(50) COMMENT 'Ban tổ chức cần duyệt yêu cầu này';

-- ===== Bước 4: mở rộng DangKyThiDau (CANCELLED/DISQUALIFIED) + bỏ UNIQUE KEY cứng cho DB đã tồn tại =====
ALTER TABLE DangKyThiDau MODIFY COLUMN trangThai ENUM('Chờ duyệt', 'Đã duyệt', 'Từ chối', 'Đã hủy', 'Bị loại') DEFAULT 'Chờ duyệt';
ALTER TABLE DangKyThiDau CHANGE COLUMN lyDoTuChoi lyDo VARCHAR(255);
-- unique_ngua_moi_chang/unique_jockey_moi_chang là index duy nhất có maChangDua làm cột đầu, đang được
-- FOREIGN KEY (maChangDua) dùng làm index hỗ trợ - phải tạo index thường thay thế TRƯỚC khi xóa 2 UNIQUE
-- KEY đó, nếu không MySQL báo lỗi 1553 "needed in a foreign key constraint" khi xóa index còn lại cuối cùng.
CREATE INDEX idx_dangkythidau_changdua ON DangKyThiDau (maChangDua);
ALTER TABLE DangKyThiDau DROP INDEX unique_ngua_moi_chang;
ALTER TABLE DangKyThiDau DROP INDEX unique_jockey_moi_chang;

-- ===== Multi-tenancy theo Ban tổ chức: mỗi Mùa giải thuộc về đúng 1 Ban tổ chức (bảng BanToChuc =====
-- đã có sẵn phía trên). ADD COLUMN dựa vào spring.sql.init.continue-on-error=true (idempotent như trên).
-- Không thêm FOREIGN KEY ràng buộc bằng ALTER (constraint tên tự sinh sẽ bị thêm lại mỗi lần khởi động
-- vì không idempotent) - giống cách TepTin.maTK không có FK, ràng buộc được đảm bảo ở tầng service.
ALTER TABLE MuaGiai ADD COLUMN maBTC VARCHAR(50) COMMENT 'Ban tổ chức sở hữu mùa giải này (multi-tenancy)';

-- ===== Tách "hồ sơ" (Ngựa/Nài) khỏi "duyệt thi đấu": bỏ trạng thái duyệt ở cấp hồ sơ, việc duyệt =====
-- chỉ còn diễn ra ở cấp đăng ký thi đấu (DangKyThiDau.trangThai). DROP COLUMN không idempotent - dựa
-- vào continue-on-error=true, lần đầu chạy sẽ xóa cột thành công, các lần sau lỗi "unknown column"
-- và bị bỏ qua (giống cơ chế ADD COLUMN chỉ chạy 1 lần thành công ở trên).
ALTER TABLE Ngua DROP COLUMN trangThai;
ALTER TABLE Jockey DROP COLUMN trangThai;

-- Ban tổ chức cần duyệt 1 yêu cầu cập nhật thông tin ngựa/nài (xác định qua đăng ký APPROVED đang
-- tham chiếu ngựa/nài đó - 1 ngựa có thể có nhiều yêu cầu song song tới nhiều BTC khác nhau).
ALTER TABLE YeuCauCapNhat ADD COLUMN maBTC VARCHAR(50) COMMENT 'Ban tổ chức cần duyệt yêu cầu này';

-- Các Index tối ưu hóa truy vấn
CREATE INDEX idx_ngua_ten ON Ngua(tenNgua);
CREATE INDEX idx_jockey_ten ON Jockey(hoTen);

-- ===== Sửa lỗi vòng đời Cuộc đua (kiểm thử thực tế): thêm số lượng đăng ký tối thiểu để cho phép =====
-- chuyển OPEN -> ONGOING (mặc định 2 - không thể đua với 0 hoặc 1 ngựa).
ALTER TABLE ChangDua ADD COLUMN soLuongToiThieu INT DEFAULT 2 COMMENT 'Số đăng ký ĐÃ DUYỆT tối thiểu để cho phép bắt đầu đua (minHorses)';
CREATE INDEX idx_changdua_ngay ON ChangDua(ngayThiDau);

-- ===== Lỗi 6: xóa mềm (soft-delete) Ngựa/Nài đã từng có đăng ký thi đấu, tránh lỗi FK constraint =====
-- hiển thị thẳng ra người dùng. Hồ sơ CHƯA từng đăng ký vẫn xóa cứng như trước (không cần cột này).
ALTER TABLE Ngua ADD COLUMN trangThai ENUM('Hoạt động', 'Ngừng hoạt động') DEFAULT 'Hoạt động' COMMENT 'Xóa mềm - Ngừng hoạt động khi đã từng có đăng ký thi đấu';
ALTER TABLE Jockey ADD COLUMN trangThai ENUM('Hoạt động', 'Ngừng hoạt động') DEFAULT 'Hoạt động' COMMENT 'Xóa mềm - Ngừng hoạt động khi đã từng có đăng ký thi đấu';