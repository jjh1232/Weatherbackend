# 배포 체크리스트

> 작업용 체크리스트입니다. **왜** 그런지는 `PROJECT_OVERVIEW.md` §11 을 보세요.
> 갱신 2026-09-01 (도커 전환 반영)
> 프론트 Cloudflare Pages / **백엔드·DB 는 로컬 PC + 도커 + Cloudflare Tunnel**

**오라클 ARM 은 포기했습니다.** 도쿄 리전에서 1 OCPU/6GB 까지 낮춰도 계속
"Out of host capacity" 라, 도커로 로컬 PC 에 올리고 Cloudflare Tunnel 로 노출하는
방식으로 틀었습니다. ARM 이 뚫리면 `compose.yml` 과 `.env` 만 옮기면 됩니다
(단 아키텍처가 x86_64 → arm64 라 앱 이미지는 재빌드해야 합니다).

```
[v] STEP 1  백엔드 코드
[v] STEP 2  프론트 코드
[v] STEP 0  도메인 · 저장소
[v] STEP 3  도커 구성        (mysql / redis / app)
[v] STEP 4  백엔드 배포      (Named Tunnel + 도메인 연결)
[ ] STEP 4-1 OAuth 콘솔 등록  ← 지금 여기
[ ] STEP 5  프론트 배포
[ ] STEP 6  통합 점검
```

**주소**

```
프론트   https://weave.prelaps.com          (Cloudflare Pages · 미배포)
백엔드   https://weave-api.prelaps.com      (Named Tunnel · 동작 중)
```

> ⚠ 서브도메인은 **점 하나까지만** 씁니다. Cloudflare 무료 인증서(Universal SSL)가
> 덮는 범위가 `prelaps.com` 과 `*.prelaps.com` 뿐이라, `api.weave.prelaps.com` 처럼
> 두 단계로 들어가면 인증서 밖이라 브라우저가 막습니다(유료 ACM 필요).
> 그래서 `api.weave...` 가 아니라 `weave-api...` 입니다.

---

## STEP 0 · 사전 준비 (완료)

- [x] ~~오라클 ARM 인스턴스~~ — 용량 부족으로 포기. 도커+로컬+터널로 전환
- [x] 도메인 `prelaps.com` 확보 + Cloudflare 관리
- [x] ~~저장소 통합~~ — 불필요. `weather_react` 는 최상위가 CRA 라 Pages 에 바로 연결됨
- [x] IDE 잔여물 추적 해제

## STEP 1 · 백엔드 코드 (완료)

- [x] **1-1** OAuth 토큰 전달 — 쿠키 → 리다이렉트 URL 프래그먼트 (`oauth2successfilter`)
- [x] **1-2** `app.frontend-url` 적용 — `oauth2successfilter` · `oauth2failservice`
- [x] **1-3** `WebConfig.addResourceHandlers()` — `/noticeimages/**` `/userprofileimg/**` `/userbackgroundimg/**`
- [x] **1-4** CORS 를 `app.cors.allowed-origins` 한 곳으로
- [x] **1-5** OAuth `redirect-uri` → `${APP_BASE_URL}/callback/{google,naver}`
- [x] **1-6** `userinfo` 를 쿠키 대신 **응답 헤더**로 (`tools/Userinfoheader`)
- [x] **1-7** 보안 — `application-prod.yml` / WS 오리진 제한 / `forward-headers-strategy: NATIVE`
- [x] **1-8** `spring.servlet.multipart` 크기 명시 · `MailPreviewController` 는 `@Profile("!prod")`
- [x] **1-9** Redis 비밀번호 지원 — `Redisconfig` 가 `conf` 를 만들고도 버리던 것을 수정.
      `spring.data.redis.password` 추가(기본값 빈 값이라 비번 없는 로컬 redis 도 동작)
- [x] **1-10** 업로드 확장자 화이트리스트 — `tools/ImageExtension` (png/jpg/jpeg/gif/webp).
      막지 않으면 `x.html` 이 API 도메인에서 실행되는 저장형 XSS. svg 는 스크립트를 담을 수 있어 제외
- [x] **1-11** `contentimagesave` 경로 탈출 수정 — 원본 파일명을 저장 경로에 붙이지 않음
- [x] **1-12** `@ColumnDefault` 따옴표 — 없으면 빈 DB 에서 `member` 테이블 생성 실패
- [x] **1-13** `garbagefiles()` null 가드 — `dir.list()` 가 null 이라 스케줄러가 12시간마다 사망
- [x] **1-14** `RequestRejectedHandler` — `//` 가 든 URL 이 500 대신 400 을 반환

## STEP 2 · 프론트 코드 (완료)

- [x] **2-1** `package.json` 의 `"proxy"` 제거
- [x] **2-2** `.env.development` / `.env.production` 에 `REACT_APP_API_URL`
- [x] **2-3** 상대경로 14곳 전환
- [x] **2-4** `localhost:8081` 하드코딩 22곳 치환
- [x] **2-5** 업로드 이미지 URL 을 API 도메인 기준으로
- [x] **2-6** OAuth 콜백 화면 (`customhook/oauthTokens.js`)
- [x] **2-7** `public/_redirects` — `/*  /index.html  200`
- [x] **2-8** `.gitignore` 오타 수정 + 업로드 이미지 추적 해제
- [x] **2-9** `PrivateRoute` 라우트 가드 버그 수정
- [x] **2-10** `userinfo` 응답 헤더 수신

