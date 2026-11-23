/**
 * ============================================================================
 * Script 1: Tests for the server request -> GET /api/overview
 * ============================================================================
 * What it does:
 * - Checks the HTTP status 200
 * - Checks that the response is not empty
 * - Saves the response body to the collection variable `overviewFromServer`
 *   (the second script needs this variable)
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
 * Script 2: Tests for the mission-service request -> GET /api/remote/overview
 * ============================================================================
 * What it does:
 * - Checks the HTTP status 200
 * - Checks that the response is not empty
 * - Compares the mission-service response with the server response saved earlier
 *   and confirms that the proxying works correctly
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
 * Quick variable setup in Postman
 * ============================================================================
 * Create Collection Variables:
 * - serverBaseUrl  = http://localhost:8082
 * - missionBaseUrl = http://localhost:8083
 *
 * Then the request URLs are:
 * - {{serverBaseUrl}}/api/overview
 * - {{missionBaseUrl}}/api/remote/overview
 */
