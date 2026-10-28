# E-commerce API

[![CI](https://github.com/GiaHung2176/e-commerce/actions/workflows/ci.yml/badge.svg)](https://github.com/GiaHung2176/e-commerce/actions/workflows/ci.yml)

REST API quản lý sản phẩm và danh mục cho hệ thống bán hàng, xây dựng bằng Spring Boot. Dự án học tập và showcase backend Java.

## Demo

- Swagger UI: https://ecommerce-api-wmy8.onrender.com/swagger-ui.html
- Health check: https://ecommerce-api-wmy8.onrender.com/actuator/health

Triển khai trên Render (gói miễn phí), lưu ý:
- Sau 15 phút không có request, app tự ngủ; request đầu tiên sau đó có thể mất 30–60 giây để app khởi động lại.
- Database PostgreSQL miễn phí của Render hết hạn sau khoảng 30 ngày, khi đó demo cần tạo lại database.

## Tech stack

- Java 17
- Spring Boot 4.1.x (Spring Web MVC, Spring Data JPA, Bean Validation)
- PostgreSQL 16 (schema quản lý bằng Flyway)
- Maven
- springdoc-openapi (Swagger UI)
- Docker, Docker Compose
- GitHub Actions (CI)

## Chạy dự án

### Cách 1: Docker (một lệnh, không cần cài JDK hay database)

Yêu cầu: Docker.

1. Tạo file biến môi trường từ file mẫu rồi điền giá trị của bạn (`DB_PASSWORD`, `JWT_SECRET`). File `.env` nằm trong `.gitignore`, không commit:

   ```bash
   cp .env.example .env
   ```

2. Dựng app và database:

   ```bash
   docker compose up --build
   ```

3. Kiểm tra app: <http://localhost:8080/actuator/health> trả `{"status":"UP"}`.

### Cách 2: Chạy app trên máy (dev)

Yêu cầu: JDK 17, Docker (để chạy PostgreSQL).

1. Tạo file cấu hình từ file mẫu. Cả hai file đều nằm trong `.gitignore`:

   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
   ```

2. Mở `application.properties` điền `jwt.secret`; mở `application-dev.properties` điền `spring.datasource.password` trùng với `DB_PASSWORD` trong `.env`.

3. Bật database (cổng `5433` trên máy), giữ terminal này mở:

   ```bash
   docker compose up postgres
   ```

4. Ở terminal khác, chạy ứng dụng:

   ```bash
   ./mvnw spring-boot:run
   ```

   Trên Windows, nếu gặp lỗi `invalid value for parameter "TimeZone"`, đặt trước khi chạy (PowerShell): `$env:JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Ho_Chi_Minh"`.

### Chạy test

Test tích hợp dùng Testcontainers nên cần Docker đang chạy:

```bash
./mvnw test
```

Ứng dụng chạy tại `http://localhost:8080`.

## API documentation (Swagger)

Sau khi chạy ứng dụng:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI spec (JSON): <http://localhost:8080/v3/api-docs>

![Swagger UI](docs/images/swagger-ui.png)

## Endpoint

| Method | Path | Mô tả |
|---|---|---|
| GET | `/api/products` | Danh sách sản phẩm, có phân trang và sắp xếp |
| GET | `/api/products/{id}` | Chi tiết một sản phẩm |
| POST | `/api/products` | Tạo sản phẩm |
| PUT | `/api/products/{id}` | Cập nhật toàn bộ sản phẩm |
| PATCH | `/api/products/{id}` | Cập nhật một phần sản phẩm |
| DELETE | `/api/products/{id}` | Xóa sản phẩm |
| GET | `/api/categories` | Danh sách danh mục |
| GET | `/api/categories/{id}` | Chi tiết một danh mục |
| POST | `/api/categories` | Tạo danh mục |
| PUT | `/api/categories/{id}` | Cập nhật toàn bộ danh mục |
| PATCH | `/api/categories/{id}` | Cập nhật một phần danh mục |
| DELETE | `/api/categories/{id}` | Xóa danh mục (không xóa được nếu còn sản phẩm) |

## Xử lý lỗi

Lỗi validation trả về `400` kèm danh sách lỗi theo từng field:

```json
{
  "errors": [
    { "field": "name", "message": "Name of category not blank" }
  ],
  "message": "Validation failed",
  "status": 400,
  "timestamp": "2026-10-11T18:12:41.3587938"
}
```

## N+1 optimization for `GET /api/orders`

Measured on seed data from [docs/seed-n-plus-1.sql](docs/seed-n-plus-1.sql): 5 orders, 2 items each, 5 users, 10 distinct products, 3 categories. Each figure is the number of SQL `select` statements counted from the Hibernate log for one request (authentication queries excluded).

| Step | Change | Selects | Breakdown |
|---|---|---|---|
| Baseline | `findAll()` | 21 | 1 orders + 5 users + 5 order_items + 10 products |
| 1 | `join fetch o.user` | 21 | 1 orders + 5 profiles + 5 order_items + 10 products |
| 2 | add `join fetch o.items i join fetch i.product` | 6 | 1 combined + 5 profiles |
| 3 | add `left join fetch u.profile` | 1 | 1 combined |

Notes:

- Step 1 did not reduce the total: `User` is now loaded by a query instead of by id, so its EAGER `profile` is loaded by 5 extra queries instead of a join.
- `profile` uses `left join fetch`: an inner join would drop every order whose user has no profile (all seed users).
- `category` is not fetched because the response DTO does not read it. Reading it would add one query per distinct category (3 here).


## Cấu trúc package

Base package `com.giahung19.ecommerce_api`:

- `rest/`: controller
- `service/`: interface và `*Impl`
- `repository/`: Spring Data JPA repository
- `entity/`: JPA entity
- `dto/`: request/response DTO
- `exception/`: custom exception và exception handler
- `config/`: cấu hình (OpenAPI)

## Trạng thái

Đã có: Docker, Docker Compose, CI bằng GitHub Actions, deploy lên Render, health check qua Actuator.

Tiếp theo: ôn tập lại các phần đã học.
