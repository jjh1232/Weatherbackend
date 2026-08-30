# 배포 체크리스트

> 작업용 체크리스트입니다. **왜** 그런지는 `PROJECT_OVERVIEW.md` §11 을 보세요.
> 갱신 2026-08-30 (전수 재점검) · 프론트 Cloudflare Pages / 백엔드·DB Oracle Cloud Always Free

**코드 작업(STEP 1·2)은 전부 끝났습니다.** 백엔드 컴파일과 프론트 프로덕션 빌드가 로컬에서 통과합니다.
남은 것은 도메인·서버·계정 작업이고, 코드는 다시 손대지 않습니다(전부 환경변수).

```
[v] STEP 1  백엔드 코드
[v] STEP 2  프론트 코드
[ ] STEP 0  도메인 · 오라클 인스턴스 · 저장소 통합     ← 지금 여기
[ ] STEP 3  서버 구축
[ ] STEP 4  백엔드 배포
[ ] STEP 5  프론트 배포
[ ] STEP 6  통합 점검
```

---

## STEP 0 · 지금 걸어둘 것 (코드와 무관 · 병렬)

- [ ] 오라클 **ARM(Ampere A1)** 인스턴스 신청 — 용량 부족으로 며칠 실패하는 게 정상. 계속 재시도
- [ ] 도메인 확보 + Cloudflare 네임서버 연결
      → 프론트 `<도메인>` / 백엔드 `api.<도메인>` 로 **같은 상위 도메인의 서브도메인**
- [x] ~~저장소 통합~~ — **불필요.** `weather_react` 는 최상위가 CRA 프로젝트라 Pages 에 바로 연결됨
- [ ] **세 저장소 push** — 커밋은 됐지만 아직 원격에 안 올라감. 배포는 원격에서 가져간다
- [x] IDE 잔여물 추적 해제 — 루트 `.gitignore` 신설 + `git rm -r --cached`. 7,933개 → 246개

> ⚠ 추적 해제는 **인덱스에만 반영**돼 있습니다. 아직 커밋하지 않았습니다.

## STEP 1 · 백엔드 코드 (완료)

- [x] **1-1** OAuth 토큰 전달 — 쿠키 → 리다이렉트 URL 프래그먼트 (`oauth2successfilter`)
- [x] **1-2** `app.frontend-url` 적용 — `oauth2successfilter` · `oauth2failservice`
- [x] **1-3** `WebConfig.addResourceHandlers()` — `/noticeimages/**` `/userprofileimg/**` `/userbackgroundimg/**`
- [x] **1-4** CORS 를 `app.cors.allowed-origins` 한 곳으로
      · `WebConfig` 끝 슬래시 제거 · `MemberController` 의 `@CrossOrigin` 삭제
      · `exposedHeaders("*")` → 명시 목록 (자격증명 요청에서는 `*` 가 무시됨)
- [x] **1-5** OAuth `redirect-uri` → `${APP_BASE_URL}/callback/{google,naver}`
- [x] **1-6** `userinfo` 를 쿠키 대신 **응답 헤더**로 (5곳 → `tools/Userinfoheader` 한 곳)
- [x] **1-7** 보안 — `application-prod.yml` 로 에러 노출 차단 / WS 오리진 제한 /
      `forward-headers-strategy: NATIVE` / IP 판별 `tools/ClientIp` 로 통일
- [x] **1-8** `spring.servlet.multipart` 크기 명시(10MB·30MB) · `MailPreviewController` 는 `@Profile("!prod")`

## STEP 2 · 프론트 코드 (완료)

- [x] **2-1** `package.json` 의 `"proxy"` 제거
- [x] **2-2** `.env.development` / `.env.production` 에 `REACT_APP_API_URL`
- [x] **2-3** 상대경로 14곳 전환
- [x] **2-4** `localhost:8081` 하드코딩 22곳 치환 (`src/config/api.js` 기본값 한 줄만 남음)
- [x] **2-5** 업로드 이미지 URL 을 API 도메인 기준으로 (`UI/profileimage.js` · `List/Detachlistitem.jsx`)
- [x] **2-6** OAuth 콜백 화면 (`customhook/oauthTokens.js` — 프래그먼트 파싱 후 주소창 정리)
- [x] **2-7** `public/_redirects` — `/*  /index.html  200`
- [x] **2-8** `.gitignore` 오타 수정 + 업로드 이미지 추적 해제
- [x] **2-9** `PrivateRoute` 의 `LoginRoute` 라우트 가드 버그 수정
- [x] **2-10** `userinfo` 응답 헤더 수신 (`customhook/userinfoheader.js`)

### 로컬 검증

```bash
cd backend/reactboot && ./mvnw package -DskipTests
cd frontend/bootproject && npm run build && npx serve -s build
```

- [ ] 일반 로그인 · 소셜 로그인 · 이미지 업로드가 `serve -s build` 에서 전부 동작
      (proxy 없는 프로덕션 환경을 로컬에서 재현하는 유일한 방법)

