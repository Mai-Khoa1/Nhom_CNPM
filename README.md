# 🐎 HỆ THỐNG QUẢN LÝ ĐUA NGỰA (HORSE RACING MANAGEMENT)

## 📖 Giới thiệu

Hệ thống Quản lý Đua Ngựa (Horse Racing Management System) là dự án môn học **Công nghệ Phần mềm (CNPM)** được thực hiện bởi nhóm 3 - Khoa Công nghệ Thông tin.

Hệ thống được xây dựng theo mô hình Client - Server gồm:

* **Backend:** Spring Boot 3 (Java 17)
* **Frontend:** ReactJS + Vite + TypeScript
* **Database:** MySQL 8
* **Authentication:** JWT (Access Token + Refresh Token)
* **Password Encryption:** BCryptPasswordEncoder

Mục tiêu của hệ thống là hỗ trợ quản lý mùa giải, chặng đua, nài ngựa, ngựa đua, đăng ký thi đấu, kết quả thi đấu, bảng xếp hạng và người dùng tham gia hệ thống.

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
│       │   ├── entity/                # Database Entities (JPA)
│       │   ├── exception/             # Global Exception Handler
│       │   ├── repository/            # JPA Repository Layer
│       │   └── service/               # Business Logic Layer
│       │
│       └── resources/
│           ├── application.properties
│           ├── schema.sql             # Tạo cấu trúc bảng (idempotent)
│           └── data.sql               # Dữ liệu mẫu (idempotent)
│
├── frontend/                          # Frontend ReactJS + Vite
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.ts
│   ├── index.html
│   └── src/
│       ├── api/                       # Axios API client theo từng module
│       ├── pages/                     # Trang public + admin
│       ├── layouts/                   # MainLayout / AdminLayout
│       └── types/                     # Type định nghĩa request/response
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

Mặc định backend kết nối tới `jdbc:mysql://localhost:3306/horse_racing` với user `root`. Có thể ghi đè bằng biến môi trường thay vì sửa file trực tiếp:

```bash
DB_HOST=localhost DB_PORT=3306 DB_NAME=horse_racing DB_USERNAME=root DB_PASSWORD=your_mysql_password
```

### Bước 3: Khởi tạo Database

Khi ứng dụng chạy lần đầu (và mỗi lần chạy sau đó):

* `schema.sql` sẽ tạo cấu trúc bảng nếu chưa tồn tại (`CREATE TABLE IF NOT EXISTS`).
* `data.sql` sẽ nạp dữ liệu mẫu nếu chưa có (`INSERT IGNORE`).

Không cần tạo dữ liệu thủ công, database `horse_racing` sẽ được tự tạo nếu chưa có.

### Bước 4: Chạy Backend

```bash
mvn spring-boot:run
```

Sau khi khởi động thành công, API sẽ chạy tại:

```text
http://localhost:8080/api/v1
```

> Lưu ý: toàn bộ API đều có tiền tố `/api/v1` (cấu hình `server.servlet.context-path`).

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
http://localhost:3000
```

(Cổng được cấu hình trong `vite.config.ts`, có thể đổi bằng biến môi trường `VITE_PORT`.)

### Build production

```bash
npm run build
```

---

# 🔐 Xác Thực Và Phân Quyền

Hệ thống sử dụng:

* JWT Authentication (Access Token + Refresh Token)
* Spring Security
* BCryptPasswordEncoder

Quy trình:

1. Người dùng đăng nhập qua `POST /api/v1/auth/login`.
2. Hệ thống tạo JWT Access Token + Refresh Token.
3. Access Token được gửi trong header `Authorization: Bearer <token>` của các API tiếp theo.
4. Spring Security xác thực và phân quyền theo vai trò (ADMIN / ORGANIZER / HORSE_OWNER).

---

# 👥 Tài Khoản Kiểm Thử (dữ liệu mẫu trong `data.sql`)

## Tài Khoản Quản Trị Viên (ADMIN)

| Thông tin | Giá trị             |
| --------- | -------------------- |
| Username  | admin                |
| Email     | admin123@gmail.com   |
| Password  | 123456                |
| Role      | ADMIN                |

### Quyền hạn

* Quản lý mùa giải, chặng đua
* Quản lý nài ngựa, ngựa đua (duyệt/từ chối/loại)
* Quản lý đăng ký thi đấu và làn đua
* Ghi nhận và công bố kết quả thi đấu
* Quản lý người dùng
* Xem nhật ký hoạt động hệ thống

---

## Tài Khoản Chủ Ngựa (HORSE_OWNER)

| Thông tin | Giá trị                  |
| --------- | -------------------------- |
| Username  | chungua_anbinh             |
| Email     | anbinhstable@gmail.com     |
| Password  | 123456                      |
| Role      | HORSE_OWNER                 |

### Quyền hạn

* Đăng ký ngựa đua, nài ngựa
* Đăng ký tham gia chặng đua
* Xem lịch thi đấu, kết quả thi đấu, bảng xếp hạng
* Quản lý hồ sơ cá nhân

---

# 📋 Chức Năng Chính & API

| Chức năng | Base path | Controller |
| --- | --- | --- |
| Xác thực | `/auth` | `AuthController` |
| Mùa giải | `/seasons` | `MuaGiaiController` |
| Chặng đua | `/races` | `ScheduleController` |
| Ngựa đua | `/horses` | `NguaController` |
| Nài ngựa (Jockey) | `/jockeys` | `NaiNguaController` |
| Chủ ngựa | `/chu-ngua` | `ChuNguaController` |
| Đăng ký thi đấu | `/registrations` | `RegistrationController` |
| Làn đua | `/lanes` | `LaneController` |
| Kết quả thi đấu | `/results` | `ResultController` |
| Bảng xếp hạng | `/rankings` | `RankingController` |
| Người dùng | `/users` | `UserController` |
| Thông báo | `/notifications` | `NotificationController` |
| Nhật ký hoạt động | `/nhat-ky-hoat-dong` | `NhatKyHoatDongController` |
| Tải file | `/upload` | `UploadController` |
| Thống kê tổng quan | `/dashboard` | `DashboardController` |

Tài liệu API chi tiết (Swagger UI) có sẵn khi backend đang chạy:

```text
http://localhost:8080/api/v1/swagger-ui.html
```

---

# 🏗️ Công Nghệ Sử Dụng

## Backend

* Java 17
* Spring Boot 3
* Spring Security
* Spring Data JPA + Hibernate
* JWT Authentication
* Springdoc OpenAPI (Swagger)
* Maven

## Frontend

* ReactJS + TypeScript
* Vite
* Tailwind CSS
* Axios
* React Router
* TanStack Query

## Database

* MySQL 8

---

# 🔒 Bảo Mật

Hệ thống áp dụng các cơ chế bảo mật:

* JWT Authentication
* BCrypt Password Hashing
* Spring Security Authorization theo Role
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
