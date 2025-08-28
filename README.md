# Wayble🏙️


**사용자를 위한 맞춤형 배리어프리 서비스 Wayble**

> 모두의 편리한 이동을 위한 맞춤형 경로 탐색 및 장소 추천 서비스 <br>
> 장애 유형별·이동 수단별 최적화된 경로 안내와 접근성 정보 공유 플랫폼, Wayble에서 만나보세요!
 <br>

 <img width="731" height="508" alt="웨이블발표" src="https://github.com/user-attachments/assets/567f7e5c-ea57-41aa-8988-fa64cbed6fd2" />

### [🛠️Wayble 서비스 링크 바로가기](https://wayble.site)
### [🎬Wayble 노션 링크 바로가기](https://www.notion.so/wayble-20475cf0b87b806d9473feb579ab23e0)

### 📂 Content
- [🔎 팀 소개](#팀-소개)
- [🔎 기술 스택](#기술-스택)
- [🔎 서비스 고안 배경](#서비스-고안-배경)
- [🔎 화면 구성](#화면-구성)
- [🔎 주요 기능](#주요-기능)
- [🔎 상세 기능](#상세-기능)
- [🔎 BE 폴더 구조](#BE-폴더-구조)
- [🔎 BE 시스템 구성도](#BE-시스템-구성)
- [🔎 데이터베이스 구조](#데이터베이스-구조)
- [🔎 api 명세](#api-명세)





<br><br>
## 😎 팀 소개
> Team 
> 기승민 양효인 유승인 이원준 주정빈 


| 기승민 (Lead)                                  | 양효인                               | 유승인                                 | 이원준                                           | 주정빈                               |   
| ---------------------------------------- | ------------------------------------ | -------------------------------------- | ------------------------------------------------ | -------------------------------------- | 
| ![profile](https://avatars.githubusercontent.com/u/67568824?v=4) | ![profile](https://avatars.githubusercontent.com/u/144425658?v=4) |![profile](https://avatars.githubusercontent.com/u/144124353?v=4)|![profile](https://avatars.githubusercontent.com/u/202200191?v=4) | ![profile](https://avatars.githubusercontent.com/u/166782961?v=4) 
| BE                                     |  BE                                  |  BE                                     |  BE                                              |  BE                                | 
| [@KiSeungMin](https://github.com/KiSeungMin) |[@hyoinYang](https://github.com/hyoinYang)|[@seung-in-Yoo](https://github.com/seung-in-Yoo)  | [@wonjun-lee-fcwj245](https://github.com/wonjun-lee-fcwj245) |[@zyovn](https://github.com/zyovn) |

<br><br>

## 🔎 기술 스택

| Category             | Stack                                                                                                                                                                                                                                                                       |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Framework / Runtime  | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) ![Java](https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=java&logoColor=white) |
| Programming Language | ![Java](https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=java&logoColor=white) |
| Database / Search    | ![Amazon RDS](https://img.shields.io/badge/Amazon%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white) ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white) |
| Infrastructure       | ![AWS EC2](https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white) ![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white) ![AWS CloudWatch](https://img.shields.io/badge/AWS%20CloudWatch-FF4F8B?style=for-the-badge&logo=amazoncloudwatch&logoColor=white) ![AWS Route 53](https://img.shields.io/badge/AWS%20Route%2053-232F3E?style=for-the-badge&logo=amazonroute53&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) |
| API / Data           | ![T map API](https://img.shields.io/badge/T%20map%20API-FF1515?style=for-the-badge&logo=naver&logoColor=white) ![공공데이터포털](https://img.shields.io/badge/공공데이터포털-005BAC?style=for-the-badge&logoColor=white) |
| Authentication       | ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white) |
| CI/CD                | ![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white) |
| Version Control      | ![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white) ![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white) |
<br>


## 🔆 서비스 고안 배경
### 📖 프로젝트 개요

WAYBLE은 장애인·교통약자 사용자를 위해 맞춤형 경로 탐색, 접근성 필터 기반 장소 추천, 접근성 리뷰 공유 기능을 제공하는 배리어프리 지도 서비스입니다. <br>
사용자의 장애 유형과 이동 수단 설정에 따라 최적화된 경로를 안내하고, 누구나 접근성 정보를 등록·확인하여 모두의 이동권을 보장합니다.

### 🚦 기획의 시작
왜 장애인은 주변에서 자주 보이지 않을까? <br>
교통수단 이용의 어려움 <br>
충분히 보장되지 않는 생활 접근성 <br>
부족한 이동 편의 서비스 <br>
장애인 인터뷰에서 나온 목소리 <br>

> "매일 똑같은 식당에 가요.",
> "하루가 계획대로 되지 않아요.",
> "엘리베이터 위치 정보가 부정확해서 이동이 힘들어요."

기존 지도 서비스에는 엘리베이터·경사로·장애인 화장실 등 장애인들을 위한 확실한 정보가 불충분하고,
장애 유형별 경로 안내, 장애인들만의 접근성 리뷰 공유 기능등 편의성이 제공되지 않습니다.

### 💡 서비스 소개

**WAYBLE은**
- 장애 유형과 이동 수단 설정에 따라 UI·안내 정보 자동 최적화
- 접근성 필터 기반 장소 검색·추천
- 장애인들을 기반으로 한 참여형 접근성 리뷰 공유
- 장애인의 이동 경험 개선 및 말못하는 사소한 불편 해소
를 목표로 하는 배리어프리 지도 플랫폼입니다.

### 🎯 서비스 목표 

**장애 유형·이동 수단별 최적화된 경로 탐색**

**접근성 필터 기반 장소 추천**

**현재 위치 기반 웨이블존 추천**

**장애인 참여형 접근성 정보 수집·공유**

**배리어프리존 (웨이블존) 기반 장애인들을 위한 각 장소별 맞춤형 정보 제공**


<br><br>

## 📱 화면 구성
<img width="1920" height="1080" alt="onboardingScreen" src="https://github.com/user-attachments/assets/0177a29c-a9d4-4e3b-a862-3af45cf85812" />
<img width="1920" height="1080" alt="homeScreen" src="https://github.com/user-attachments/assets/f94b1c92-959f-4187-8e8a-f564d6f4981c" />
<img width="1920" height="1080" alt="waybleZoneScreen" src="https://github.com/user-attachments/assets/2bfc89de-92fe-4de9-a659-01c942190eb2" />
<img width="1920" height="1080" alt="findmapScreen" src="https://github.com/user-attachments/assets/4b7874fc-34a6-44ad-9522-8efafc290f8e" />

<br><br>


## 🛠 주요 기능
**1. 지도 기반 접근 가능 장소 검색**

엘리베이터, 경사로, 문턱, 장애인 화장실 등 장애인들이 필수적으로 알고싶어하는 정보들로 장소를 필터링할 수 있습니다. <br>
위치 기반 추천으로, 해당 위치를 기반으로 웨이블존을 추천해서 원하는 카테코리 (음식점,카페 등)의 배리어프리 존을 빠르고 편안하게 찾아갈 수 있습니다.

**2. 맞춤형 경로 안내**

장애 유형별로 경로 최적화 (휠체어, 시각장애, 지적장애 등) 각 상황에 맞는 최적의 경로를 안내해주는 웨이블만의 웨이블존 추천 경로가 존재합니다. <br>

웨이블 마커 (경사로, 휠체어 충전기, 지하철 역 출구의 엘리베이터 유무 등)을 활용하여 커스텀 추천인 웨이블존 추천 경로를 제공합니다. <br>

또한 웨이블존 추천경로 이외에도 T MAP에서 제공하는 일반적인 경로 또한 제공되어 웨이블존 추천경로와 일반 경로를 비교하여 사용자의 상황에 맞는 최적의 경로로 이동이 가능합니다. <br> 

대중교통 경로에선 장애인들에게 필요한 정보(지하철역-엘리베이터 위치 등, 버스-저상버스 여부 등) 제공하여 대중교통을 이용하는 장애인들이 이동하지 못하는 경우를 최소화합니다.

**3. 접근성 리뷰 작성·열람**

이용자가 남긴 접근성 중심 리뷰 확인하여 사용자는 해당 웨이블존의 리뷰를 확인하고, 직접 작성하여 각 웨이블존에 대한 사용자 경험을 추가하여 서로에게 도움을 줄 수 있습니다. <br>
웨이블존은 장애인들을 위한 직접적인 데이터 외에 사용자들의 리뷰를 더하여 장애인들이 직접 가보지않아도 웨이블존의 대략적인 정보를 알 수 있습니다.

**4. 마이 플레이스**

사용자는 나만의 장소인 웨이블존 저장을 위해 사용자가 직접 커스텀하여 찜 형식의 웨이블존을 저장할 리스트를 생성할 수 있습니다. <br>
저장한 리스트를 통해 본인이 커스텀 한 해당 리스트안에 원하는 여러 웨이블존을 넣어서 나중에도 쉽고 편리하게 사용자만의 웨이블존을 찾아가거나 저장할 수 있습니다. <br> 
저장한 웨이블존은 장소별로 리스트에서 웨이블존 조회 및 삭제가 가능하여 사용자가 장애인인만큼 원하는 웨이블존들을 쉽게 찾아갈 수 있도록 설정하였습니다.


<br><br>

## 🗃️데이터베이스 구조
<img width="920" height="443" alt="image" src="https://github.com/user-attachments/assets/2e9969cf-1ab1-4502-a262-285c3daf142c" />


<br><br>


## 🌴폴더 구조
```
wayble-server/
├── java/
│ └── com/
│ └── wayble/
│ └── server/
│ ├── admin/                 # 관리자 관련 기능
│ ├── auth/                  # 인증 및 인가 관련 기능 
│ ├── aws/                   # AWS 연동 (S3, CloudWatch 등)
│ ├── common/                # 공통 유틸, 예외 처리 등
│ ├── direction/             # 길찾기 및 경로 안내
│ ├── explore/               # 탐색 및 추천 관련
│ ├── logging/               # 로깅 설정
│ ├── review/                # 리뷰 작성 및 조회
│ ├── user/                  # 유저 관련 기능
│ ├── wayblezone/            # 웨이블존 관련 기능
│ └── ServerApplication.java 
│
├── resources/
│ ├── data/                  # 데이터 관련 리소스
│ ├── elasticsearch/         # Elasticsearch 관련 설정
│ ├── templates/             # 템플릿 파일
│ ├── application.properties # Spring Boot 환경 설정
│ ├── application_secret.yml # 민감 정보 설정
│ ├── keystore.p12           # HTTPS 인증서
│ ├── logback-spring.xml
│ ├── seocho_pedestrian.json # 서초구 보행자 데이터
│ └── wayble_markers.json    # 웨이블 마커 데이터
│
├── test/                    # 테스트 관련
│
├── wrapper/ 
├── .gitattributes
├── .gitignore
├── .coderabbit.yml
├── application.yml
├── build.gradle
├── docker-compose.yml
├── docker-els.yml
├── Dockerfile
├── Dockerfile.elasticsearch
└── gradlew
```
<br><br>

## 🌐시스템 구성도
<img width="5638" height="3182" alt="wayble_infra" src="https://github.com/user-attachments/assets/9a8bc6f5-bfd9-44d2-b5c2-96c9fdf374e0" />

<br><br>

## ⛓️API 명세

#### [🛠️Wayble 기능명세 링크](https://www.notion.so/API-21d75cf0b87b80248a0ec55c6134ad20)

<br>