## STEP 3 · 도커 구성 (완료)

`backend/compose.yml` 하나로 4개 컨테이너를 띄웁니다.

| 서비스 | 이미지 | 호스트 포트 | 비고 |
|---|---|---|---|
| `wt-mysql` | `mysql:8.0` | 3307 | 윈도우 MySQL80 이 3306 점유 |
| `wt-redis` | `redis:7.4-alpine` | 6380 | 윈도우 redis-server 가 6379 점유 · `requirepass` + `appendonly` |
| `wt-app` | 직접 빌드 | 9081 | 8081·8082 는 윈도우 예약 대역(8048-8147) |
| `wt-tunnel` | `cloudflare/cloudflared` | **없음** | 나가는 연결만 씀 |

- [x] `docker/initdb/01-weatherregion.sql` — 3,526행. mysql 이미지가 최초 기동 때 자동 적재.
      ⚠ **볼륨이 비어 있을 때 한 번만** 실행됨. 다시 태우려면 볼륨을 지워야 함
- [x] mysql·redis 에 `healthcheck` → app 이 `depends_on: condition: service_healthy` 로 대기.
      단순히 "컨테이너가 떴다"만 보면 MySQL 초기화 중에 앱이 죽음
- [x] `reactboot/Dockerfile` — 멀티 스테이지(maven 빌드 → `temurin:17-jre-jammy`), 594MB.
      `pom.xml` 을 `src` 보다 먼저 COPY 해서 의존성 레이어 캐시. non-root `app`(uid 1001)
- [x] `reactboot/.dockerignore` — `application-secret.yml` 이 빌드 컨텍스트에 들어가는 것 차단
- [x] 앱은 3307/6380 이 아니라 **컨테이너 내부 네트워크**로 `mysql:3306` / `redis:6379` 에 접속

> 호스트 포트(3307·6380·9081)는 내가 브라우저·툴로 들여다보려고 열어둔 것뿐입니다.
> 터널이 내부 네트워크로 직접 붙으므로 **최종적으로는 전부 닫아도 됩니다.**

**운영 명령**

```bash
cd backend
docker compose up -d          # 켜기
docker compose ps             # 상태 (healthy 확인)
docker compose logs -f app    # 로그
docker compose down           # 끄기 (볼륨=데이터는 남음)
docker compose down -v        # 볼륨까지 삭제. DB·업로드가 사라짐
docker builder prune -f       # 빌드 캐시 정리 (C 드라이브가 빠듯할 때)
```

## STEP 4 · 백엔드 배포 (완료)

- [x] `.env` 작성 (`.env.example` 참고 · **커밋 금지**)
- [x] Named Tunnel 생성 — 터널명 `weave`

```bash
# 1) 계정 인증 (브라우저 필요) -> cloudflared/cert.pem
docker run --rm -it -v "<프로젝트>/backend/cloudflared:/home/nonroot/.cloudflared" \
  cloudflare/cloudflared:latest tunnel login

# 2) 터널 생성 -> cloudflared/<터널id>.json
docker run --rm -v "...:/home/nonroot/.cloudflared" \
  cloudflare/cloudflared:latest tunnel create weave

# 3) DNS 연결 (CNAME 자동 생성)
docker run --rm -v "...:/home/nonroot/.cloudflared" \
  cloudflare/cloudflared:latest tunnel route dns weave weave-api.prelaps.com
```

- [x] `cloudflared/config.yml` 작성 — ingress 로 `weave-api.prelaps.com` → `http://app:8081`.
      마지막 줄은 반드시 `hostname` 없는 `service: http_status:404` (없으면 기동 거부)
- [x] `compose.yml` 에 `cloudflared` 서비스 추가
- [x] 검증 — `https://weave-api.prelaps.com/open/regionsearch?keyword=종로` 가 200 + 한글 JSON

> `cloudflared/cert.pem` 과 `<터널id>.json` 은 **터널을 조종할 수 있는 열쇠**입니다.
> `.gitignore` 로 막아뒀습니다. `config.yml` 은 비밀이 없어 커밋합니다.

**환경변수** (`compose.yml` 의 `environment` + `.env`)

| 변수 | 값 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` ← **빠뜨리면 스택트레이스가 그대로 나가고 메일 미리보기가 열립니다** |
| `DB_HOST` `DB_PORT` | `mysql` `3306` ← **3307 아님** |
| `DB_NAME` `DB_USERNAME` `DB_PASSWORD` | `.env` |
| `REDIS_HOST` `REDIS_PORT` `REDIS_PASSWORD` | `redis` `6379` ← **6380 아님** |
| `UPLOAD_PUBLIC_DIR` | `/opt/weathertw/uploads` — prod 에서는 **없으면 부팅 실패** |
| `APP_BASE_URL` | `https://weave-api.prelaps.com` — 메일 링크 + OAuth 콜백이 함께 따라감 |
| `APP_FRONTEND_URL` | `https://weave.prelaps.com` |
| `APP_CORS_ORIGINS` | `https://weave.prelaps.com` — 쉼표로 여러 개 · **끝 슬래시 금지** |
| `MAIL_*` `GOOGLE_*` `NAVER_*` `JWT_SECRET` `WEATHER_API_KEY` | `reactboot/application-secret.yml` 을 `/app` 에 ro 마운트해서 전달 |

