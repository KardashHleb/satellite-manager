# Satellite Manager

## Пользовательский сценарий (нагрузочный тест)

Оператор центра управления: обзор системы → просмотр группировки RU Basic → создание группировки → добавление спутника → переименование → удаление спутника и группировки.

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
