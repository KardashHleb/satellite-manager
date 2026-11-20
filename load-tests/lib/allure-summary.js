import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.2/index.js';

function randomId() {
  return `${Date.now()}-${Math.random().toString(16).slice(2, 10)}`;
}

function groupSteps(group, steps) {
  if (group.checks) {
    const checks = Object.entries(group.checks);
    if (checks.length > 0) {
      const failed = checks.some(([, value]) => value.fails > 0);
      steps.push({
        name: group.name,
        status: failed ? 'failed' : 'passed',
        stage: 'finished',
        start: Date.now(),
        stop: Date.now(),
      });
    }
  }

  if (group.groups) {
    group.groups.forEach((child) => groupSteps(child, steps));
  }
}

function buildMetricsAttachment(data) {
  const metrics = data.metrics || {};
  return {
    profile: __ENV.PROFILE || 'load10',
    baseUrl: __ENV.BASE_URL || 'http://localhost:8082',
    state: data.state,
    durationMs: (metrics.iteration_duration && metrics.iteration_duration.values.avg) || null,
    httpReqDurationP95:
      (metrics.http_req_duration && metrics.http_req_duration.values['p(95)']) || null,
    httpReqFailedRate:
      (metrics.http_req_failed && metrics.http_req_failed.values.rate) || null,
    iterations: (metrics.iterations && metrics.iterations.values.count) || null,
    vusMax: (metrics.vus_max && metrics.vus_max.values.max) || null,
  };
}

export function buildAllureSummary(data) {
  const steps = [];
  groupSteps(data.root_group, steps);

  const hasFailedStep = steps.some((step) => step.status === 'failed');
  const httpFailedRate =
    (data.metrics.http_req_failed && data.metrics.http_req_failed.values.rate) || 0;
  const status = hasFailedStep || httpFailedRate > 0 ? 'failed' : 'passed';

  const resultId = randomId();
  const attachmentId = `${resultId}-metrics.json`;
  const profile = __ENV.PROFILE || 'load10';

  const result = {
    uuid: resultId,
    historyId: `load-tests-${profile}`,
    name: `Operator constellation scenario (${profile})`,
    fullName: `load-tests.scenario.${profile}`,
    status,
    stage: 'finished',
    steps,
    attachments: [
      {
        name: 'k6-metrics',
        source: attachmentId,
        type: 'application/json',
      },
    ],
    labels: [
      { name: 'suite', value: 'load-tests' },
      { name: 'framework', value: 'k6' },
      { name: 'feature', value: 'constellation-operator' },
    ],
    start: Date.now(),
    stop: Date.now(),
  };

  return {
    [`allure-results/${resultId}-result.json`]: JSON.stringify(result, null, 2),
    [`allure-results/${attachmentId}`]: JSON.stringify(buildMetricsAttachment(data), null, 2),
  };
}

export function handleSummary(data) {
  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    ...buildAllureSummary(data),
  };
}
