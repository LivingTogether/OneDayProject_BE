#!/bin/bash

# 1. 자바 빌드 (테스트 생략하고 빠르게)
echo "Gradle Build..."
./gradlew clean build -x test

# 2. 도커 이미지 빌드 (플랫폼 옵션은 맥 M1/M2 사용자 호환용, 윈도우/리눅스는 빼도 됨)
echo "Docker Build..."
docker build --platform linux/amd64 -t chanwooong/oneday-server:latest .

# 3. 도커 허브 푸시
echo "Docker Push..."
docker push chanwooong/oneday-server:latest

echo "업데이트 완료! 서버에서 ./deploy.sh를 실행하세요."
