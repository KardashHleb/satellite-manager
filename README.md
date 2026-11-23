
A multi-module Gradle project: the **server** module (mission control center, REST, Swagger) and **mission-service** (a client of the center, configured with `SERVER_URL`).

Detailed instructions on Docker, environment variables and how to check that everything works are in [README-DOCKER.md](README-DOCKER.md).

Default ports: center — **8082**, mission service — **8083** (see `server/.../application.properties` and `mission-service/.../application.properties`).
