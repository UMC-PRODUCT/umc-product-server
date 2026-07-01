import http from "k6/http";
import {check, fail, group, sleep} from "k6";
import {Counter, Rate, Trend} from "k6/metrics";
import {SharedArray} from "k6/data";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const USERS_FILE = __ENV.USERS_FILE || "../../data/users.local.json";
const TARGET_VUS = Number(__ENV.TARGET_VUS || 1000);
const RAMP_UP = __ENV.RAMP_UP || "5m";
const HOLD = __ENV.HOLD || "10m";
const RAMP_DOWN = __ENV.RAMP_DOWN || "2m";
const RUN_ONCE = (__ENV.RUN_ONCE || "false").toLowerCase() === "true";
const ALLOW_USER_REUSE = (__ENV.ALLOW_USER_REUSE || "false").toLowerCase() === "true";

const CLIENT_TYPE = __ENV.CLIENT_TYPE || "WEB";
const GISU_ID = __ENV.GISU_ID;
const NOTICE_ID = __ENV.NOTICE_ID;
const NOTICE_TAB = __ENV.NOTICE_TAB || "CHALLENGER";
const PAGE_SIZE = __ENV.PAGE_SIZE || "20";
const INCLUDE_RECORD_READ = (__ENV.INCLUDE_RECORD_READ || "true").toLowerCase() === "true";
const INCLUDE_READ_STATUS = (__ENV.INCLUDE_READ_STATUS || "true").toLowerCase() === "true";
const READ_STATUS = __ENV.READ_STATUS || "UNREAD";
const READ_STATUS_FILTER_TYPE = __ENV.READ_STATUS_FILTER_TYPE || "ALL";
const THINK_MIN_MS = Number(__ENV.THINK_MIN_MS || 500);
const THINK_MAX_MS = Number(__ENV.THINK_MAX_MS || 2000);

const LOGIN_P95_MS = Number(__ENV.LOGIN_P95_MS || 800);
const NOTICE_LIST_P95_MS = Number(__ENV.NOTICE_LIST_P95_MS || 500);
const NOTICE_DETAIL_P95_MS = Number(__ENV.NOTICE_DETAIL_P95_MS || 500);
const NOTICE_READ_P95_MS = Number(__ENV.NOTICE_READ_P95_MS || 500);
const NOTICE_READ_STATUS_P95_MS = Number(__ENV.NOTICE_READ_STATUS_P95_MS || 800);
const JOURNEY_P95_MS = Number(__ENV.JOURNEY_P95_MS || 5000);

const users = new SharedArray("notice-login-users", () => {
  const parsed = JSON.parse(open(USERS_FILE));
  const dataset = Array.isArray(parsed) ? parsed : parsed.users;
  if (!Array.isArray(dataset)) {
    throw new Error(`${USERS_FILE}은 배열 또는 { "users": [...] } 형식이어야 합니다.`);
  }
  return dataset;
});

const noticeJourneyDuration = new Trend("notice_journey_duration", true);
const noticeLoginSuccess = new Rate("notice_login_success");
const noticeListSuccess = new Rate("notice_list_success");
const noticeDetailSuccess = new Rate("notice_detail_success");
const noticeRecordReadSuccess = new Rate("notice_record_read_success");
const noticeReadStatusSuccess = new Rate("notice_read_status_success");
const noticeListEmptyTotal = new Counter("notice_list_empty_total");

export const options = {
  scenarios: RUN_ONCE ? {
    notice_login_once: {
      executor: "per-vu-iterations",
      vus: TARGET_VUS,
      iterations: 1,
      maxDuration: __ENV.MAX_DURATION || "20m",
      exec: "noticeJourney",
      tags: {flow: "notice_login_1000_vus", mode: "once"},
    },
  } : {
    notice_login_1000_vus: {
      executor: "ramping-vus",
      stages: [
        {duration: RAMP_UP, target: TARGET_VUS},
        {duration: HOLD, target: TARGET_VUS},
        {duration: RAMP_DOWN, target: 0},
      ],
      gracefulRampDown: "30s",
      exec: "noticeJourney",
      tags: {flow: "notice_login_1000_vus", mode: "sustained"},
    },
  },
  thresholds: {
    "checks{flow:notice_login_1000_vus}": ["rate>0.99"],
    "http_req_failed{flow:notice_login_1000_vus}": ["rate<0.01"],
    "http_req_duration{api:login_email}": [`p(95)<${LOGIN_P95_MS}`],
    "http_req_duration{api:notice_list}": [`p(95)<${NOTICE_LIST_P95_MS}`],
    "http_req_duration{api:notice_list_back}": [`p(95)<${NOTICE_LIST_P95_MS}`],
    "http_req_duration{api:notice_detail}": [`p(95)<${NOTICE_DETAIL_P95_MS}`],
    "http_req_duration{api:notice_record_read}": [`p(95)<${NOTICE_READ_P95_MS}`],
    "http_req_duration{api:notice_read_status}": [`p(95)<${NOTICE_READ_STATUS_P95_MS}`],
    notice_journey_duration: [`p(95)<${JOURNEY_P95_MS}`],
    notice_login_success: ["rate>0.99"],
    notice_list_success: ["rate>0.99"],
    notice_detail_success: ["rate>0.99"],
    notice_record_read_success: ["rate>0.99"],
    notice_read_status_success: ["rate>0.99"],
  },
  summaryTrendStats: ["min", "avg", "med", "p(90)", "p(95)", "p(99)", "max"],
  userAgent: "umc-product-k6-notice-login/1.0",
};

