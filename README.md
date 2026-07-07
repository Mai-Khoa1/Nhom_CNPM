# 🐎 Horse Racing Tournament Management System

Hệ thống quản lý giải đua ngựa đa Ban tổ chức (multi-tenant) — hỗ trợ nhiều đơn vị tổ chức giải cùng vận hành độc lập trên một hệ thống dùng chung, cho phép chủ ngựa quản lý hồ sơ ngựa/nài và đăng ký thi đấu, và công khai lịch thi đấu, kết quả, bảng xếp hạng cho khán giả.

Dự án môn học **Công nghệ Phần mềm (CNPM)** — Nhóm 3, Khoa Công nghệ Thông tin.


---

## 📖 Giới thiệu

**Horse Racing Tournament Management System** thay thế quy trình quản lý thủ công/Excel của các Ban tổ chức giải đua ngựa bằng một nền tảng web tập trung. Điểm khác biệt cốt lõi của hệ thống:

- **Đa Ban tổ chức (multi-tenancy):** nhiều Ban tổ chức cùng dùng chung hệ thống nhưng dữ liệu mùa giải, cuộc đua, đăng ký của mỗi Ban tổ chức hoàn toàn tách biệt, được cách ly ngay ở tầng backend.
- **Hồ sơ tách biệt khỏi phê duyệt:** Chủ ngựa tự do quản lý hồ sơ Ngựa/Nài ngựa của mình, không cần chờ duyệt; việc phê duyệt chỉ diễn ra ở cấp từng **lần đăng ký thi đấu** gửi tới một Ban tổ chức cụ thể.
- **Minh bạch và bảo mật:** tự động tính điểm/xếp hạng theo thời gian thực, ghi nhật ký toàn bộ thao tác quan trọng, và ẩn các trường thông tin nhạy cảm (liên hệ, hồ sơ sức khỏe, giấy phép) khỏi khán giả.

## ✨ Tính năng chính

| Vai trò | Tính năng |
|---|---|
| **Khán giả** (không cần đăng nhập) | Tra cứu lịch thi đấu, kết quả, bảng xếp hạng; xem hồ sơ công khai của ngựa/nài đã có đăng ký được duyệt (ẩn thông tin nhạy cảm) |
| **Chủ ngựa** | Tạo/sửa/xóa hồ sơ Ngựa, Nài ngựa không cần phê duyệt; gửi đăng ký thi đấu (kèm tệp đính kèm) tới Ban tổ chức bất kỳ; tự hủy đăng ký; theo dõi thông báo, lịch sử thi đấu |
| **Ban tổ chức** | Quản lý Mùa giải/Cuộc đua của riêng mình; duyệt/từ chối/loại đăng ký thi đấu; phân làn đua; nhập và công bố kết quả; duyệt yêu cầu cập nhật hồ sơ liên quan tới các đăng ký mình đang quản lý |
| **Quản trị viên** | Toàn quyền trên mọi Ban tổ chức; quản lý tài khoản người dùng và phân quyền; giám sát nhật ký hoạt động toàn hệ thống |

Các module nghiệp vụ: Tài khoản & phân quyền · Ban tổ chức · Mùa giải & Cuộc đua · Hồ sơ Ngựa/Nài ngựa · Đăng ký thi đấu & Yêu cầu cập nhật · Phân làn đua · Kết quả thi đấu & Tính điểm · Bảng xếp hạng · Tệp đính kèm · Thông báo · Nhật ký hoạt động.

## 🏗️ Công nghệ sử dụng

### Backend

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.3 |
| Bảo mật | Spring Security + JWT (`jjwt`) |
| Truy xuất dữ liệu | Spring Data JPA + Hibernate |
| Validation | Spring Validation (Jakarta Bean Validation) |
| Cơ sở dữ liệu | MySQL 8 (`mysql-connector-j`) |
| Tài liệu API | Springdoc OpenAPI (Swagger UI) |
| Build tool | Maven |
| Khác | Lombok |

