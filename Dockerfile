# =============================================================
# 多阶段构建：前端(Node) -> 后端(Maven) -> 运行(JRE)
# 一条命令即可得到可运行镜像，无需本机安装 JDK/Node/Maven
# =============================================================

# ---------- 阶段 1：构建前端 ----------
FROM node:20-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm config set registry https://registry.npmmirror.com && npm install
COPY frontend/ ./
# 打包输出到 /app/backend/src/main/resources/static
RUN npm run build

# ---------- 阶段 2：构建后端 ----------
FROM maven:3.9-eclipse-temurin-8 AS backend
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn -q dependency:go-offline || true
COPY backend/ ./
# 复制前端构建产物到后端静态目录
COPY --from=frontend /app/backend/src/main/resources/static ./src/main/resources/static
RUN mvn -q clean package -DskipTests

# ---------- 阶段 3：运行 ----------
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=backend /app/backend/target/hrms.jar ./hrms.jar
EXPOSE 8080
# 默认使用内嵌 H2；如需 MySQL，通过环境变量 SPRING_PROFILES_ACTIVE=mysql 及 MYSQL_* 覆盖
ENV SPRING_PROFILES_ACTIVE=h2
ENTRYPOINT ["java", "-jar", "hrms.jar"]
