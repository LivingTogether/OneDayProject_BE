-- #65 user_attend 중복 수강 데이터 정리 및 UNIQUE 제약 추가
-- 운영 DB에서 애플리케이션 배포 "전에" 수동으로 실행합니다.
-- (ddl-auto=update는 중복 데이터가 남아 있으면 UNIQUE 제약 생성에 실패하고 조용히 넘어갑니다)

BEGIN;

-- 1. 정리 전 중복 현황 확인 (기록용)
SELECT student_id, lec_id, COUNT(*) AS cnt
FROM user_attend
GROUP BY student_id, lec_id
HAVING COUNT(*) > 1
ORDER BY cnt DESC;

-- 2. PK가 없다면 추가
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'user_attend'::regclass AND contype = 'p'
    ) THEN
        ALTER TABLE user_attend ADD PRIMARY KEY (idx);
    END IF;
END $$;

-- 3. (student_id, lec_id)별로 가장 먼저 등록된 행(idx 최소)만 남기고 삭제
DELETE FROM user_attend a
USING user_attend b
WHERE a.student_id = b.student_id
  AND a.lec_id = b.lec_id
  AND a.idx > b.idx;

-- 4. 복합 UNIQUE 제약 추가 (엔티티의 uk_user_attend_student_lec와 같은 이름)
ALTER TABLE user_attend
    ADD CONSTRAINT uk_user_attend_student_lec UNIQUE (student_id, lec_id);

COMMIT;

-- 5. 중복으로 부풀려진 users 학점 합계는 #66 검증 쿼리로 확인 후 재계산합니다.