## STEP 4-1 · OAuth 콘솔 등록 ← 지금 여기

- [ ] **구글** — `console.cloud.google.com` → API 및 서비스 → 사용자 인증 정보 →
      OAuth 2.0 클라이언트 ID → **승인된 리디렉션 URI** 에 추가
      `https://weave-api.prelaps.com/callback/google`
      · 기존 `http://localhost:8081/...` 은 로컬 개발용이므로 **지우지 말 것**
      · 동의 화면이 "테스트" 상태면 등록된 테스트 사용자만 로그인됩니다
- [ ] **네이버** — `developers.naver.com` → 내 애플리케이션 → API 설정
      · 서비스 URL `https://weave.prelaps.com` (프론트 · 경로 없이)
      · Callback URL `https://weave-api.prelaps.com/callback/naver`
      · 개발 상태에서는 지정한 계정만 로그인됩니다(공개하려면 검수 신청)

## STEP 5 · 프론트 배포

- [ ] `weather_react` 가 push 돼 있는지 확인
- [ ] Pages 연결 — 저장소 `jjh1232/weather_react` · 빌드 `npm run build` · 출력 `build`
      (루트 디렉터리는 비워둠 — 저장소 최상위가 곧 프로젝트)
- [ ] Pages 빌드 환경변수에 `REACT_APP_API_URL=https://weave-api.prelaps.com`
      (비워두면 `localhost:8081` 로 빌드됩니다)
- [ ] Pages 커스텀 도메인 `weave.prelaps.com` 연결
- [ ] 배포 후 `.env` 의 `APP_CORS_ORIGINS` 에서 `http://localhost:3001` 제거 → 앱 재기동

## STEP 6 · 통합 점검

- [ ] 일반 로그인 / 로그아웃
- [ ] 소셜 로그인 — 구글
- [ ] 소셜 로그인 — 네이버
- [ ] 회원가입 + 인증메일 링크 클릭 → 프론트 복귀
- [ ] 게시글 이미지 업로드 → 목록·상세에서 표시
- [ ] 프로필 사진·닉네임 변경 → 헤더에 즉시 반영 (`userinfo` 응답 헤더 경로)
- [ ] 채팅 (STOMP WebSocket) — 터널이 웹소켓을 그대로 통과시키는지 확인
- [ ] SSE 알림 (새 글 / 안 읽은 채팅 수) — 터널의 장기 연결 유지 확인
- [ ] 날씨 조회 (지역 3단계 검색)
- [ ] `/login` 등 내부 주소에서 **새로고침** → 404 안 나는지
- [ ] 관리자 페이지 접근 제어
- [ ] PC 재부팅 후 `restart: unless-stopped` 로 4개가 자동 복구되는지

---

## 배포 후에도 남는 숙제

- **`profileimagesave()` 가 파일명에 이메일을 넣습니다** — `MemberController:219`.
  확장자 없이 `uuid_이메일` 로 저장해서 공개 URL 에 가입 이메일이 노출됩니다.
  `imagesave()` 가 이미 대체 구현이므로 호출부만 바꾸면 됩니다.
- **토큰 저장 위치** — 프론트가 비-HttpOnly 쿠키에 넣습니다(XSS 시 탈취 가능).
  같은 상위 도메인이므로 리프레쉬 토큰만 `Domain=.prelaps.com; HttpOnly; Secure` 로
  옮길 수 있습니다. 바꿀 곳은 `oauth2successfilter` 한 메서드입니다.
- **Security 인가 규칙이 `Authorizationdfilter` 단독 의존** (`antMatchers("/**").permitAll()`)
- **인증 실패 응답이 200** — 토큰이 없거나 잘못돼도 빈 본문 200 이 나갑니다. 401 이 맞습니다.
- **볼륨 백업 절차 부재** — DB 와 업로드가 도커 볼륨 안에만 있습니다.
  `docker run --rm -v backend_mysql-data:/data -v .:/backup alpine tar czf /backup/db.tar.gz /data`
- **테스트 부재** — `application-test.yml` + 슬라이스 테스트 (`ImageExtensionTest` 하나뿐)
- **동작하지 않는 실습 코드 정리** — `UI/CreateReadChat.jsx` 는 서버에 없는
  `/open/ws/stomp/chat` 에 붙으려 하고, `WebSocketConfig` 의 raw WS 핸들러는
  `"open/ws"` 로 앞 슬래시가 빠져 등록조차 되지 않습니다. STOMP 로 대체된 옛 구조라 삭제 권장.
- **미사용 코드·중복 라이브러리 정리** (`PROJECT_OVERVIEW.md` §9)
