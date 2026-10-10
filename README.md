# 👨‍🎓 JOLUV

## 기여자
<table>
<tr>
<td>

|                 ChanWoong                  |
| :---------------------------: |
| <img width="240px" src="https://avatars.githubusercontent.com/ChanWooong" /> |
|     [@ChanWooong](https://github.com/ChanWooong)       |
|          National - 🇰🇷 Republic Of Korea           |

</td>
<td>

|                  HyunWook Kwak                   |
| :------------------------------------------------: |
| <img width="240px" src="https://avatars.githubusercontent.com/Woogie333" /> |
|     [@woogie333](https://github.com/woogie333) |
|          National - 🇰🇷 Republic Of Korea            |

</td>
<td>

|           JaeMin Park          |
| :---------------------------: |
| <img width="240px" src="https://avatars.githubusercontent.com/Jmespoir" /> |
|     [@jmespoir](https://github.com/JMespoir)       |
|          National - 🇰🇷 Republic Of Korea           |

</td>
</tr>
</table>

## Title
**JOLUV  - 경북대학교 졸업 관리 플랫폼**
"성공적인 졸업을 위한 길라잡이"

경북대학교 컴퓨터학부의 **복잡한 졸업 요건을 한눈에 파악하고, 체계적으로 학점과 커리어를 관리**할 수 있는 경북대학교 컴퓨터학부생의 맞춤형 웹 서비스입니다.

## 프로젝트 개요
컴퓨터학부 내부에 필요한 졸업 요건들에 대해서 기존에는 확인이 어려워 직접 수기로 적거나 입력해서 관리하는 방식을 택해야 했습니다. 이러한 불편함을 인지하고 졸업에 필요한 요소들을 미리 파악해 졸업을 하는데 부족한 점과 현재 나의 상태를 보다 편리하게 알고싶다는 니즈가 강하다는 것을 주변 학우들을 통해 알게 되었고, 이러한 고민이 발단이 되어 졸업 요건을 확인할 수 있는 길라잡이를 만들고자 JOLUV라는 서비스를 기획하게 되었습니다.

### 대상
경북대학교 컴퓨터학부 소속 학생
### 목표
복잡한 졸업 요건을 편리한 UI로 쉽게 확인할 수 있는 서비스 제공.


## 주요 기능
#### 1. 수강과목 정리
본인이 수강한 내용에 대해서 전공과 교양을 기반으로 나눠서 간단하게 UI 형태로 볼 수 있습니다. 재수강을 해야하는 과목들에 대해서도 확인이 가능하고 필수로 들어야하는 과목들에 대해서도 특징적으로 보여주는 것이 해당 페이지의 특징입니다.

- 총 학점 이수 현황
- 남은 전공 학점 상태
- 남은 교양 학점 상태
- 현재 이수 완료한 과목 리스트
- 재이수 필요 과목 표시

#### 2. 졸업요건 체크
로그인 한 전공에 맞게 해당 전공이 가지는 졸업 트랙을 자동으로 불러와 몇학점이 부족하고 어떤걸 얼마나 더 채워야하는지를 보다 직관적으로 보여주는 슬롯입니다. 채우지 못한 요건들이 있으면 미완료로 표시되며 바 형태의 그래프로 진행률도 보다 편리하게 확인할 수 있습니다.

- 전공별 졸업 요건 변동
- 공통 필요 요건 / 트랙 및 전공별 필요 요건 기능 구현
- 필수 과목 이수 현황 표시 구현
- 다중전공 이수학점 표시 구현

#### 3. 학점 관리
본인이 수강한 과목에 대한 정보를 얻어오는 공간입니다. 학년과 학기를 입력하면 해당 학년과 학기에 본인 전공은 무엇을 듣는것이 좋은지 커리큘럼 기반으로 추천을 해주고, 성적을 입력하여 본인이 이수한 과목들에 대한 정리도 가능합니다. 특히 다중전공을 선택하거나 Abeek 기반의 전공을 따로 체크해서 관리할 수 있습니다.

- 학년, 학기 선택 시 전공별 추천 커리큘럼 표시
- 과목명 단어별 검색 기능 구현
- 성적 및 이수 구분 설정 후 등록 (이수한 경우)
- 등록 이후 수정 및 삭제 기능 구현

#### 4.  마이페이지 & 커리어 관리
현장 실습, 어학 성적, 세부 트랙 정보를 입력하여 입력을 통해서 정보를 저장해야하는 값들에 대해 처리할 수 있는 마이페이지를 제공하고 있으며, 우측에 본인의 성적이 전체, 전공별로 어떻게 다른지도 확인이 가능합니다. 본인의 수상경력이나 부트캠프 등의 활동들도 우측에 기재하면 저장시켜놓고 이력 관리도 가능합니다.

- 세부트랙 정보 / 어학 성적 / 현장실습 관리 기능 구현
- 성적(전체/전공) 현황 표시 기능 구현
- 경력 및 활동 등록 및 표시 구현


## API 문서
> Notion Link : https://www.notion.so/API-28aaefd6245e80819a93e81d9744ba7c?source=copy_link

## DataBase 구조
> Notion Link : https://www.notion.so/DATABASE-2a1aefd6245e8080bbb5f349dbfaa71d?source=copy_link

## 기술 스택
> Framework
>
![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
> DB
>
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)

> Deploy & Automation
>
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)


> Collaboration Tool
>
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)

## 프로젝트 종료 후 개선 기록
> 팀 프로젝트는 **2025-11-30** 마지막 커밋으로 종료되었습니다.
> 아래 항목은 그 이후(**2026-10~**) 개인적으로 진행한 개선 작업이며, 프로젝트 기간 중 작업과 구분하기 위해 따로 기록합니다.

| 날짜 | 이슈 | 내용 | 개선 전 → 후 |
|---|---|---|---|
| 2026-10-10 | #58 | DB 접속 정보 환경변수 분리 | 설정 파일·스크립트에 비밀번호 평문 커밋 → 환경변수 주입 (`.env.example` 제공) |
