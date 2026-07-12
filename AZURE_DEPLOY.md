# Hướng dẫn deploy dự án lên Azure

## 1. Phương án đề nghị

Với cấu trúc hiện tại, cách deploy đơn giản và phù hợp nhất là:
- Backend: Azure App Service cho Spring Boot
- Frontend: Azure Static Web Apps hoặc Azure App Service (Nginx)
- Database: Azure Database for MySQL

Nếu bạn muốn deploy nhanh nhất, ưu tiên:
- Backend trên Azure App Service
- Frontend trên Azure Static Web Apps
- MySQL trên Azure Database for MySQL

## 2. Yêu cầu trước khi deploy

### Backend
- Java 17
- Spring Boot 3.3
- Dùng biến môi trường cho cấu hình DB/JWT/CORS

### Frontend
- Build bằng Vite
- Cần cung cấp biến môi trường:
  - VITE_API_URL
  - VITE_WS_URL
  - VITE_APP_NAME

## 3. Cấu hình biến môi trường cho backend

Trên Azure App Service, thêm các biến môi trường sau:

- SERVER_PORT=8080
- DB_HOST=<your-mysql-host>
- DB_PORT=3306
- DB_NAME=horse_racing
- DB_USERNAME=<your-db-user>
- DB_PASSWORD=<your-db-password>
- JWT_SECRET=<your-long-secret>
- JWT_EXPIRATION=86400000
- JWT_REFRESH_EXPIRATION=604800000
- CORS_ALLOWED_ORIGINS=https://<frontend-domain>,http://localhost:3000
- UPLOAD_DIR=uploads

## 4. Cấu hình database

Khuyến nghị dùng Azure Database for MySQL Flexible Server.

Các bước cần làm:
1. Tạo MySQL Flexible Server trên Azure
2. Tạo database tên horse_racing
3. Cho phép kết nối từ App Service tới MySQL
4. Cập nhật biến môi trường DB_* trên backend

## 5. Deploy backend

### Option A: Deploy từ GitHub Actions
Tạo workflow build + deploy backend lên Azure App Service.

Ví dụ các bước chính:
1. Push code lên GitHub
2. Tạo Azure App Service Plan + Web App
3. Cấu hình deployment từ GitHub Actions
4. Dùng Maven build để tạo file jar

### Option B: Deploy thủ công bằng Maven
```bash
cd backend
mvn clean package
```
Sau đó upload file jar tới Azure App Service hoặc dùng Azure CLI.

## 6. Deploy frontend

### Nếu dùng Azure Static Web Apps
- Kết nối repo GitHub
- Build command: npm run build
- Output folder: dist
- Environment variables:
  - VITE_API_URL=https://<backend-domain>/api/v1
  - VITE_WS_URL=https://<backend-domain>/ws
  - VITE_APP_NAME=Horse Racing Tournament

### Nếu dùng Azure App Service cho frontend
- Build Docker image hoặc deploy dist static files
- Cấu hình Nginx để serve SPA

## 7. Lưu ý quan trọng

- Backend hiện đang dùng context-path /api/v1 nên frontend cần gọi đúng URL này
- WebSocket nếu có dùng STOMP/SockJS thì cần expose đúng endpoint /ws
- Upload file cần lưu ở persistent storage hoặc Azure Blob Storage để không bị mất khi App Service scale out
- Nếu dùng Azure App Service, nên cấu hình file upload sang Azure Blob Storage cho production

## 8. Mức độ phù hợp

- Demo nhanh: Azure App Service + Azure Database for MySQL
- Production hơn: Azure Container Apps + Azure Database for MySQL + Azure Blob Storage

## 9. Khuyến nghị tốt nhất

Nếu bạn muốn deploy ngay trong hôm nay, làm theo thứ tự:
1. Tạo Azure Database for MySQL
2. Tạo Azure App Service cho backend
3. Tạo Azure Static Web Apps cho frontend
4. Cấu hình biến môi trường
5. Test API và login

Nếu bạn muốn, tôi có thể tiếp tục giúp bạn tạo luôn file GitHub Actions để deploy backend và frontend lên Azure tự động.
