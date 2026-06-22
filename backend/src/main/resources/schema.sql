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
    trangThai ENUM('Mở đăng ký', 'Đang diễn ra', 'Đã kết thúc', 'Đã hủy') DEFAULT 'Mở đăng ký'
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
    trangThai ENUM('Chưa diễn ra', 'Đang đua', 'Hoàn thành', 'Đã hủy') DEFAULT 'Chưa diễn ra',
    moTa TEXT,
    FOREIGN KEY (maMuaGiai) REFERENCES MuaGiai(maMuaGiai) ON DELETE RESTRICT
);

-- Bảng Jockey
CREATE TABLE IF NOT EXISTS Jockey (
    maNaiNgua VARCHAR(50) PRIMARY KEY,
    maChuNgua VARCHAR(50) NOT NULL,
    hoTen VARCHAR(100) NOT NULL,
    ngaySinh DATE,
    quocTich VARCHAR(50),
    kinhNghiem INT,
    soGiayPhep VARCHAR(50) UNIQUE,
    trangThai ENUM('Sẵn sàng', 'Chấn thương', 'Nghỉ hưu') DEFAULT 'Sẵn sàng',
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
    trangThai ENUM('Đủ điều kiện', 'Chờ duyệt', 'Chấn thương', 'Bị loại') DEFAULT 'Chờ duyệt',
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

-- Các Index tối ưu hóa truy vấn
CREATE INDEX idx_ngua_ten ON Ngua(tenNgua);
CREATE INDEX idx_jockey_ten ON Jockey(hoTen);
CREATE INDEX idx_changdua_ngay ON ChangDua(ngayThiDau);