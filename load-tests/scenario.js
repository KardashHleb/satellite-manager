import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { handleSummary } from './lib/allure-summary.js';

export { handleSummary };

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8082';
const SEED_CONSTELLATION = 'RU Basic';

const profiles = {
  smoke1: {
    vus: 1,
    duration: '30s',
    thresholds: {
      http_req_failed: ['rate<0.01'],
    },
  },
  smoke2: {
    vus: 2,
    duration: '30s',
    thresholds: {
      http_req_failed: ['rate<0.01'],
    },
  },
  load10: {
    stages: [
      { duration: '15s', target: 10 },
      { duration: '60s', target: 10 },
      { duration: '10s', target: 0 },
    ],
    thresholds: {
      http_req_failed: ['rate<0.05'],
      http_req_duration: ['p(95)<3000'],
    },
  },
};

export const options = profiles[__ENV.PROFILE || 'load10'];

function jsonHeaders() {
  return { headers: { 'Content-Type': 'application/json' } };
}

export default function operatorScenario() {
  const constellationName = `k6-c-${__VU}-${__ITER}`;
  const renamedConstellation = `${constellationName}-renamed`;
  const satelliteName = `k6-s-${__VU}-${__ITER}`;

  group('01 Overview', () => {
    const res = http.get(`${BASE_URL}/api/overview`);
    check(res, {
      'overview status 200': (r) => r.status === 200,
      'overview has content': (r) => r.body && r.body.length > 0,
    });
  });

  group('02 Constellation details', () => {
    const res = http.get(
      `${BASE_URL}/api/constellations/${encodeURIComponent(SEED_CONSTELLATION)}`
    );
    check(res, {
      'constellation status 200': (r) => r.status === 200,
    });
  });

  group('03 Create constellation', () => {
    const res = http.post(
      `${BASE_URL}/api/crud/constellations`,
      JSON.stringify({ constellationName }),
      jsonHeaders()
    );
    check(res, {
      'create constellation status 201': (r) => r.status === 201,
    });
  });

  let satelliteId = null;

  group('04 Add satellite', () => {
    const body = {
      constellationName,
      satelliteParam: {
        type: 'COMMUNICATION',
        name: satelliteName,
        batteryLevel: 0.8,
        bandwidth: 500,
      },
    };
    const res = http.post(
      `${BASE_URL}/api/crud/satellites`,
      JSON.stringify(body),
      jsonHeaders()
    );
    const ok = check(res, {
      'add satellite status 201': (r) => r.status === 201,
      'add satellite returns id': (r) => r.json('id') !== undefined,
    });
    if (ok) {
      satelliteId = res.json('id');
    }
  });

  group('05 Rename constellation', () => {
    const res = http.put(
      `${BASE_URL}/api/crud/constellations/${encodeURIComponent(constellationName)}`,
      JSON.stringify({ newName: renamedConstellation }),
      jsonHeaders()
    );
    check(res, {
      'rename constellation status 200': (r) => r.status === 200,
    });
  });

  group('06 Delete satellite', () => {
    if (satelliteId === null) {
      check(null, { 'satellite id available': () => false });
      return;
    }
    const res = http.del(`${BASE_URL}/api/crud/satellites/${satelliteId}`);
    check(res, {
      'delete satellite status 204': (r) => r.status === 204,
    });
  });

  group('07 Delete constellation', () => {
    const res = http.del(
      `${BASE_URL}/api/crud/constellations/${encodeURIComponent(renamedConstellation)}`
    );
    check(res, {
      'delete constellation status 204': (r) => r.status === 204,
    });
  });

  sleep(0.3);
}
