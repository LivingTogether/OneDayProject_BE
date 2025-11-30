package com.knuaf.oneday.service;

import com.knuaf.oneday.component.LectureTableNameProvider; // ★ 아까 만든 객체 import
import com.knuaf.oneday.dto.LectureResponseDto;
import com.knuaf.oneday.entity.Lecture;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final EntityManager em; // ★ Native Query를 날리기 위한 매니저
    private final LectureTableNameProvider tableNameProvider; // ★ 테이블 이름 생성기

    @Transactional(readOnly = true)
    public List<LectureResponseDto> getLectureList(String keyword) {

        // 1. Provider를 통해 안전하게 테이블 이름 가져오기
        // String tableName = tableNameProvider.getTableName(fullSemester);
        String tableName = "lecture_list";

        // 2. 동적 SQL 생성
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(tableName); // 테이블 이름 삽입
        sql.append(" WHERE 1=1 "); // 조건 추가를 위한 더미 조건

        // 검색어가 있으면 SQL에 조건 추가
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND lec_name LIKE :keyword ");
        }

        try {
            // 3. 네이티브 쿼리 생성
            // (Lecture.class를 넣으면 결과를 자동으로 엔티티로 매핑해줍니다)
            Query query = em.createNativeQuery(sql.toString(), Lecture.class);

            // 4. 파라미터 바인딩
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword + "%");
            }

            // 5. 실행 및 결과 가져오기
            List<Lecture> resultList = query.getResultList();

            // 6. DTO로 변환하여 반환
            return resultList.stream()
                    .map(LectureResponseDto::from)
                    .toList(); // Java 16+

        } catch (Exception e) {
            // 해당 학기의 테이블이 없는 경우 (SQL 에러 발생 시) 빈 리스트 반환
            return Collections.emptyList();
        }
    }
    // 학년(grade)과 학기(semester)를 받아서 -> DB에서 상세 정보를 조회하여 반환
    @Transactional(readOnly = true)
    public List<LectureResponseDto> getStandardCourses(String major, int grade, int Semester) {

        // 2. 해당 학년/학기에 들어야 할 '강좌번호 리스트' 가져오기 (하드코딩된 메서드 호출)
        List<String> targetLecIds = getTargetLectureIds(major, grade, Semester);

        // 해당하는 과목이 없으면 빈 리스트 반환
        if (targetLecIds.isEmpty()) {
            return Collections.emptyList();
        }
       // String fullSemester = "2025" + Semester;
        // 3. DB 테이블 이름 가져오기 (예: lecture_list20251)
       // String tableName = tableNameProvider.getTableName(fullSemester);
        String tableName = "lecture_list";

        // 4. DB에서 조회 (WHERE lec_num IN (...))
        // JPA Native Query로 'IN' 절 사용하기
        String sql = "SELECT * FROM " + tableName + " WHERE lec_id IN (:ids)";

        try {
            List<Lecture> resultList = em.createNativeQuery(sql, Lecture.class)
                    .setParameter("ids", targetLecIds) // 리스트를 파라미터로 넘김
                    .getResultList();

            // 5. DTO 변환 및 반환
            return resultList.stream()
                    .map(LectureResponseDto::from)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ========================================================
    // ★ [설정] 학년/학기별 권장 과목 ID 리스트 (여기만 관리하면 됨)
    // ========================================================
    private List<String> getTargetLectureIds(String major, int grade, int term) {
        List<String> ids = new ArrayList<>();
        if("심화컴퓨팅전공".equals(major) || "플랫폼SW융합전공".equals(major)) {
            if (grade == 1) {
                if (term == 1) {  // [1학년 1학기 권장 과목 번호들]
                    ids.add("CLTR0045");
                    ids.add("CLTR0112");
                    ids.add("CLTR0205");
                    ids.add("CLTR0819");
                    ids.add("COMP0453");
                    ids.add("ITEC0201");

                } else if (term == 3) {
                    // [1학년 2학기]
                    ids.add("CLTR0003");
                    ids.add("COME0301");
                    ids.add("CLTR0211");
                    ids.add("COMP0204");
                    ids.add("COMP0205");
                }
            } else if (grade == 2) {
                if (term == 1) {
                    // [2학년 1학기]
                    ids.add("FUTR0208");
                    ids.add("CLTR0213");
                    ids.add("MTED0231");
                    ids.add("COMP0217");
                    ids.add("COMP0315");
                    ids.add("ELEC0247");
                    ids.add("COME0331");
                    ids.add("COMP0216");

                }
                if (term == 3) {
                    // [2학년 2학기]
                    ids.add("COME0311");
                    ids.add("ITEC0419");
                    ids.add("CLTR0246");
                    ids.add("COMP0225");
                    ids.add("COMP0411");
                    ids.add("COMP0432");
                    ids.add("ELEC0462");
                }
            } else if (grade == 3) {
                if (term == 1) {
                    // [3학년 1학기]
                    ids.add("CLTR0264");
                    ids.add("COMP0432");
                    ids.add("EECS0312");
                    ids.add("GLSO0219");
                    ids.add("ITEC0415");
                    ids.add("ITEC0419");
                }
                if (term == 3) {
                    // [3학년 2학기]
                    ids.add("FUTR0214");
                    ids.add("STAT0452");
                    ids.add("TCHR0593");
                    ids.add("TCHR0594");
                    ids.add("COMP0320");
                    ids.add("COMP0322");
                    ids.add("COMP0328");
                    ids.add("COMP0423");
                    ids.add("EECS00312");
                    ids.add("ITEC0417");
                    ids.add("ITEC0401");
                }
            } else if (grade == 4) {
                if (term == 1) {
                    // [4학년 1학기]
                    ids.add("ITEC0403");
                    ids.add("COMP0417");
                    ids.add("TCHR0522");
                    ids.add("COMP0413");
                    ids.add("COMP0414");
                    ids.add("COMP0419");
                    ids.add("COMP0420");
                    ids.add("COMP0462");
                    ids.add("GLSO0215");
                    ids.add("GLSO0224");
                    ids.add("COMP0460");
                    ids.add("ITEC0402");
                    ids.add("ITEC0414");
                    ids.add("ITEC0415");
                    ids.add("ITEC0416");
                    ids.add("MBIO0402");
                    ids.add("MOBI0224");

                }
                if (term == 3) {
                    // [4학년 2학기]
                    ids.add("CLTR0043");
                    ids.add("COMP0428");
                    ids.add("CAIBO211");
                    ids.add("COME0368");
                    ids.add("COMP0435");
                    ids.add("COMP0436");
                    ids.add("COMP0455");
                    ids.add("COMP0457");
                    ids.add("COMP0458");
                    ids.add("COMP0461");
                    ids.add("GLSO0227");
                    ids.add("ITEC0418");
                    ids.add("ITEC0424");
                    ids.add("ITEC0425");
                }
            }
        }
        else if("글로벌SW융합전공".equals(major)){
            if (grade == 1) {
                if (term == 1) {  // [1학년 1학기 권장 과목 번호들]
                    ids.add("CLTR0205");
                    ids.add("CLTR0819");
                    ids.add("COME0301");
                    ids.add("COMP0204");
                    ids.add("ITEC0201");

                } else if (term == 3) {
                    // [1학년 2학기]
                    ids.add("CLTR0003");
                    ids.add("CLTR0205");
                    ids.add("COME0331");
                    ids.add("COMP0216");
                    ids.add("GLSO0212");
                    ids.add("GLSO0213");
                }
            } else if (grade == 2) {
                if (term == 1) {
                    // [2학년 1학기]
                    ids.add("FUTR0201");
                    ids.add("FUTR0208");
                    ids.add("COME0311");
                    ids.add("COMP0217");
                    ids.add("COMP0411");
                    ids.add("ELECO462");
                    ids.add("GLSO0214");
                    ids.add("GLSO0216");

                }
                if (term == 3) {
                    // [2학년 2학기]
                    ids.add("CLTR0043");
                    ids.add("FUTR0214");
                    ids.add("COMP0224");
                    ids.add("COMP0312");
                    ids.add("COMP0323");
                    ids.add("COMP0324");
                    ids.add("GLSO0212");
                }
            } else if (grade == 3) {
                if (term == 1) {
                    // [3학년 1학기]
                    ids.add("CLTR0264");
                    ids.add("COMP0432");
                    ids.add("EECS0312");
                    ids.add("GLSO0219");
                    ids.add("ITEC0415");
                    ids.add("ITEC0419");
                }
                if (term == 3) {
                    // [3학년 2학기]
                    ids.add("CLTR0089");
                    ids.add("COMP0322");
                    ids.add("COMP0328");
                    ids.add("COMP0414");
                    ids.add("COMP0460");
                    ids.add("ITEC0417");
                }
            } else if (grade == 4) {
                if (term == 1) {
                    // [4학년 1학기]
                    ids.add("FUTR0226");
                    ids.add("COMP0321");
                    ids.add("COMP0413");
                    ids.add("COMP0420");
                    ids.add("COMP0462");
                    ids.add("GLSO0215");
                    ids.add("GLSO0223");
                    ids.add("ITEC0401");
                    ids.add("ITEC0403");
                    ids.add("MBIO0402");
                    ids.add("MOBI0224");

                }
                if (term == 3) {
                    // [4학년 2학기]
                    ids.add("FUTR0231");
                    ids.add("CAIB0211");
                    ids.add("COME0368");
                    ids.add("COMP0423");
                    ids.add("COMP0428");
                    ids.add("COMP0436");
                    ids.add("COMP0457");
                    ids.add("GLSO0227");
                    ids.add("GLSO0229");
                    ids.add("ITEC0418");
                    ids.add("ITEC0424");
                }
            }
        }
        else if("인공지능컴퓨팅전공".equals(major)){
            if (grade == 1) {
                if (term == 1) {  // [1학년 1학기 권장 과목 번호들]
                    ids.add("CLTR0205");
                    ids.add("CLTR0045");
                    ids.add("CLTR0819");
                    ids.add("COMP0453");
                   // ids.add("신규"); 인공지능과컴퓨팅
                    ids.add("COME0301");

                } else if (term == 3) {
                    // [1학년 2학기]
                    ids.add("CLTR0003");
                    ids.add("CLTR0211");
                    ids.add("COMP0454");
                    ids.add("COMP0204");
                    ids.add("COMP0205");
                }
            } else if (grade == 2) {
                if (term == 1) {
                    // [2학년 1학기]
                    ids.add("MTED0231");
                    ids.add("COME0331");
                    ids.add("COMP0216");
                    ids.add("COMP0217");
                    ids.add("COMP0411");
                    ids.add("COMP0315");
                }
                if (term == 3) {
                    // [2학년 2학기]
                    ids.add("CLTR0246");
                    ids.add("COME0311");
                    ids.add("ITEC0419");
                    ids.add("COMP0324");
                    ids.add("COMP0312");
                    ids.add("ELEC0462");
                    ids.add("COMP0224");
                }
            } else if (grade == 3) {
                if (term == 1) {
                    // [3학년 1학기]
                    ids.add("CLTR0264");
                    ids.add("ITEC");
                    ids.add("CAIB0233");
                    ids.add("COMP0321");
                    ids.add("GLSO0215");
                    ids.add("COMP0319");
                    ids.add("COMP0323");
                }
                if (term == 3) {
                    // [3학년 2학기]
                    ids.add("CLTR0212");
                    ids.add("MOBI0224");
                    ids.add("CAIB0211");
                    ids.add("ITEC0424");
                    ids.add("CAIB0216");
                    ids.add("COMP0322");
                    ids.add("ITEC0401");
                }
            } else if (grade == 4) {
                if (term == 1) {
                    // [4학년 1학기]
                    ids.add("FUTR0201");
                    ids.add("COMP0462");
                    ids.add("MBIO0402");
                    ids.add("COMP0419");
                    ids.add("COMP0219");
                    ids.add("CAIB0222");
                    ids.add("CAIB0234");
                    ids.add("CAIB0225");
                    ids.add("COMP0414");
                    ids.add("ITEC0415");
                    ids.add("COMP0460");

                }
                if (term == 3) {
                    // [4학년 2학기]
                    ids.add("CLTR0078");
                    ids.add("COMP0457");
                    ids.add("CAIB0224");
                    ids.add("COMP0455");
                    ids.add("ITEC0514");
                    ids.add("COMP0436");
                    ids.add("COMP0328");
                    ids.add("COME0368");
                    ids.add("ITEC0418");
                    ids.add("GLSO0227");
                  //  ids.add("신규"); AI심화글쓰기
                }
            }
        }


        return ids;
    }
}