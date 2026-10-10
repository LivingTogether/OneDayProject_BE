import pandas as pd
from sqlalchemy import create_engine, text
from sshtunnel import SSHTunnelForwarder
import sys
import paramiko
import os
import re

# =========================================================
# 🛠️ [Paramiko Patch]
# =========================================================
if not hasattr(paramiko, 'DSSKey'):
    paramiko.DSSKey = paramiko.RSAKey

# =========================================================
# [설정] 서버, DB 정보
# =========================================================
# 접속 정보는 환경변수로 주입합니다. (oneday/.env.example 참고)
SSH_HOST = os.environ['SSH_HOST']
SSH_USER = os.environ.get('SSH_USER', 'ubuntu')
SSH_KEY_PATH = os.environ['SSH_KEY_PATH']

DB_USER = os.environ['DB_USER']
DB_PASSWORD = os.environ['DB_PW']
DB_NAME = os.environ.get('DB_NAME', 'appdb')
TABLE_NAME = 'lecture_list'  # 테이블 이름 변경 (전체 히스토리이므로)

# =========================================================
# 📝 [설정] 파일 리스트 자동 생성 (2020-1 ~ 2025-2)
# =========================================================
# W: 원격, E: 영어(전공), J: 전공, K: 교양
FILES_CONFIG = []

years = range(2020, 2026) # 2020 ~ 2025
semesters = [1, 2]
file_types = {
    'W': {'cat': '원격', 'lang': 'KR', 'eng_filter': False},
    'E': {'cat': '전공', 'lang': 'EN', 'eng_filter': True},
    'J': {'cat': '전공', 'lang': 'KR', 'eng_filter': False},
    'K': {'cat': '교양', 'lang': 'KR', 'eng_filter': False},
}

print("📋 처리할 파일 리스트 생성 중...")
for year in years:
    for sem in semesters:
        semester_str = f"{year}-{sem}" # 예: 2020-1
        
        for suffix, info in file_types.items():
            # 파일명 예시: 2020-1W.xlsx, 2020-1J.xlsx
            filename = f"{semester_str}{suffix}.xlsx"
            
            config = {
                'path': filename,
                'semester': semester_str,
                'cat': info['cat'],
                'lang': info['lang'],
                'english_filter': info['eng_filter']
            }
            FILES_CONFIG.append(config)

# (확인용) 생성된 파일 개수 출력
print(f"   -> 총 {len(FILES_CONFIG)}개의 파일 설정이 준비되었습니다.")

# =========================================================
# 2. 데이터 전처리 함수
# =========================================================
def preprocess(df, category, lang_code, semester):
    rename_map = {
        '강좌\n번호': 'lec_num',
        '학점': 'credit',
        '교과목명': 'lec_name',
        '학년': 'grade',
        '구분': 'lec_type',
        '개설\n학과': 'open_depart',
        '강의시간\n(강의실)': 'time_room',
        '비고': 'remark'
    }

    available_cols = [col for col in rename_map.keys() if col in df.columns]
    
    df_selected = df[available_cols].copy()
    df_selected = df_selected.rename(columns=rename_map)
    
    # 강좌번호 하이픈 제거
    if 'lec_num' in df_selected.columns:
        df_selected['lec_num'] = df_selected['lec_num'].astype(str).str.split('-').str[0]

    # 메타 데이터 추가
    df_selected['category'] = category
    df_selected['language'] = lang_code
    df_selected['semester'] = semester  # [중요] 학기 정보 추가
    
    return df_selected

# =========================================================
# 1. 엑셀 파일 읽기 및 병합
# =========================================================
print("1. 엑셀 파일 읽기 및 전처리 시작...")
dfs_to_merge = []

for config in FILES_CONFIG:
    file_path = config['path']
    # 파일 존재 여부 확인 (없으면 건너뜀)
    if not os.path.exists(file_path):
        # 너무 로그가 길어질 수 있으므로 파일이 없을 때는 조용히 넘어가거나 짧게 출력
        # print(f"   (Skip) 파일 없음: {file_path}")
        continue

    try:
        # 엑셀 읽기
        df_temp = pd.read_excel(file_path, header=2)
        
        # 영어 강의 필터링 (E 타입인 경우)
        if config['english_filter'] and '강의\n언어' in df_temp.columns:
            df_temp = df_temp[df_temp['강의\n언어'] == '영어'].copy()

        # 전처리 수행
        df_processed = preprocess(
            df_temp, 
            category=config['cat'], 
            lang_code=config['lang'], 
            semester=config['semester']
        )
        dfs_to_merge.append(df_processed)
        print(f"   -> [{file_path}] 로드 완료 ({len(df_processed)}건)")

    except Exception as e:
        print(f"❌ [오류] {file_path} 읽기 실패: {e}")
        continue

# 전체 병합
if not dfs_to_merge:
    print("❌ [오류] 읽어온 데이터가 하나도 없습니다. 파일 경로와 이름을 확인해주세요.")
    sys.exit(1)

df_combined = pd.concat(dfs_to_merge, ignore_index=True)
print("   -> 모든 데이터 병합 완료")

# =========================================================
# 중복 제거 (학기별 강좌번호 기준)
# =========================================================
initial_count = len(df_combined)

# [수정] 같은 학기(semester) 내에서 강좌번호(lec_num)가 겹치는 것만 제거
# (서로 다른 학기의 같은 강좌는 유지됨)
df_combined.drop_duplicates(subset=['semester', 'lec_num'], keep='first', inplace=True)

final_count = len(df_combined)
print(f"2. 중복 제거 결과: {initial_count}개 -> {final_count}개")

# idx 컬럼 생성
df_combined.insert(0, 'idx', range(1, len(df_combined) + 1))

# =========================================================
# 3. SSH 터널링 및 DB 적재
# =========================================================
print("3. DB 연결 및 업로드 시작...")

try:
    with SSHTunnelForwarder(
            (SSH_HOST, 22),
            ssh_username=SSH_USER,
            ssh_pkey=SSH_KEY_PATH,
            remote_bind_address=('localhost', 5432)
    ) as tunnel:

        local_port = tunnel.local_bind_port
        db_connection_str = f'postgresql://{DB_USER}:{DB_PASSWORD}@localhost:{local_port}/{DB_NAME}'
        engine = create_engine(db_connection_str)

        # 1) 데이터 저장
        df_combined.to_sql(name=TABLE_NAME, con=engine, if_exists='replace', index=False)
        print(f"   - '{TABLE_NAME}' 테이블 저장 완료")

        # 2) Primary Key 설정
        with engine.connect() as con:
            con.execute(text(f'ALTER TABLE "{TABLE_NAME}" ALTER COLUMN idx SET NOT NULL;'))
            con.execute(text(f'ALTER TABLE "{TABLE_NAME}" ADD PRIMARY KEY (idx);'))
            con.commit()

        print(f"✅ [성공] 업데이트 완료! (총 {len(df_combined)}건)")

except Exception as e:
    print(f"❌ [에러 발생] : {e}")
