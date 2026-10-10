-- #66 수강 이력(user_attend)과 users 학점 합계 불일치 검증 쿼리
-- 결과가 0건이면 정합성이 맞는 상태입니다.
-- 불일치가 있으면 oneday.credit.recalculate-on-startup=true 로 서버를 한 번 기동해 재계산합니다.
WITH expected AS (
    SELECT u.student_id,
           COALESCE(SUM(a.credit), 0) AS total,
           COALESCE(SUM(a.credit) FILTER (WHERE a.lec_type IN ('전공', '전공필수', '전공선택', '전공기초')
               OR (u.major IN ('심화컴퓨팅전공', '플랫폼SW융합전공') AND a.lec_type IN ('전공기반', '공학전공'))), 0) AS major,
           COALESCE(SUM(a.credit) FILTER (WHERE a.lec_type IN ('교양', '교양필수', '교양선택')
               OR (u.major IN ('심화컴퓨팅전공', '플랫폼SW융합전공') AND a.lec_type = '기본소양')), 0) AS general
    FROM users u
    LEFT JOIN user_attend a ON a.student_id = u.student_id
    GROUP BY u.student_id, u.major
)
SELECT u.student_id,
       u.total_credit, e.total AS expected_total,
       u.major_credit, e.major AS expected_major,
       u.general_credit, e.general AS expected_general
FROM users u
JOIN expected e ON e.student_id = u.student_id
WHERE u.total_credit <> e.total OR u.major_credit <> e.major OR u.general_credit <> e.general;
