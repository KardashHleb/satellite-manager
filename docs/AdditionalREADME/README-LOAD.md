# Satellite Manager


## Запуск приложения

```bash
docker compose up --build
```

API: http://localhost:8082/swagger-ui/index.html

## Нагрузочное тестирование (k6 + Allure)

**Требуется:**

- Docker
- k6
- Node.js
- Java





При первом запуске k6 загружает официальную утилиту `textSummary` с CDN Grafana:
`https://jslib.k6.io/k6-summary/0.0.2/index.js` (см. `load-tests/lib/allure-summary.js`).
Она форматирует итоговую сводку теста в консоль; Allure-файлы пишутся отдельно.
Нужен интернет при первом прогоне (далее k6 может использовать кэш).

Перед командами ниже перейдите в `load-tests` (из корня репозитория):

```bash
cd load-tests
```
### Подготовка (один раз)

```bash
npm install
```
`npm install` скачивает **Allure CLI** в `node_modules` (пакет `allure-commandline`). Он нужен для `npm run serve` и `npm run report` — глобально Allure ставить не нужно. Для работы Allure как и для запуска приложения требуется Java.

Далее воспользуйтесь одной из следующих команд для запуска тестирования

```bash
npm run test:1      # 1 пользователь, 30 с
npm run test:2      # 2 пользователя, 30 с
npm run test:10     # разгон до 10, удержание 60 с
npm run report      # статический HTML в allure-report/
npm run serve       # allure serve — отчёт в браузере (из allure-results/)
npm run test:all    # test:10 + serve (отчёт в браузере)
```

Другой URL сервера (тоже из `load-tests`):

```bash
cd load-tests
k6 run -e BASE_URL=http://127.0.0.1:8082 -e PROFILE=load10 scenario.js
```

Подробности по Docker — [README-DOCKER.md](docs/AdditionalREADME/README-DOCKER.md).


## Пользовательский сценарий (нагрузочный тест)

Оператор центра управления проходит цепочку действий из `load-tests/scenario.js`. Каждый шаг — отдельная k6-группа и вызов REST API:

| Шаг | Действие оператора | API |
|-----|-------------------|-----|
| 1 | Обзор системы | `GET /api/overview` |
| 2 | Просмотр группировки RU Basic | `GET /api/constellations/RU Basic` |
| 3 | Создание группировки | `POST /api/crud/constellations` |
| 4 | Добавление спутника в группировку | `POST /api/crud/satellites` |
| 5 | Переименование группировки | `PUT /api/crud/constellations/{name}` |
| 6 | Удаление спутника | `DELETE /api/crud/satellites/{id}` |
| 7 | Удаление группировки | `DELETE /api/crud/constellations/{name}` |

На шагах 3–7 каждый виртуальный пользователь работает со своей группировкой (`k6-c-{VU}-{ITER}`); переименование выполняется перед удалением, чтобы проверить обновление по новому имени.

Для запросов с телом используется заголовок `Content-Type: application/json`.

### 1. Обзор системы

`GET /api/overview`

Тело запроса не требуется.

**Ответ:** `200 OK`, текстовый отчёт по всем группировкам.

```text
=== Space Operation Center Overview ===

=== Статус группировки 'RU Basic' ===
Всего спутников: 5
...
```

### 2. Просмотр группировки RU Basic

`GET /api/constellations/RU%20Basic`

**Ответ:** `200 OK`, JSON группировки (поле `satellites` может быть пустым в сериализации из‑за lazy-связей):

```json
{
  "id": 1,
  "constellationName": "RU Basic",
  "satellites": []
}
```

При отсутствии группировки — `404 Not Found`.

### 3. Создание группировки

`POST /api/crud/constellations`

```json
{
  "constellationName": "k6-c-1-0"
}
```

**Ответ:** `201 Created`

```json
{
  "id": 2,
  "constellationName": "k6-c-1-0",
  "satellites": []
}
```

### 4. Добавление спутника

`POST /api/crud/satellites`

```json
{
  "constellationName": "k6-c-1-0",
  "satelliteParam": {
    "type": "COMMUNICATION",
    "name": "k6-s-1-0",
    "batteryLevel": 0.8,
    "bandwidth": 500
  }
}
```

**Ответ:** `201 Created` — в теле возвращается созданный спутник; поле `id` используется на шаге 6:

```json
{
  "id": 42,
  "name": "k6-s-1-0",
  "temperatureInside": null,
  "temperatureOutside": null,
  "bandwidth": 500.0,
  "dataSent": 0.0
}
```

### 5. Переименование группировки

`PUT /api/crud/constellations/k6-c-1-0`

```json
{
  "newName": "k6-c-1-0-renamed"
}
```

**Ответ:** `200 OK`

```json
{
  "id": 2,
  "constellationName": "k6-c-1-0-renamed",
  "satellites": []
}
```

### 6. Удаление спутника

`DELETE /api/crud/satellites/42`

Тело запроса не требуется (`42` — `id` из шага 4).

**Ответ:** `204 No Content`

### 7. Удаление группировки

`DELETE /api/crud/constellations/k6-c-1-0-renamed`

Тело запроса не требуется (имя после переименования из шага 5).

**Ответ:** `204 No Content`