### Frontend

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | TypeScript |
| Framework | React 18 |
| Build tool | Vite |
| Định tuyến | React Router DOM |
| Quản lý state server | TanStack Query (React Query) |
| Quản lý state client | Zustand |
| Form & validate | React Hook Form + Zod |
| UI component | Radix UI (shadcn/ui), Tailwind CSS, lucide-react |
| Gọi API | Axios |
| Thông báo (toast) | Sonner |
| Biểu đồ | Recharts |

### Kiểm thử

| Thành phần | Công nghệ |
|---|---|
| Backend | JUnit 5, Mockito, AssertJ, Spring Security Test |
| Frontend | TypeScript compiler (type-check), ESLint |

## 🏛️ Kiến trúc hệ thống

Mô hình Client - Server tách biệt hoàn toàn:

```text
┌──────────────────┐        REST API (JWT)        ┌──────────────────┐        JDBC        ┌──────────────┐
│   Frontend SPA    │  ───────────────────────────▶ │   Backend API     │ ─────────────────▶ │  MySQL 8      │
│  React + Vite     │ ◀─────────────────────────── │  Spring Boot 3     │ ◀───────────────── │  Database     │
└──────────────────┘                                └──────────────────┘                     └──────────────┘
```

Backend theo kiến trúc phân lớp chuẩn của Spring Boot:

```text
Controller  →  Service  →  Repository  →  Entity (JPA)
   │              │
   ▼              ▼
  DTO      Business rule / Multi-tenancy scope
```

- **Đa Ban tổ chức:** mỗi request từ vai trò Ban tổ chức được gắn `organizerScopeId` ngay tại tầng service, mọi truy vấn danh sách/chi tiết đều lọc theo đúng phạm vi này; truy cập chéo dữ liệu của Ban tổ chức khác bị chặn bằng lỗi 403.
- **Xác thực:** JWT access token (ngắn hạn) + refresh token (lưu DB, có thể thu hồi), không dùng session server-side.
- **Khởi tạo dữ liệu:** `schema.sql`/`data.sql` chạy tự động mỗi lần backend khởi động (idempotent), không cần công cụ migration riêng.

## 📂 Cấu trúc dự án

```text
Nhom_CNPM/
├── backend/                           # Backend Spring Boot
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/horseracing/
│       │   │   ├── HorseRacingApplication.java
│       │   │   ├── config/            # Cấu hình Security, JWT, CORS
│       │   │   ├── controller/        # REST API Controllers
│       │   │   ├── dto/               # Request/Response DTO
│       │   │   ├── entity/            # JPA Entities
│       │   │   ├── exception/         # Exception dùng riêng + Global Exception Handler
│       │   │   ├── repository/        # Spring Data JPA Repositories
│       │   │   └── service/           # Business logic
│       │   └── resources/
│       │       ├── application.properties
│       │       ├── schema.sql         # Cấu trúc bảng (idempotent)
│       │       └── data.sql           # Dữ liệu mẫu (idempotent)
│       └── test/java/com/horseracing/ # Unit test (JUnit + Mockito)
│
├── frontend/                          # Frontend React + Vite
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.ts
│   └── src/
│       ├── api/                       # Axios client theo từng module
│       ├── components/                # UI component dùng chung
│       ├── hooks/                     # Custom React hooks
│       ├── layouts/                   # MainLayout / DashboardLayout / AdminLayout
│       ├── pages/                     # Trang theo vai trò (public / horse-owner / admin)
│       ├── schemas/                   # Zod schema cho form
│       ├── store/                     # Zustand store
│       ├── types/                     # Type định nghĩa request/response
│       └── utils/                     # Hàm tiện ích chung
│
└── README.md
```

## 🛠️ Yêu cầu hệ thống

