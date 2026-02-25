import http from 'k6/http';
import {check, group, sleep} from 'k6';
import {Rate, Trend} from 'k6/metrics';

// 커스텀 메트릭
const readStatusDuration = new Trend('read_status_duration', true);
const successRate = new Rate('success_rate');

export let options = {
    stages: [
        {duration: '10s', target: 5},   // 워밍업 (JVM JIT 안정화)
        {duration: '30s', target: 10},  // 본 테스트
        {duration: '10s', target: 0},   // 종료
    ],
    thresholds: {
        'read_status_duration': ['p(95)<3000'],  // 95%가 3초 이내
        'success_rate': ['rate>0.95'],           // 성공률 95% 이상
    },
};

const BASE_URL = 'http://localhost:8080';
const NOTICE_ID = 1; // 테스트할 공지 ID
const ACCESS_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzcyMDAwNzIyLCJleHAiOjE3NzIwMDQzMjJ9.lmQR3Vg6zGkdtBf4vwtnVVO01fPNamYyw5LhhvRpKzSSDifk56i3LHLpPwlYNxBlkmLByEjB0aPIWBiqB2YX3w";

export default function () {
    group('읽음 현황 조회', () => {
        const res = http.get(`${BASE_URL}/api/v1/notices/${NOTICE_ID}/read-status?filterType=ALL&status=UNREAD`, {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${ACCESS_TOKEN}` // 인증 필요하면 추가
            },
        });

        const success = check(res, {
            'status 200': (r) => r.status === 200,
            '3초 이내': (r) => r.timings.duration < 3000,
        });

        // 커스텀 메트릭에 기록
        readStatusDuration.add(res.timings.duration);
        successRate.add(success);
    });

    sleep(1);
}