## STEP 3 · 서버 구축

- [ ] JDK 17 설치 (ARM 이면 arm64)
- [ ] MySQL 8 설치 → `spring5fs` 스키마 생성
- [ ] **`weatherregion` 데이터 적재** (없으면 날씨 기능 전체 불가)
- [ ] **Redis 설치** — 없으면 앱이 아예 기동되지 않음. `bind 127.0.0.1` + `requirepass`
- [ ] 방화벽 — **VCN 보안목록** 과 **VM iptables** 를 둘 다

## STEP 4 · 백엔드 배포

- [ ] `./mvnw package -DskipTests` (테스트는 DB·Redis·시크릿을 모두 요구함)
- [ ] `/etc/weathertw/app.env` 작성 → systemd `EnvironmentFile` 로 주입

| 변수 | 값 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` ← **빠뜨리면 스택트레이스가 그대로 나가고 메일 미리보기가 열립니다** |
| `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD` | |
| `REDIS_HOST` `REDIS_PORT` | |
| `MAIL_USERNAME` `MAIL_PASSWORD` | Gmail 앱 비밀번호 |
| `GOOGLE_CLIENT_ID` `GOOGLE_CLIENT_SECRET` | |
| `NAVER_CLIENT_ID` `NAVER_CLIENT_SECRET` | |
| `JWT_SECRET` | `openssl rand -base64 64` |
| `WEATHER_API_KEY` | 기상청 일반 인증키(Encoding) |
| `APP_BASE_URL` | `https://api.<도메인>` — 메일 링크 + OAuth 콜백이 함께 따라감 |
| `APP_FRONTEND_URL` | `https://<도메인>` |
| `APP_CORS_ORIGINS` | `https://<도메인>` — 쉼표로 여러 개 · **끝 슬래시 금지** |
| `UPLOAD_PUBLIC_DIR` | `/opt/weathertw/uploads` — prod 에서는 **없으면 부팅 실패** |
| `APP_CONTACT_EMAIL` `MAX_FILE_SIZE` `MAX_REQUEST_SIZE` | 선택 |

- [ ] Cloudflare Tunnel 로 `api.<도메인>` HTTPS 노출 · SSL 모드 **Full (strict)**
- [ ] **구글·네이버 콘솔에 승인 리디렉션 URI 등록** ← 도메인 확정 후에만 가능
      `https://api.<도메인>/callback/google` · `https://api.<도메인>/callback/naver`

## STEP 5 · 프론트 배포

- [ ] `weather_react` 가 push 돼 있는지 확인
- [ ] Pages 연결 — 저장소 `jjh1232/weather_react` · 빌드 `npm run build` · 출력 `build`
      (루트 디렉터리는 비워둠 — 저장소 최상위가 곧 프로젝트)
- [ ] Pages 빌드 환경변수에 `REACT_APP_API_URL=https://api.<도메인>`
      (비워두면 `localhost:8081` 로 빌드됩니다)

## STEP 6 · 통합 점검

- [ ] 일반 로그인 / 로그아웃
- [ ] 소셜 로그인 — 구글
- [ ] 소셜 로그인 — 네이버
- [ ] 회원가입 + 인증메일 링크 클릭 → 프론트 복귀
- [ ] 게시글 이미지 업로드 → 목록·상세에서 표시
- [ ] 프로필 사진·닉네임 변경 → 헤더에 즉시 반영 (`userinfo` 응답 헤더 경로)
- [ ] 채팅 (STOMP WebSocket)
- [ ] SSE 알림 (새 글 / 안 읽은 채팅 수)
- [ ] 날씨 조회 (지역 3단계 검색)
- [ ] `/login` 등 내부 주소에서 **새로고침** → 404 안 나는지
- [ ] 관리자 페이지 접근 제어

---

## 배포 후에도 남는 숙제

- **토큰 저장 위치** — 프론트가 비-HttpOnly 쿠키에 넣습니다(XSS 시 탈취 가능).
  같은 상위 도메인이므로 리프레쉬 토큰만 `Domain=.<도메인>; HttpOnly; Secure; SameSite=None`
  쿠키로 옮길 수 있습니다. 바꿀 곳은 `oauth2successfilter` 한 메서드입니다.
- **Security 인가 규칙이 `Authorizationdfilter` 단독 의존** (`antMatchers("/**").permitAll()`)
- **테스트 부재** — `application-test.yml` + 슬라이스 테스트
- **동작하지 않는 실습 코드 정리** — `UI/CreateReadChat.jsx`(라우트 `readchat`) 는
  서버에 없는 `/open/ws/stomp/chat` 에 붙으려 하고, `WebSocketConfig` 의 raw WS 핸들러는
  `"open/ws"` 로 앞 슬래시가 빠져 등록조차 되지 않습니다. STOMP 로 대체된 옛 구조라 삭제 권장.
- **미사용 코드·중복 라이브러리 정리** (`PROJECT_OVERVIEW.md` §9)