| Thành phần | Phiên bản tối thiểu |
|---|---|
| Java JDK | 17+ |
| Apache Maven | 3.x |
| Node.js | 18 hoặc 20 |
| npm | đi kèm Node.js (hoặc pnpm/yarn) |
| MySQL Server | 8.0+ |

## 🚀 Cài đặt và khởi chạy

### 1. Backend (Spring Boot)

```bash
cd backend
```

Cấu hình kết nối database tại `src/main/resources/application.properties`, hoặc ghi đè bằng biến môi trường thay vì sửa file trực tiếp:

```bash
DB_HOST=localhost DB_PORT=3306 DB_NAME=horse_racing DB_USERNAME=root DB_PASSWORD=your_mysql_password
```

Không cần tạo database/bảng thủ công — khi backend khởi động, `schema.sql` tự tạo cấu trúc bảng (`CREATE TABLE IF NOT EXISTS` / `ALTER TABLE` idempotent) và `data.sql` tự nạp dữ liệu mẫu (`INSERT IGNORE`) nếu chưa có.

```bash
mvn spring-boot:run
```

API chạy tại `http://localhost:8080/api/v1` (tiền tố `/api/v1` cấu hình qua `server.servlet.context-path`).

### 2. Frontend (React)

```bash
cd frontend
npm install
npm run dev
```

Frontend chạy tại `http://localhost:3000` (cấu hình trong `vite.config.ts`, đổi được qua biến môi trường `VITE_PORT`).

Build production:

```bash
npm run build
```

## 👥 Tài khoản demo

Dữ liệu mẫu có sẵn trong `data.sql`, mật khẩu chung là `123456`:

| Vai trò | Username | Ghi chú |
|---|---|---|
| ADMIN | `admin` | Toàn quyền hệ thống |
| ORGANIZER | `btc_lananh` | Ban tổ chức #1 |
| ORGANIZER | `btc_minhtuan` | Ban tổ chức #2 — dùng cùng lúc với BTC #1 để kiểm thử cách ly dữ liệu đa Ban tổ chức |
| HORSE_OWNER | `chungua_anbinh` | Chủ ngựa |

Đăng ký tài khoản mới qua `POST /auth/register` sẽ luôn tạo vai trò `HORSE_OWNER`; vai trò `ORGANIZER`/`SPECTATOR` do ADMIN gán qua chức năng Quản lý người dùng.

## 📋 Tài liệu API

| Nhóm chức năng | Base path |
|---|---|
| Xác thực | `/auth` |
| Ban tổ chức | `/organizers` |
| Mùa giải | `/seasons` |
| Cuộc đua | `/races` |
| Ngựa đua | `/horses` |
| Nài ngựa | `/jockeys` |
| Chủ ngựa | `/chu-ngua` |
| Đăng ký thi đấu | `/registrations` |
| Yêu cầu cập nhật hồ sơ | `/update-requests` |
| Làn đua | `/lanes` |
| Kết quả thi đấu | `/results` |
| Bảng xếp hạng | `/rankings` |
| Người dùng | `/users` |
| Thông báo | `/notifications` |
| Nhật ký hoạt động | `/nhat-ky-hoat-dong` |
| Tệp đính kèm | `/upload` |
| Thống kê tổng quan | `/dashboard` |

Tài liệu API đầy đủ (Swagger UI) có sẵn khi backend đang chạy tại `http://localhost:8080/api/v1/swagger-ui.html`.

## 🧪 Kiểm thử

```bash
cd backend
mvn test
```

```bash
cd frontend
npx tsc --noEmit   # type-check
npm run lint
```

## 👨‍💻 Nhóm thực hiện

**Môn học:** Công nghệ Phần mềm (CNPM)
**Dự án:** Horse Racing Tournament Management System
**Nhóm:** Nhóm 3 — Khoa Công nghệ Thông tin

## 📄 Giấy phép

Dự án được phát triển phục vụ mục đích học tập và nghiên cứu trong môn học Công nghệ Phần mềm.
