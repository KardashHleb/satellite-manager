/**
 * ============================================================================
 * Script 1: Tests для запроса server -> GET /api/overview
 * ============================================================================
 * Что делает:
 * - Проверяет HTTP-статус 200
 * - Проверяет, что ответ не пустой
 * - Сохраняет тело ответа в переменную коллекции `overviewFromServer`
 *   (эта переменная понадобится второму скрипту)
 */
const SCRIPT_1 = `
pm.test("Status code is 200", function () {
  pm.response.to.have.status(200);
});

pm.test("Response body is not empty", function () {
  const body = pm.response.text();
  pm.expect(body).to.be.a("string").and.not.empty;
});

pm.collectionVariables.set("overviewFromServer", pm.response.text());

pm.test("Saved server overview to collection variable", function () {
  pm.expect(pm.collectionVariables.get("overviewFromServer")).to.be.a("string").and.not.empty;
});
`;

/**
 * ============================================================================
 * Script 2: Tests для запроса mission-service -> GET /api/remote/overview
 * ============================================================================
 * Что делает:
 * - Проверяет HTTP-статус 200
 * - Проверяет, что ответ не пустой
 * - Сравнивает ответ mission-service с ранее сохранённым ответом server
 *   и подтверждает, что проксирование работает корректно
 */
const SCRIPT_2 = `
pm.test("Status code is 200", function () {
  pm.response.to.have.status(200);
});

pm.test("Response body is not empty", function () {
  const body = pm.response.text();
  pm.expect(body).to.be.a("string").and.not.empty;
});

pm.test("Mission response equals server response", function () {
  const fromServer = pm.collectionVariables.get("overviewFromServer");
  pm.expect(fromServer, "Run Script 1 request first to set overviewFromServer").to.be.a("string").and.not.empty;
  pm.expect(pm.response.text()).to.eql(fromServer);
});
`;

/**
 * ============================================================================
 * Быстрая настройка переменных в Postman
 * ============================================================================
 * Создай Collection Variables:
 * - serverBaseUrl  = http://localhost:8082
 * - missionBaseUrl = http://localhost:8083
 *
 * Тогда URL запросов будут:
 * - {{serverBaseUrl}}/api/overview
 * - {{missionBaseUrl}}/api/remote/overview
 */
