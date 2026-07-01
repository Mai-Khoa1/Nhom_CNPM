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
    trangThai ENUM('Chờ duyệt', 'Đã duyệt', 'Bị từ chối', 'Đang hoạt động', 'Không hoạt động') DEFAULT 'Chờ duyệt',
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
    trangThai ENUM('Chờ duyệt', 'Đủ điều kiện', 'Bị từ chối', 'Bị loại') DEFAULT 'Chờ duyệt',
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
    trangThai ENUM('Chờ duyệt', 'Đã duyệt', 'Từ chối') DEFAULT 'Chờ duyệt',
    lyDoTuChoi VARCHAR(255),
    ghiChu VARCHAR(255),
    FOREIGN KEY (maChangDua) REFERENCES ChangDua(maChangDua) ON DELETE CASCADE,
    FOREIGN KEY (maNgua) REFERENCES Ngua(maNgua) ON DELETE RESTRICT,
    FOREIGN KEY (maNaiNgua) REFERENCES Jockey(maNaiNgua) ON DELETE RESTRICT,
    UNIQUE KEY unique_ngua_moi_chang (maChangDua, maNgua),
    UNIQUE KEY unique_jockey_moi_chang (maChangDua, maNaiNgua)
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

-- Bảng TepTin - lưu trữ file upload (ảnh ngựa/jockey, hồ sơ sức khỏe...)
CREATE TABLE IF NOT EXISTS TepTin (
    maTepTin VARCHAR(50) PRIMARY KEY,
    tenFile VARCHAR(255) NOT NULL,
    duongDan VARCHAR(500) NOT NULL,
    loaiFile VARCHAR(50) COMMENT 'FileType: HORSE_PHOTO, JOCKEY_AVATAR...',
    loaiDoiTuong VARCHAR(50) COMMENT 'HORSE, JOCKEY, HEALTH_RECORD, DOPING_TEST',
    maDoiTuong VARCHAR(50),
    kichThuoc BIGINT,
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng YeuCauCapNhat - lưu yêu cầu chủ ngựa sửa thông tin Ngựa/Nài đã được duyệt trước đó,
-- chờ Ban tổ chức duyệt lại. Dữ liệu cũ/mới lưu dạng JSON để hiển thị so sánh; dữ liệu gốc
-- trong bảng Ngua/Jockey chỉ bị ghi đè khi yêu cầu được DUYỆT.
CREATE TABLE IF NOT EXISTS YeuCauCapNhat (
    maYeuCau VARCHAR(50) PRIMARY KEY,
    loaiDoiTuong ENUM('NGUA', 'NAI_NGUA') NOT NULL,
    maDoiTuong VARCHAR(50) NOT NULL COMMENT 'maNgua hoặc maNaiNgua tương ứng',
    maTK VARCHAR(50) NOT NULL COMMENT 'Tài khoản chủ ngựa gửi yêu cầu',
    duLieuCu TEXT NOT NULL COMMENT 'Snapshot JSON dữ liệu trước khi sửa',
    duLieuMoi TEXT NOT NULL COMMENT 'Snapshot JSON dữ liệu đề xuất sửa',
    trangThai ENUM('Chờ duyệt', 'Đã duyệt', 'Từ chối') DEFAULT 'Chờ duyệt',
    lyDoTuChoi VARCHAR(255),
    ngayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    ngayXuLy DATETIME,
    FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) ON DELETE CASCADE
);

-- Các Index tối ưu hóa truy vấn
CREATE INDEX idx_ngua_ten ON Ngua(tenNgua);
CREATE INDEX idx_jockey_ten ON Jockey(hoTen);
CREATE INDEX idx_changdua_ngay ON ChangDua(ngayThiDau);