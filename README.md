
Мультимодульный Gradle-проект: модуль **server** (центр управления, REST, Swagger) и **mission-service** (клиент к центру по `SERVER_URL`).

Подробные инструкции по Docker, переменным окружения и проверке работы — в [README-DOCKER.md](README-DOCKER.md).

Порты по умолчанию: центр — **8082**, сервис миссий — **8083** (см. `server/.../application.properties` и `mission-service/.../application.properties`).