let accessToken;
let memberId;

export function setup() {
  if (!users || users.length === 0) {
    throw new Error(`${USERS_FILE}에 로그인 계정이 없습니다.`);
  }

  if (users.length < TARGET_VUS && !ALLOW_USER_REUSE) {
    throw new Error(
      `TARGET_VUS=${TARGET_VUS}인데 계정은 ${users.length}개입니다. ` +
      "계정 재사용은 결과를 왜곡할 수 있으므로, 필요하면 ALLOW_USER_REUSE=true를 명시하세요."
    );
  }

  if (!GISU_ID) {
    throw new Error("공지 목록 조회에는 GISU_ID가 필요합니다.");
  }

  return {
    startedAt: new Date().toISOString(),
    targetVus: TARGET_VUS,
  };
}

export function noticeJourney() {
  const startedAt = Date.now();
  const user = pickUser();

  group("login", () => {
    ensureLoggedIn(user);
  });

  let noticeId = NOTICE_ID;

  group("notice list", () => {
    const listResponse = listNotices("notice_list");
    const listOk = isSuccess(listResponse);
    noticeListSuccess.add(listOk);
    check(listResponse, {
      "notice list status is 2xx": (response) => response.status >= 200 && response.status < 300,
      "notice list api success": isSuccess,
    }, {flow: "notice_login_1000_vus"});

    if (!noticeId) {
      noticeId = extractFirstNoticeId(listResponse);
    }

    if (!noticeId) {
      noticeListEmptyTotal.add(1);
      fail("공지 목록에서 상세 조회할 noticeId를 찾지 못했습니다. NOTICE_ID를 직접 지정하거나 시딩 데이터를 확인하세요.");
    }
  });

  randomThinkTime();

  group("notice detail", () => {
    const detailResponse = http.get(`${BASE_URL}/api/v1/notices/${noticeId}`, authParams("notice_detail"));
    const detailOk = isSuccess(detailResponse);
    noticeDetailSuccess.add(detailOk);
    check(detailResponse, {
      "notice detail status is 2xx": (response) => response.status >= 200 && response.status < 300,
      "notice detail api success": isSuccess,
    }, {flow: "notice_login_1000_vus"});
  });

  if (INCLUDE_RECORD_READ) {
    randomThinkTime();
    group("notice record read", () => {
      const readResponse = http.post(`${BASE_URL}/api/v1/notices/${noticeId}/read`, null, authParams("notice_record_read"));
      const readOk = isSuccess(readResponse);
      noticeRecordReadSuccess.add(readOk);
      check(readResponse, {
        "notice record read status is 2xx": (response) => response.status >= 200 && response.status < 300,
        "notice record read api success": isSuccess,
      }, {flow: "notice_login_1000_vus"});
    });
  } else {
    noticeRecordReadSuccess.add(true);
  }

  if (INCLUDE_READ_STATUS) {
    randomThinkTime();
    group("notice read status", () => {
      const readStatusResponse = http.get(noticeReadStatusUrl(noticeId), authParams("notice_read_status"));
      const readStatusOk = isSuccess(readStatusResponse);
      noticeReadStatusSuccess.add(readStatusOk);
      check(readStatusResponse, {
        "notice read status status is 2xx": (response) => response.status >= 200 && response.status < 300,
        "notice read status api success": isSuccess,
      }, {flow: "notice_login_1000_vus"});
    });
  } else {
    noticeReadStatusSuccess.add(true);
  }

  randomThinkTime();

  group("notice list back", () => {
    const backResponse = listNotices("notice_list_back");
    const backOk = isSuccess(backResponse);
    noticeListSuccess.add(backOk);
    check(backResponse, {
      "notice list back status is 2xx": (response) => response.status >= 200 && response.status < 300,
      "notice list back api success": isSuccess,
    }, {flow: "notice_login_1000_vus"});
  });

  noticeJourneyDuration.add(Date.now() - startedAt, {flow: "notice_login_1000_vus"});
}

