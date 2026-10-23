ALTER TABLE public.event_outbox ADD COLUMN processing_group varchar(50);

-- 순차 배포 중 구버전 writer가 저장하는 이벤트도 현재 라우팅 규칙으로 분류한다.
-- OutboxRoutingConfig의 규칙을 변경하면 기존 미완료 데이터와 이 함수도 함께 이관한다.
CREATE FUNCTION public.assign_event_outbox_processing_group() RETURNS trigger
    LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.processing_group IS NULL THEN
        NEW.processing_group := CASE
            WHEN NEW.event_type LIKE 'chat.%'
                OR NEW.event_type IN (
                    'community.thread.updated',
                    'community.thread.deleted',
                    'community.thread.invited',
                    'community.thread.member.left',
                    'community.thread.member.kicked'
                ) THEN 'realtime'
            ELSE 'background'
        END;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_event_outbox_processing_group
    BEFORE INSERT OR UPDATE ON public.event_outbox
    FOR EACH ROW EXECUTE FUNCTION public.assign_event_outbox_processing_group();

-- 트리거가 기존 데이터도 동일한 규칙으로 채운다.
UPDATE public.event_outbox SET processing_group = NULL WHERE processing_group IS NULL;

ALTER TABLE public.event_outbox ALTER COLUMN processing_group SET NOT NULL;

CREATE INDEX idx_event_outbox_group_publishable
    ON public.event_outbox (processing_group, next_attempt_at, id)
    WHERE status IN ('PENDING', 'PROCESSING');

-- 구버전 poller의 순차 배포/롤백을 위해 기존 시간순 인덱스는 유지한다.
