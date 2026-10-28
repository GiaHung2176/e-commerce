# Giai đoạn 1: build ra file .jar (cần JDK + Maven)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Chép pom.xml trước và tải dependency riêng, để Docker cache lại lớp này
# khi chỉ sửa code mà không đổi pom.xml
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
# Bỏ qua test vì integration test dùng Testcontainers cần Docker, mà trong lúc build image không có
RUN mvn -B package -DskipTests

# Giai đoạn 2: chỉ chạy app (chỉ cần JRE), chép đúng file .jar từ giai đoạn 1
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
