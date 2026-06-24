# 🐎 HỆ THỐNG QUẢN LÝ ĐUA NGỰA (HORSE RACING MANAGEMENT)

## 📖 Giới thiệu

Hệ thống Quản lý Đua Ngựa (Horse Racing Management System) là dự án môn học **Công nghệ Phần mềm (CNPM)** được thực hiện bởi nhóm 3 - Khoa Công nghệ Thông tin.

Hệ thống được xây dựng theo mô hình Client - Server gồm:

* **Backend:** Spring Boot (Java)
* **Frontend:** ReactJS + Vite
* **Database:** MySQL
* **Authentication:** JWT (JSON Web Token)
* **Password Encryption:** BCryptPasswordEncoder

Mục tiêu của hệ thống là hỗ trợ quản lý giải đấu đua ngựa, nài ngựa, ngựa đua, lịch thi đấu, kết quả thi đấu và người dùng tham gia hệ thống.

---

# 📂 Cấu Trúc Thư Mục Dự Án

```text
Nhom_CNPM/
│
├── backend/                           # Backend Spring Boot
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/horseracing/
│       │   ├── HorseRacingApplication.java
│       │   │
│       │   ├── config/                # JWT & Security Configuration
│       │   ├── controller/            # REST API Controllers
│       │   ├── dto/                   # Request/Response DTO
│       │   ├── entity/                # Database Entities
│       │   ├── exception/             # Global Exception Handler
│       │   ├── repository/            # JPA Repository Layer
│       │   └── service/               # Business Logic Layer
│       │
│       └── resources/
│           ├── application.properties
│           ├── schema.sql
│           └── data.sql
│
├── frontend/                          # Frontend ReactJS + Vite
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.ts
│   ├── index.html
│   └── src/
│
└── README.md
```

---

# 🛠️ Yêu Cầu Hệ Thống

Trước khi chạy dự án, cần cài đặt các công cụ sau:

## Backend

* Java JDK 17+
* Apache Maven 3.x

## Frontend

* Node.js v18 hoặc v20
* npm / pnpm / yarn

## Database

* MySQL Server 8.0+

---

# 🚀 Hướng Dẫn Cài Đặt Và Khởi Chạy

## 1. Khởi Chạy Backend (Spring Boot)

### Bước 1: Di chuyển vào thư mục Backend

```bash
cd backend
```

### Bước 2: Cấu hình kết nối Database

Mở file:

```text
src/main/resources/application.properties
```

Cập nhật thông tin MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/horseracing?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

### Bước 3: Khởi tạo Database

Khi ứng dụng chạy lần đầu:

* `schema.sql` sẽ tạo cấu trúc bảng.
* `data.sql` sẽ nạp dữ liệu mẫu.

Không cần tạo dữ liệu thủ công.

### Bước 4: Chạy Backend

```bash
mvn clean spring-boot:run
```

Sau khi khởi động thành công:

```text
http://localhost:8080
```

---

## 2. Khởi Chạy Frontend (ReactJS)

### Bước 1: Mở Terminal mới

```bash
cd frontend
```

### Bước 2: Cài đặt thư viện

```bash
npm install
```

### Bước 3: Chạy ứng dụng

```bash
npm run dev
```

Frontend sẽ chạy tại:

```text
http://localhost:5173
```

(Vite có thể tự động đổi sang cổng khác nếu cổng mặc định đang được sử dụng.)

---

# 🔐 Xác Thực Và Phân Quyền

Hệ thống sử dụng:

* JWT Authentication
* Spring Security
* BCryptPasswordEncoder

Quy trình:

1. Người dùng đăng nhập.
2. Hệ thống tạo JWT Token.
3. Token được gửi trong Header của các API.
4. Spring Security xác thực và phân quyền người dùng.

---

# 👥 Tài Khoản Kiểm Thử

## Tài Khoản Quản Trị Viên (ADMIN)

| Thông tin | Giá trị                                   |
| --------- | ----------------------------------------- |
| Username  | admin                                     |
| Email     | [admin123@gmail.com](mailto:admin123@gmail.com) |
| Password  | 123456                                  |
| Role      | ADMIN                                     |

### Quyền hạn

* Quản lý giải đấu
* Quản lý lịch thi đấu
* Quản lý nài ngựa
* Quản lý ngựa đua
* Quản lý kết quả
* Quản lý người dùng
* Xem nhật ký hệ thống

---

## Tài Khoản Người Dùng (USER)

| Thông tin | Giá trị                                 |
| --------- | --------------------------------------- |
| Username  | chungua_anbinh                                    |
| Email     | [anbinhstable@gmail.com](mailto:user@gmail.com) |
| Password  | 123456                                 |
| Role      | USER                                    |

### Quyền hạn

* Xem thông tin giải đấu
* Xem lịch thi đấu
* Xem kết quả thi đấu
* Theo dõi bảng xếp hạng
* Đăng ký tham gia giải đấu

---

# 📋 Chức Năng Chính

## Quản Lý Giải Đấu

* Tạo giải đấu
* Cập nhật giải đấu
* Hủy giải đấu
* Theo dõi trạng thái giải đấu

## Quản Lý Lịch Thi Đấu

* Tạo lịch thi đấu
* Cập nhật lịch thi đấu
* Quản lý vòng đua

## Quản Lý Ngựa Đua

* Thêm ngựa đua
* Chỉnh sửa thông tin
* Theo dõi thành tích

## Quản Lý Nài Ngựa

* Thêm nài ngựa
* Cập nhật hồ sơ
* Theo dõi thành tích

## Quản Lý Kết Quả

* Cập nhật kết quả thi đấu
* Xếp hạng thành tích
* Thống kê giải đấu

## Quản Lý Người Dùng

* Đăng ký
* Đăng nhập
* Phân quyền
* Quản lý hồ sơ cá nhân

---

# 🏗️ Công Nghệ Sử Dụng

## Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* JWT Authentication
* Maven

## Frontend

* ReactJS
* Vite
* Tailwind CSS
* Axios
* React Router

## Database

* MySQL 8

---

# 🔒 Bảo Mật

Hệ thống áp dụng các cơ chế bảo mật:

* JWT Authentication
* BCrypt Password Hashing
* Spring Security Authorization
* REST API Protection
* Role-Based Access Control (RBAC)

Lưu ý: Mật khẩu người dùng không được lưu dưới dạng văn bản thuần (Plain Text) mà được mã hóa bằng BCrypt trước khi lưu vào cơ sở dữ liệu.

---

# 👨‍💻 Nhóm Thực Hiện

**Môn học:** Công nghệ Phần mềm (CNPM)

**Dự án:** Horse Racing Management System

**Nhóm:** nhóm 3

---

# 📄 Giấy Phép

Dự án được phát triển phục vụ mục đích học tập và nghiên cứu trong môn học Công nghệ Phần mềm.
