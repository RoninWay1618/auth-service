# Auth Service

Сервис авторизации с JWT на Spring Boot.

## Запуск через Docker Compose

1. Соберите JAR:
   ```bash
   mvn clean package
2. Запустите:
    ```bash
    docker-compose up -d --build
3. Запустите команды в файле data.sql
4. Сервис доступен на http://localhost:8080.

## Эндпоинты

- POST http://localhost:8080/auth/register
    ```json
    { 
    "username": "john_doe",
    "password": "P@ssw0rd!",
    "email": "john.doe@example.com" 
    }
- POST http://localhost:8080/auth/login
    ```json
    {
    "username": "john_doe",
    "password": "P@ssw0rd!"
    }
- POST http://localhost:8080/auth/refresh
    ```json
    {
    "refreshToken": "<REFRESH_TOKEN>"
    }
- POST http://localhost:8080/auth/logout
    ```json
    {
    "refreshToken": "<REFRESH_TOKEN>"
    }

## Конфигурация
Настройки в application.yaml:
   ```yaml
        jwt.secret=YourVerySecretKeyChangeThis
        jwt.access.expiration=900000
        jwt.refresh.expiration=604800000
