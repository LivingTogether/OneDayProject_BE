import pandas as pd
from sqlalchemy import create_engine, text
from sshtunnel import SSHTunnelForwarder
import paramiko
import os
import sys

# =========================================================
# 🛠️ [Paramiko Patch]
# =========================================================
if not hasattr(paramiko, 'DSSKey'):
    paramiko.DSSKey = paramiko.RSAKey

# =========================================================
# [설정] 서버 및 DB 정보
# =========================================================
SSH_HOST = 'ec2-16-176-198-162.ap-southeast-2.compute.amazonaws.com'
SSH_USER = 'ubuntu'
SSH_KEY_PATH = '/home/woong/IdeaProjects/OneDayProject_BE/oneday/postgresqlKey.pem'

DB_USER = 'yummy'
DB_PASSWORD = 'yummy1234'
DB_NAME = 'appdb'
TABLE_NAME = 'startup_course'

# =========================================================
# 📝 [설정] 대상 파일 및 시트 정보
# =========================================================
TARGET_FILE = 'startup.xlsx'       # 엑셀 파일 이름
TARGET_SHEET = '2023~2025'         # 읽어올 시트 이름
HEADER_ROW = 1                     # 헤더 위치 (0부터 시작하므로 2번째 줄은 1)

# =========================================================
# 1. 데이터 읽기 및 전처리
# =========================================================
def load_and_preprocess():
    print(f"1. '{TARGET_FILE}'의 '{TARGET_SHEET}' 시트 읽는 중...")

    if not os.path.exists(TARGET_FILE):
        print(f"❌ [오류] 폴더에 '{TARGET_FILE}' 파일이 없습니다.")
        sys.exit(1)

    try:
        # 1) 엑셀 읽기 (특정 시트 지정)
        df = pd.read_excel(TARGET_FILE, sheet_name=TARGET_SHEET, header=HEADER_ROW)
        
        # 2) 컬럼명 정리 (공백/줄바꿈 제거)
        df.columns = df.columns.astype(str).str.strip().str.replace('\n', '')
        print(f"   - 컬럼 목록: {list(df.columns)}")

        # 3) 필요한 컬럼 찾기
        lec_id_col = next((c for c in df.columns if '교과목번호' in c or '학수번호' in c), None)
        lec_name_col = next((c for c in df.columns if '교과목명' in c), None)

        if not (lec_id_col and lec_name_col):
            print("❌ [오류] 시트 내에서 '교과목번호' 또는 '교과목명' 컬럼을 찾을 수 없습니다.")
            sys.exit(1)

        # 4) 데이터 추출 및 컬럼명 변경
        df_final = df[[lec_id_col, lec_name_col]].copy()
        df_final.columns = ['lec_id', 'lec_name']

        # 5) 결측치 제거 (교과목번호가 빈 행 삭제)
        df_final.dropna(subset=['lec_id'], inplace=True)

        # 6) 중복 제거 (가장 마지막 행 유지)
        initial_count = len(df_final)
        df_final.drop_duplicates(subset=['lec_id'], keep='last', inplace=True)
        final_count = len(df_final)
        
        print(f"   - 데이터 로드 완료: {final_count}건 (중복 {initial_count - final_count}건 제거됨)")

        # 7) idx 컬럼 생성 (1부터 시작)
        df_final.insert(0, 'idx', range(1, final_count + 1))
        
        return df_final

    except ValueError as ve:
        print(f"❌ [오류] 시트 이름 문제: {ve}")
        print(f"   (엑셀 파일 안에 '{TARGET_SHEET}' 시트가 있는지 확인해주세요.)")
        sys.exit(1)
    except Exception as e:
        print(f"❌ [에러] 처리 중 오류 발생: {e}")
        sys.exit(1)

# =========================================================
# 2. DB 적재
# =========================================================
def upload_to_db(df):
    print("2. DB 연결 및 업로드 시작...")
    
    try:
        with SSHTunnelForwarder(
                (SSH_HOST, 22),
                ssh_username=SSH_USER,
                ssh_pkey=SSH_KEY_PATH,
                remote_bind_address=('localhost', 5432)
        ) as tunnel:
            
            local_port = tunnel.local_bind_port
            db_url = f'postgresql://{DB_USER}:{DB_PASSWORD}@localhost:{local_port}/{DB_NAME}'
            engine = create_engine(db_url)
            
            # 1) 데이터 저장 (기존 테이블 대체)
            df.to_sql(name=TABLE_NAME, con=engine, if_exists='replace', index=False)
            print(f"   - 데이터 {len(df)}건 전송 완료")
            
            # 2) PK 및 Index 설정
            with engine.connect() as con:
                con.execute(text(f'ALTER TABLE "{TABLE_NAME}" ADD PRIMARY KEY (idx);'))
                con.execute(text(f'CREATE INDEX idx_startup_lec_id ON "{TABLE_NAME}" (lec_id);'))
                con.commit()
                
            print(f"✅ [성공] '{TABLE_NAME}' 테이블 업데이트 완료!")

    except Exception as e:
        print(f"❌ [DB 에러] : {e}")

# =========================================================
# 실행
# =========================================================
if __name__ == "__main__":
    df_result = load_and_preprocess()
    upload_to_db(df_result)
