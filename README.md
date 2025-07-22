# Auth Service

Сервис авторизации с JWT на Spring Boot.

## Запуск через Docker Compose

1. Соберите JAR:
   ```bash
   mvn clean package
2. Запустите:
    ```bash
    docker-compose up -d --build
3. Сервис доступен на http://localhost:8080.

## Эндпоинты

- POST /api/auth/register
    ```json
    { "login":"user1", "email":"user1@example.com", "password":"123456" }
- POST /api/auth/login
    ```json
    { "login":"user1", "password":"123456" }
- POST /api/auth/refresh
    ```json
    { "refreshToken":"<uuid>" }
- POST /api/auth/logout
    ```json
    { "refreshToken":"<uuid>" }

## Конфигурация
Настройки в application.yaml:
   ```yaml
    jwt:
        secret: verySecretKeyChangeMe
        expiration-ms: 3600000
        refresh-expiration-ms: 8640000
