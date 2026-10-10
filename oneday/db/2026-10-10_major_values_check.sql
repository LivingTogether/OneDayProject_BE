-- #60 배포 전 확인: users.major 값이 MajorCode(전공명 또는 별칭)로 모두 변환되는지 점검합니다.
-- 아래 목록에 없는 값이 나오면 MajorCode의 aliases에 추가하거나 데이터를 정리한 뒤 배포합니다.
--   심화컴퓨팅전공 (별칭: 심화컴퓨터공학, 심화컴퓨터공학전공)
--   플랫폼SW융합전공
--   글로벌SW융합전공 (별칭: 글로벌소프트웨어융합전공)
--   인공지능컴퓨팅전공
SELECT TRIM(major) AS major, COUNT(*) AS cnt
FROM users
GROUP BY TRIM(major)
ORDER BY cnt DESC;