function ensureLoggedIn(user) {
  if (accessToken) {
    return;
  }

  const response = http.post(`${BASE_URL}/api/v1/auth/login/email`, JSON.stringify({
    email: user.email,
    password: user.password,
    clientType: user.clientType || CLIENT_TYPE,
  }), {
    headers: {"Content-Type": "application/json"},
    tags: {api: "login_email", flow: "notice_login_1000_vus"},
  });

  const loginOk = isSuccess(response) && !!jsonValue(response, "result.accessToken");
  noticeLoginSuccess.add(loginOk);
  check(response, {
    "login status is 2xx": (res) => res.status >= 200 && res.status < 300,
    "login api success": isSuccess,
    "login access token exists": (res) => !!jsonValue(res, "result.accessToken"),
  }, {flow: "notice_login_1000_vus"});

  if (!loginOk) {
    fail(`로그인 실패: status=${response.status}, email=${maskEmail(user.email)}`);
  }

  accessToken = jsonValue(response, "result.accessToken");
  memberId = jsonValue(response, "result.memberId");
}

function pickUser() {
  const index = ALLOW_USER_REUSE ? (__VU - 1) % users.length : __VU - 1;
  const user = users[index];
  if (!user || !user.email || !user.password) {
    fail(`VU ${__VU}에 매핑할 로그인 계정이 없습니다.`);
  }
  return user;
}

function listNotices(apiTag) {
  return http.get(noticeListUrl(), authParams(apiTag));
}

function authParams(apiTag) {
  return {
    headers: {Authorization: `Bearer ${accessToken}`},
    tags: {
      api: apiTag,
      flow: "notice_login_1000_vus",
      member_id_bucket: memberId ? String(Number(memberId) % 10) : "unknown",
    },
  };
}

function noticeListUrl() {
  return `${BASE_URL}/api/v1/notices?${queryString({
    gisuId: GISU_ID,
    noticeTab: NOTICE_TAB,
    page: "0",
    size: PAGE_SIZE,
    sort: "createdAt,DESC",
    chapterId: __ENV.CHAPTER_ID,
    schoolId: __ENV.SCHOOL_ID,
    part: __ENV.PART,
  })}`;
}

function noticeReadStatusUrl(noticeId) {
  return `${BASE_URL}/api/v1/notices/${noticeId}/read-status?${queryString({
    filterType: READ_STATUS_FILTER_TYPE,
    status: READ_STATUS,
    cursorId: __ENV.READ_STATUS_CURSOR_ID,
    organizationIds: __ENV.ORGANIZATION_IDS,
  })}`;
}

function extractFirstNoticeId(response) {
  const content = jsonValue(response, "result.content") || jsonValue(response, "content") || [];
  if (!Array.isArray(content) || content.length === 0) {
    return undefined;
  }
  return content[0].id || content[0].noticeId;
}

function isSuccess(response) {
  if (response.status < 200 || response.status >= 300) {
    return false;
  }
  const wrappedSuccess = jsonValue(response, "success");
  return wrappedSuccess === undefined || wrappedSuccess === true;
}

function jsonValue(response, path) {
  try {
    return response.json(path);
  } catch (e) {
    return undefined;
  }
}

function queryString(params) {
  const pairs = [];

  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === "") {
      continue;
    }

    if (key === "organizationIds") {
      String(value)
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean)
        .forEach((item) => pairs.push(`${encodeURIComponent(key)}=${encodeURIComponent(item)}`));
      continue;
    }

    pairs.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`);
  }

  return pairs.join("&");
}

function randomThinkTime() {
  if (THINK_MAX_MS <= 0) {
    return;
  }
  const min = Math.max(0, THINK_MIN_MS);
  const max = Math.max(min, THINK_MAX_MS);
  sleep((Math.random() * (max - min) + min) / 1000);
}

function maskEmail(email) {
  const [local, domain] = String(email).split("@");
  if (!domain) {
    return "***";
  }
  return `${local.slice(0, 3)}***@${domain}`;
}
