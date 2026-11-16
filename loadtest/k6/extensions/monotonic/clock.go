package monotonic

import (
	"time"

	"go.k6.io/k6/v2/js/modules"
)

var origin = time.Now()

func init() {
	modules.Register("k6/x/monotonic", new(Clock))
}

type Clock struct{}

// Now는 시작 시 고정한 Unix 시각에 단조 경과시간을 더해 시각 보정의 영향을 피한다.
func (*Clock) Now() int64 {
	return origin.UnixMilli() + time.Since(origin).Milliseconds()
}
