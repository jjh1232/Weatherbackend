# weathertw — 프로젝트 구조 & 기술 스택 정리

> 날씨 정보와 SNS(트위터형 타임라인)를 합친 개인 학습/포트폴리오 프로젝트.
> Spring Boot 2.7(REST API) + React 18(SPA) 분리 구조.
> 이 문서는 2026-08-23 시점의 소스를 읽고 역으로 정리했고,
> **2026-08-29 배포 준비 전수 점검**에서 실제 소스와 대조해 갱신했습니다(§9, §11).

---

## 1. 한눈에 보기

| 항목 | 내용 |
|---|---|
| 백엔드 | Spring Boot 2.7.0 / Java 17 / Maven |
| 프론트 | React 18 (CRA, react-scripts 5) / JavaScript |
| DB | MySQL 8 (`spring5fs` 스키마), Spring Data JPA + Hibernate (`ddl-auto: update`) |
| **캐시/세션 저장소** | **Redis (Lettuce)** — 날씨 캐시 · 조회수 버퍼 · STOMP 세션 상태 (§4.6) |
| 인증 | Spring Security + 자체 JWT(Access/Refresh) + OAuth2(Google, Naver) |
| 실시간 | STOMP over WebSocket(SockJS) — 채팅 / SSE — 알림 |
| 외부 API | 기상청 단기예보(초단기예보 `getUltraSrtFcst`, data.go.kr) |
| 메일 | Gmail SMTP + Thymeleaf 템플릿 (인증메일/비번찾기/탈퇴) |
| 포트 | 백엔드 `8081`, 프론트 `3001`, Redis `6379` |
| 배포(예정) | 프론트 Cloudflare Pages / 백엔드 Oracle Cloud Always Free — §11 |
| 규모 | Java 126파일 / 약 8.5k LOC, JS·JSX 167파일 / 약 24.4k LOC |

---

## 2. 저장소 구조

> 이 문서는 **`Weatherbackend` 저장소 최상위**에 있습니다. 프로젝트 전체(프론트 포함)를
> 설명하므로, 아래 경로는 두 저장소를 같은 부모 폴더에 나란히 둔 로컬 작업 폴더 기준입니다.

```
weathertw/                       # 로컬 작업 폴더 (원격 없음 · 문서 히스토리용 .git 만 남음)
├── backend/                     # ← 저장소: jjh1232/Weatherbackend  (브랜치 master)
│   ├── PROJECT_OVERVIEW.md      #   이 문서
│   ├── DEPLOY_CHECKLIST.md
│   ├── .metadata/               #   Eclipse 워크스페이스 메타데이터 (gitignore)
│   └── reactboot/               #   실제 Spring Boot 프로젝트 (pom.xml 위치)
│       ├── application-secret.yml          # ⚠ 실제 시크릿. gitignore (커밋 금지)
│       ├── application-secret.yml.example  #   채우는 방법 안내
│       └── src/main/
│           ├── java/com/example/firstproject/
│           └── resources/
│               ├── application.yml
│               ├── application-prod.yml    #   운영 전용 덮어쓰기 (§11.2-⑫)
│               └── templates/              #   메일용 Thymeleaf
└── frontend/
    └── bootproject/             # ← 저장소: jjh1232/weather_react  (브랜치 main)
        ├── package.json         #   저장소 최상위 = CRA 프로젝트 → Pages 에 바로 연결
        ├── .env.development / .env.production
        ├── public/
        │   ├── noticeimages/, userprofileimg/, userbackgroundimg/  # 업로드 (gitignore)
        │   ├── img/             #   기본 프로필·소셜 버튼 등 정적 자산
        │   └── _redirects       #   SPA 라우팅 (Pages)
        └── src/
            └── config/api.js    #   API 주소는 여기 한 곳 (§11.2-③)
```

**참고 포인트**

- **`backend/reactboot` 가 Maven 루트입니다.** `backend/` 자체는 Eclipse 워크스페이스라
  `.metadata` 가 생기는데, 지금은 추적하지 않습니다.
  (예전 문서에 `weatherboot` 로 적혀 있었으나 그건 이미 지워진 옛 프로젝트입니다.)
- **배포에 쓰는 저장소는 두 개, 각자 독립적으로 완결돼 있습니다.**
  VM 에서는 `Weatherbackend` 를 클론해 `reactboot` 로 들어가고,
  Cloudflare Pages 에는 `weather_react` 를 루트 디렉터리 지정 없이 연결합니다.
  통합할 필요가 없습니다(§11.2-⑧).
- **로컬 작업 폴더(`weathertw/`)에도 `.git` 이 있지만 원격이 없습니다.**
  옛 구조의 잔재이고 프론트 파일 108개를 중복 추적하고 있습니다. 배포에는 쓰지 않습니다.

---

## 3. 기술 스택 상세

### 3.1 백엔드 의존성 (`backend/reactboot/pom.xml`)

| 의존성 | 용도 |
|---|---|
| `spring-boot-starter-web` | REST API |
| `spring-boot-starter-data-jpa` + `mysql-connector-java` | 영속성 |
| **`spring-boot-starter-data-redis`** | **캐시 · 조회수 버퍼 · STOMP 세션 상태 (§4.6)** |
| `spring-boot-starter-security` | 인증/인가 필터체인 |
| `spring-boot-starter-oauth2-client` | 구글/네이버 소셜 로그인 |
| `jjwt 0.11.5` + `com.auth0:java-jwt 3.4.1` | JWT 발급·검증 (라이브러리 2개 혼용) |
| `spring-boot-starter-websocket` | STOMP / raw WebSocket |
| `spring-boot-starter-mail` + `starter-thymeleaf` | HTML 메일 발송 |
| `spring-boot-starter-aop` | 컨트롤러 로깅 |
| `spring-boot-starter-validation` | DTO 유효성 검증 |
| `lombok` | 보일러플레이트 제거 |

### 3.2 프론트 의존성 (`frontend/bootproject/package.json`)

| 분류 | 라이브러리 |
|---|---|
| 코어 | `react 18`, `react-dom`, `react-router-dom 6`, `react-scripts 5` |
| 서버 상태 | `@tanstack/react-query 5` (+ devtools) — 30여 개 컴포넌트에서 사용 |
| HTTP | `axios` (인터셉터로 토큰 자동 주입/갱신) |
| 실시간 | `@stomp/stompjs 7`, `stompjs 2`, `sockjs-client`, `event-source-polyfill`(SSE) |
| 스타일 | `styled-components 5` + `ThemeProvider`(다크모드), `@fortawesome/*` |
| 에디터/이미지 | `react-quill`(Quill 2), `react-cropper`, `react-easy-crop`, `react-image-crop`, `react-image-file-resizer` |
| 기타 UI | `react-modal`, `react-select`, `react-daum-postcode`(주소검색), `react-intersection-observer`(무한스크롤), `date-fns` |
| 인증 보조 | `react-cookie`, `react-session-api` |

> `stompjs`/`@stomp/stompjs`, `react-cropper`/`react-easy-crop`/`react-image-crop`처럼 같은 목적의 라이브러리가 중복 설치돼 있습니다(실험하며 붙인 흔적). 정리 여지 있음.

---

## 4. 백엔드 아키텍처

### 4.1 패키지 레이어

```
com.example.firstproject
├── ReactbootApplication.java     # 진입점
├── controller/                   # REST 엔드포인트 9개 + @RestControllerAdvice
├── Service/                      # 비즈니스 로직 (인터페이스 + Impl)
│   ├── Memberservice/            # 회원, 로그인이력, SSE
│   ├── chatService/              # 채팅방
│   ├── Followservice/            # 팔로우
│   └── mailservice/              # 메일 발송
├── Handler/                      # Service ↔ Repository 사이 한 겹 더 (DAO 성격)
├── Repository/                   # Spring Data JPA
├── Entity/                       # JPA 엔티티
├── Dto/ , Vo/                    # 요청·응답 DTO
├── configure/                    # Security / WebSocket / CORS / Cache 설정
│   ├── auth/                     # JWT 필터, OAuth2 핸들러, provider
│   ├── websocket/                # STOMP·WebSocket Config, ErrorHandler
│   └── Interceptor/              # STOMP 인터셉터
├── aop/                          # LoggingAspect (+ @NoLogging 커스텀 애노테이션)
├── CustomError/                  # CustomException, ErrorCode, ResponseDto
└── tools/                        # CommonUtil, HttpResRequestWrapper
```

**특징**: 일반적인 `Controller → Service → Repository` 3계층이 아니라 **`Controller → Service → Handler → Repository`의 4계층**입니다. `Handler`가 리포지토리 호출과 엔티티 변환을 맡습니다.

### 4.2 인증/인가 흐름 (`configure/securityconfig.java`)

세션 미사용(`STATELESS`), formLogin/httpBasic 비활성, CSRF 비활성.

```
[로그인]
POST /login          # authenticationfilter 가 setFilterProcessesUrl 을 지정하지 않아
                     # Spring Security 기본 경로 그대로다. 프론트도 /login 으로 보낸다.
  → authenticationfilter (UsernamePasswordAuthenticationFilter 상속)
      · 성공 → JWT AccessToken + RefreshToken 발급
               응답 헤더 Authorization / Refreshtoken 에 실어 보냄
               HistoryService 로 LoginHistory 기록 (IP, 성공여부)
      · 실패 → unsuccessfulAuthentication 에서 커스텀 에러 응답

[요청 인가]
모든 요청 → Authorizationdfilter (BasicAuthenticationFilter 상속)
      · Authorization: Bearer <token> 파싱 → 검증
      · 만료 임박/만료 시 Refreshtoken 헤더로 재발급 후 새 토큰을 응답 헤더로 반환

[소셜 로그인]
/oauth2/authorization/{google|naver}
  → 콜백 /callback/**  → oauth2loginservice(OAuth2UserService)
  → 성공: oauth2successfilter / 실패: oauth2failservice
  → 신규 가입자는 프론트에서 /signup/extrainfo 로 추가정보 입력

[예외 경로]
antMatchers("/admin/**").hasAnyRole("Admin")   # 유일하게 실제로 걸리는 규칙
antMatchers("/**").permitAll()                 # 나머지 전부 통과
anyRequest().authenticated()                   # ⚠ 위 줄에서 이미 다 걸려 도달 불가능(죽은 코드)

# WebSecurity.ignoring("/open/**") 는 현재 주석 처리돼 있다.
# 즉 /open/** 도 필터체인은 그대로 탄다.
```

- 권한 값: `ROLE_User`, `ROLE_Admin` (JWT claim의 `role`).
- 프론트도 `ParseJwt`로 토큰을 디코드해 `PrivateRoute`/`LoginRoute`/`NoLoginRoute`로 라우트 가드를 겁니다.
- **인가는 사실상 `Authorizationdfilter` 단독에 의존합니다.** `/admin/**` 외에는 Security 레벨 규칙이 없습니다(§9-3).
- **CORS 설정이 세 군데에 흩어져 있고 값이 서로 다릅니다** — 배포 전 정리 대상(§11.2-⑤).

| 위치 | 값 | 상태 |
|---|---|---|
| `securityconfig.corsConfigurationSource()` | `http://localhost:3001` | 실제로 동작하는 쪽 |
| `WebConfig.addCorsMappings()` | `http://localhost:3001/` | ⚠ 끝 슬래시 — Origin 헤더엔 슬래시가 없어 영원히 미매치 |
| `MemberController` 클래스 `@CrossOrigin` | `https://localhost:3000` | ⚠ 포트·프로토콜 둘 다 틀린 죽은 값. 클래스 레벨이라 전역 매핑을 덮어씀 |

- 노출 헤더로 `Authorization`, `Refreshtoken`, `userinfo`를 열어둡니다.

### 4.3 실시간 통신 — 3가지가 공존

| 방식 | 설정 파일 | 엔드포인트 | 용도 |
|---|---|---|---|
| **STOMP** (메인) | `configure/websocket/Stompconfig.java` | `/open/stomp` (SockJS) | 채팅. pub `/pub`, sub `/sub`·`/queue` |
| raw WebSocket | `configure/websocket/WebSocketConfig.java` + `Handler/Websocket/ChatHandler` | `open/ws` | 초기 실습용(단일 채팅방) |
| **SSE** | `Service/Memberservice/SseService`, `Repository/EmitterRepository` | `GET /ssesub` | 알림(새 글, 안 읽은 채팅 수) |

- STOMP는 `Stompinterceptor`(핸드셰이크)와 `StompHandler`(inbound 채널)에서 JWT를 검증하고, `ChatErrorHandler`로 에러를 내려줍니다.
- SSE는 `EventSourcePolyfill`로 헤더에 토큰을 실어 연결하며, 커스텀 이벤트 `connect` / `unreadcount` / `noticealarm`을 사용합니다.

### 4.4 날씨 조회 (`Service/WeatherServiceimpl.java`)

1. DB의 `weatherregion` 테이블에서 `시/도 + 시군구 + 읍면동` → 기상청 격자좌표(`gridx`, `gridy`) 조회.
2. `RestTemplate`으로 기상청 **초단기예보** API 호출
   `http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst`
   (base_date/base_time은 현재시각 -3시간 기준으로 계산)
3. 응답 JSON을 `frontweather` DTO로 변환.
4. `@Cacheable(value="getweather", key="#reg1+#reg2+#reg3", unless="#result==null")`로 캐싱.
   **캐시 저장소는 Redis 입니다** — `@Qualifier("redisCachemanager")`로 `configure/Redisconfig`의
   `RedisCacheManager`(TTL 59분)를 주입받습니다.
   `@Scheduled(fixedDelay=1000*60*59)` + `@CacheEvict(allEntries=true)`로 59분마다 비웁니다.
   > `configure/CashConfig`의 `ConcurrentMapCacheManager`는 **클래스 전체가 주석 처리돼 있어 쓰이지 않습니다.**
   > `@EnableCaching`은 `ReactbootApplication`에 붙어 있습니다.

### 4.5 AOP / 로깅

- `aop/LoggingAspect` — `@Around("controller()")`로 컨트롤러 실행시간 측정, `@Before`로 요청 파라미터·클라이언트 IP 로깅.
- `@NoLogging`, `@Logoutano` 커스텀 애노테이션으로 특정 메서드 로깅 제외.
- `Interceptor/LogInterceptor`도 있으나 등록 클래스(`configure/Interceptor/addInterceptors`)의 `@Configuration`이 주석 처리돼 **현재 비활성**입니다.

### 4.6 Redis — 무엇을 담고 있나

> **Redis는 선택 사항이 아닙니다.** `configure/Redisconfig`가 기동 시 `LettuceConnectionFactory`를
> 만들기 때문에 **Redis가 없으면 애플리케이션이 뜨지 않습니다.** 로컬·서버 모두 설치가 필요합니다(§8, §11.3).

| 용도 | 위치 | 키 패턴 | TTL / 정리 |
|---|---|---|---|
| 날씨 응답 캐시 | `Service/WeatherServiceimpl` | `getweather::{시도}{시군구}{읍면동}` | 59분 (스케줄러가 `@CacheEvict`) |
| 게시글 조회수 버퍼 | `tools/NoticeViewtools` | `viewcount:{noticeid}` | 1일 · **1분마다 DB 반영 후 삭제** |
| 채팅방·멤버 조회 캐시 | `Service/chatService/ChatService` | `EzmemberDto` / `EzRoomDto` 직렬화 | 1800초(30분) |
| 방별 마지막 읽은 메시지 | `Service/chatService/ChatService` | 문자열 값 | — |
| STOMP 세션 ↔ 유저·방 매핑 | `configure/Interceptor/StompHandler` | `stomp:chatroom:{roomid}:userid:{userid}` (Set)<br>`stomp:sessiontouser:{sessionid}`<br>`stomp:sessiontoroom:{sessionid}` | 연결 해제 시 직접 삭제 |

**주의점 2가지**

1. **조회수는 Redis에만 있다가 1분 주기로 DB에 넘어갑니다.** Redis가 죽거나 재시작하면
   마지막 반영 이후의 조회수는 사라집니다(치명적이진 않지만 알고 있을 것).
2. **STOMP 접속 상태가 Redis에 저장됩니다.** Redis를 비우면 접속 중인 세션 추적이 끊겨
   퇴장 처리가 안 된 유령 세션이 남습니다.

**설정 키 주의** — `application.yml`은 `spring.data.redis.*`(Spring Boot 3 표기)를 쓰는데
이 프로젝트는 **Boot 2.7**입니다. Boot 2.7의 표준 키는 `spring.redis.*`입니다.
`Redisconfig`가 `@Value("${spring.data.redis.host}")`로 **직접 읽고 자체 빈을 만들기 때문에 동작은 합니다.**
표준 자동설정을 쓰려면 키를 옮겨야 합니다. `spring.data.redis.type: redis`는 아무 의미 없는 값입니다.

### 4.7 요청 제한 (`configure/ratelimit/`)

- `RateLimitInterceptor`를 로그인 없이 부를 수 있는 조회·가입 API에만 겁니다 —
  `/open/emailcheck`, `/open/profileidcheck`, `/open/usernamefind/**`, `/open/passwordfind`,
  `/open/member/resend`, `/open/membercreate`.
- 클라이언트 IP는 `CF-Connecting-IP` → `X-Forwarded-For` 순으로 봅니다(Cloudflare 대비 완료).
- ⚠ 반면 `HistoryService.getrequestIp()`와 `aop/LoggingAspect`는 `X-Forwarded-For`만 봅니다.
  이대로 배포하면 **로그인 이력에 Cloudflare IP가 기록됩니다**(§11.2-⑨).

---

## 5. 데이터 모델

```
MemberEntity (member)
 ├─ id, username(email, unique), password(BCrypt), nickname, profileimg
 ├─ role, refreshtoken, provider/providerid(소셜), auth(인증키)
 ├─ homeaddress : Address (@Embedded)
 ├─ red / updatered (생성·수정 시각, Auditing)
 ├─ 1:N NoticeEntity, CommentEntity, Notification, MemberRoom
 └─ 1:N FollowEntity (followings: frommember / followers: tomember)

NoticeEntity (notice)   ← 게시글(트윗)
 ├─ id, username, nickname, title(unique), text, red
 ├─ N:1 MemberEntity
 ├─ 1:N CommentEntity
 └─ 1:N detachfile

CommentEntity (comment) ← 대댓글 지원
 └─ id, depth, cnum, username, nickname, text, redtime, N:1 Notice / Member

detachfile (detachfiles)
 └─ id, idx, rangeindex(본문 내 위치), filename, path, N:1 Notice

FollowEntity
 └─ id, frommember, tomember, favorite(즐겨찾기), times

Notification
 └─ id, reading(default false), message, noticeid, N:1 Member

LoginHistory
 └─ id, userid, logindt, islogin(성공여부), clientip, userdata

--- 채팅 ---
Room (room_id, roomname) extends BaseTime
 ├─ 1:N MemberRoom (참여자 조인 엔티티: membernickname, roomname)
 └─ 1:N chatmessage (sender, message, MessageType, sendDate)

--- 날씨 ---
WeatherdataEntity (weatherregion)
 └─ areacode(PK), step1/2/3(행정구역 3단계), gridx, gridy, 경위도(도/분/초/ms)
    * WeatherregionEntity 는 엔티티가 아니라 JPA Projection 인터페이스
```

---

## 6. REST API 목록

`/open/**`은 Security 필터체인을 타지 않는 **공개 API**입니다.

### 회원 — `MemberController` / `RefreshController`
| Method | Path | 설명 |
|---|---|---|
| POST | `/open/membercreate` | 회원가입 |
| GET | `/open/member/register` | 이메일 인증키 확인 |
| GET | `/open/emailcheck` | 이메일 중복 확인 |
| POST | `/login` | 로그인 (인증 필터가 가로챔 · Security 기본 경로) |
| GET | `/open/passwordfind` | 비밀번호 찾기 메일 |
| GET | `/open/logincheck` | 로그인 상태 확인 |
| GET | `/userdata` | 내 정보 조회 |
| PUT | `/memberupdate/{email}` | 회원정보 수정 |
| POST | `/memberdeletemail` | 탈퇴 확인 메일 |
| DELETE | `/memberdelete` | 회원 탈퇴 |
| GET | `/refresh` | 토큰 재발급 |

### 게시글/댓글/파일 — `MainController`
| Method | Path | 설명 |
|---|---|---|
| GET | `/open/notice` | 게시글 목록(페이징) |
| GET | `/open/noticesearch` | 제목/내용 검색 |
| GET | `/open/noticedetail/{num}` | 상세 |
| GET | `/open/count` | 전체 개수 |
| POST | `/noticecreate` | 작성 |
| PUT | `/noticeupdate/{num}` | 수정 |
| DELETE | `/noticedelete/{num}` | 삭제 |
| POST / PUT / DELETE | `/commentcreate`, `/commentupdate`, `/commentdelete/{id}` | 댓글 CRUD |
| GET | `/open/comment/{num}` | 댓글 목록 |
| POST | `/contentimage` | 본문 이미지 업로드 |
| DELETE | `/deletecontentimage` | 미사용 이미지 정리 |
| POST / GET | `/open/getdetach`, `/open/atagdown` | 첨부 다운로드 |

### 팔로우 — `FollowController`
`/follow`, `/followlist`, `/followerlist`, `/followdelete/{friendname}`, `/followcheck`,
`/favoritefollow/{friendname}`, `/favoriteunfollow/{friendname}`, `/favoritelist`, `/open/usersearch`

### 채팅 — `ChatMemberController` + `StompController`
| Method | Path | 설명 |
|---|---|---|
| POST | `/createchatroom` | 채팅방 생성 |
| GET | `/findchatroomlist` | 내 채팅방 목록 |
| GET | `/chatroomdataget` | 방 상세/메시지 |
| POST | `/chatroomexit` | 나가기 |
| POST | `/chatroominvite` | 초대 |
| STOMP | `@MessageMapping("/channel/{roomid}")` | 메시지 발행 (`/pub/channel/{roomid}`) |

### 날씨 — `Weathercontroller`
`/open/weatherdata?region=`, `/open/regionsearch?keyword=&page=`, `/open/regioncount`, `/open/pageregion`, `/weatherresult`, `/board/weatherStep.do`

### 알림 — `SSEController`
`GET /ssesub` (SseEmitter 구독), `GET /ssetest`

---

## 7. 프론트엔드 구조

### 7.1 폴더 역할 (`frontend/bootproject/src`)

| 폴더 | 역할 |
|---|---|
| `MainPage/` | 레이아웃(`MainLayout`, `Header`, `LeftSideBar`, `RightSideBar`, `Footer`), 날씨 위젯 `WeatherComponent`, 날씨 애니메이션 `WeatherObject/{Rain,Snow,Sky}Object` |
| `Noticepage/` | 게시글 작성·상세·수정 페이지 |
| `List/noticeformlist/` | **트위터형 타임라인 본체** (`Twitformex`=레이아웃, `Twitformver`=피드, `Twitcomment`, `Imageform` 등) |
| `List/` | 채팅 UI(`ChatUi`, `Chatroomlist`), 댓글 트리(`CommentTree`), 팔로우 목록(`Followmenus/`) |
| `MemberPage/` | 로그인·회원가입·아이디/비번찾기·탈퇴, OAuth2 결과 페이지 |
| `MemberPage/UserPages/` | 유저 프로필 페이지 (`UserDetail` + 탭: 게시글/사진/하이라이트) |
| `MemberPage/Memberupdata/` | 프로필 이미지 편집(Cropper, SelfDraw), 닉네임 변경 |
| `admin/` | 관리자 페이지 — 회원/게시글/댓글/채팅방 관리 |
| `customhook/` | `CreateAxios`(axios 인스턴스), `ParseJwt`, `authCheck`, `Pagenation`, `useChatroomservice`, `Admintools/*`(라우트 가드·관리자 유틸) |
| `Context/` | `SseProvider` — SSE 연결과 알림 카운트 전역 공유 |
| `UI/` | 버튼·모달·드롭다운·날씨 아이콘·페이지네이션 등 공통 컴포넌트, `Themecss`(다크/라이트 테마) |

### 7.2 라우팅 (`src/App.js`)

```
<SseProvider> → <QueryClientProvider> → <CookiesProvider> → <ThemeProvider> → <BrowserRouter>

/                     MainLayout
  ├ /                 Twitformex → Twitformver          (타임라인)
  ├ /notice           Twitformex
  │   ├ twitform, twitform/liked, imgform
  │   └ detail/:noticeid
  ├ /userpage/:profileid  UserDetail (index=Userposts, photo, highlight)
  ├ /noticecreate, /noticeupdate/:num   [LoginRoute]
  ├ /weatherregion, /chatui, /readchat …
  └ /signup/extrainfo  (소셜 로그인 추가정보)
/admin                [PrivateRoute = 관리자]
  ├ index, chatroom, comment, member, notice
  ├ room/:roomid, notice/detail/:noticeid, loginhistory
[NoLoginRoute] /membercreate, /Loginpage, /memberidfind, /memberpasswordfind
[LoginRoute]   /oauthsuccess
```

### 7.3 데이터 흐름

- **HTTP**: `CreateAxios()` 훅이 `baseURL: http://localhost:8081`, `withCredentials: true`인 axios 인스턴스를 만들고
  - 요청 인터셉터: 쿠키의 `Acesstoken`을 `Authorization: Bearer ...`로 자동 주입
  - 응답 인터셉터: 응답 헤더의 새 토큰을 쿠키에 재저장, **401이면 `/refresh` 호출 후 원 요청 자동 재시도**, 실패 시 쿠키 정리 후 재로그인 안내
- **서버 상태**: React Query 5 (`useQuery` / `useInfiniteQuery` / `useMutation`) — 타임라인 무한스크롤은 `react-intersection-observer`와 조합
- **토큰 저장소**: 쿠키 (`react-cookie`) — `Acesstoken`, `Refreshtoken`, `userinfo`, `weather`
- **테마**: 시간대 기반 자동 다크모드 (18시~6시), 1분마다 갱신 (`App.js`)

---

## 8. 로컬 실행 방법

```bash
# 1) MySQL 준비
CREATE DATABASE spring5fs;
#    weatherregion 테이블(기상청 격자좌표 데이터)이 미리 적재돼 있어야 날씨 기능이 동작

# 2) Redis 준비 (포트 6379) — 없으면 백엔드가 아예 기동되지 않는다 (§4.6)
redis-server

# 3) 시크릿 파일 작성 (최초 1회)
cd backend/reactboot
cp application-secret.yml.example application-secret.yml
#    DB / Gmail / OAuth2 / JWT / 기상청 키를 채운다. 이 파일은 gitignore 대상이다.
#    application.yml 이 optional:file:./application-secret.yml 로 읽어간다.

# 4) 백엔드 (포트 8081) — application-secret.yml 이 있는 reactboot 에서 실행할 것
./mvnw spring-boot:run

# 5) 프론트 (포트 3001)
cd frontend/bootproject
npm install
npm start
```

**시크릿은 저장소에 두지 않습니다.** 로컬은 위 `application-secret.yml`,
서버는 같은 이름의 환경변수(systemd `EnvironmentFile`)로 주입합니다.
채워야 할 항목은 `application-secret.yml.example`에 전부 나열돼 있습니다.

`${VAR}` (기본값 없음) 형태인 값은 **없으면 부팅이 실패합니다** — 이는 의도된 설계입니다.

> **테스트 실행 주의**: `mvn test` / `mvn package`는 현재 **실패합니다.**
> `ReactbootApplicationTests`가 프로필 없는 맨 `@SpringBootTest`이고 `src/test/resources`도 없어서
> MySQL·Redis·모든 시크릿이 갖춰져야만 통과합니다. 빌드만 하려면 `./mvnw package -DskipTests`.

---

## 9. 다시 손대기 전에 알아야 할 것들 (기술 부채)

> **2026-08-30 재점검 기준**입니다. 해결된 항목은 ✅ 로 남겨 둡니다(다시 돌아가지 않기 위해).

**✅ 해결됨**

- ~~업로드 경로가 4곳에 하드코딩~~ → 저장·삭제·청소·다운로드가 모두 `app.upload.public-dir`
  하나를 봅니다. `WebConfig.addResourceHandlers()` 로 서버가 직접 서빙하고,
  `prod` 프로필에서는 `UPLOAD_PUBLIC_DIR` 가 **필수**입니다(없으면 부팅 실패).
- ~~시크릿이 저장소에 그대로~~ → `application-secret.yml`(gitignore) + 환경변수로 분리.
  `${VAR}` 무기본값이라 빠지면 부팅이 실패합니다.
- ~~API 주소가 프론트에 흩어져 있음(36곳)~~ → `src/config/api.js` 한 곳(`API_BASE`·`WS_BASE`·`apiUrl`).
  `package.json` 의 `"proxy"` 도 제거했습니다. → §11.2-③
- ~~토큰 전달 방식이 두 갈래~~ → 일반 로그인은 응답 헤더, OAuth 는 URL 프래그먼트.
  **백엔드는 이제 인증 쿠키를 굽지 않습니다.** `userinfo` 도 응답 헤더입니다. → §11.2-①②
- ~~프론트 URL 이 백엔드에 하드코딩~~ → `app.frontend-url` 로 전부 통일. → §11.2-⑦
- ~~CORS 오리진이 세 군데 제각각~~ → `app.cors.allowed-origins` 하나. → §11.2-⑤
- ~~IDE 산출물이 커밋됨~~ → 루트 `.gitignore` 신설 + 추적 해제. 7,933개 → 246개. → §11.2-⑧
- ~~업로드 이미지가 커밋돼 있음~~ → `.gitignore` 오타(`.public/`)를 고치고 추적 해제.
- ~~프론트 라우트 가드 버그~~ → `LoginRoute` 가 `["ROLE_User","ROLE_Admin"].includes(role)` 로 수정됨.
- ~~클라이언트 IP 판별이 세 군데 제각각~~ → `tools/ClientIp` 하나. → §11.2-⑨
- ~~`SelectBox.jsx` 의 `https://localhost:8081`~~ → `API_BASE` 로 교체.

**남아 있는 것**

1. **Security 설정이 사실상 전부 개방** — `antMatchers("/**").permitAll()` 뒤의
   `anyRequest().authenticated()` 는 도달 불가능한 죽은 코드입니다.
   (`WebSecurity.ignoring("/open/**")` 는 현재 주석 처리됨.)
   `/admin/**` 외 모든 경로의 인가를 `Authorizationdfilter` 단독에 의존합니다.
   **배포를 막는 문제는 아니지만 가장 큰 구조적 부채입니다.**
2. **`Authorizationdfilter` 의 "토큰 없음" 분기가 비어 있습니다** — 위 1번과 같은 뿌리입니다.
   `/open/` 이 아닌 경로를 토큰 없이 부르면 `chain.doFilter()` 도 에러 응답도 없이 끝나서
   **200 + 빈 본문**이 나갑니다. 401 이 나가야 맞습니다.
   업로드 이미지 경로만 앞에서 통과시켜 급한 불은 껐지만(§11.2-④), 분기 자체는 그대로입니다.
   호출한 프론트 입장에서는 "성공했는데 데이터가 없음"으로 보여서 원인 찾기가 매우 어렵습니다.
3. **토큰 저장 위치** — 프론트가 비-HttpOnly 쿠키에 넣습니다. XSS 가 나면 그대로 털립니다.
   백엔드가 쿠키를 굽지 않게 됐으므로 `HttpOnly` 를 붙일 주체가 없어졌습니다.
   프론트·백엔드가 같은 상위 도메인이면 리프레쉬 토큰만
   `Domain=.<도메인>; HttpOnly; Secure; SameSite=None` 쿠키로 옮길 수 있습니다(§11.1).
4. **실험/미사용 코드 잔존** — `websocketex/`, `Stompex/`, `Service/test.java`,
   `customhook/Lifecycletest/`, `Statetest`, `Noticeex`, `chatex`, `/usertest` 라우트,
   `MemberController` 의 `/userdata`·`/open/logincheck`(연습용), 라이브러리 중복(§3.2).
5. **동작하지 않는 채로 남아 있는 실습 코드** — `UI/CreateReadChat.jsx`(라우트 `readchat`)가
   `/open/ws/stomp/chat` 에 붙으려 합니다. **서버에 없는 경로**입니다.
   `WebSocketConfig` 의 raw WS 핸들러는 `"open/ws"` 로 **앞 슬래시가 빠져** 등록 자체가 안 됩니다.
   STOMP(`/open/stomp`)로 대체된 옛 구조라 살리는 대신 **지우는 쪽이 맞습니다.**
   지금은 화면을 열면 StompJS 가 재연결을 무한 반복합니다.
6. **테스트가 빌드를 막음** — `ReactbootApplicationTests` 가 프로필 없는 맨 `@SpringBootTest` 라
   MySQL·Redis·시크릿이 모두 있어야 통과합니다. `application-test.yml` 을 추가하거나 `-DskipTests`.
7. **중첩 Git 저장소 3개** — 루트 / `backend/.git` / `frontend/bootproject/.git`, 원격도 3개.
   Cloudflare Pages 는 저장소 하나에 연결되므로 배포 전 통합이 필요합니다(§11.2-⑧).
8. **네이밍 일관성** — 패키지가 대소문자 혼용(`Service`, `Dto`, `configure`, `controller`),
   클래스도 `securityconfig`, `detachfile`, `chatmessage` 처럼 소문자 시작이 섞여 있습니다.

---

## 10. 기능 요약

- **회원**: 이메일 인증 가입, 로그인(JWT), 구글/네이버 소셜 로그인, 아이디·비번 찾기, 프로필 이미지 크롭 업로드, 회원 탈퇴(메일 확인)
- **게시글**: 트위터형 타임라인, 무한스크롤, Quill 에디터, 다중 이미지 첨부, 좋아요/즐겨찾기, 검색, 대댓글
- **소셜**: 팔로우/팔로워/즐겨찾기, 유저 프로필 페이지(게시글·사진·하이라이트 탭)
- **채팅**: STOMP 실시간 채팅방, 생성/초대/나가기, 방 목록
- **알림**: SSE 기반 실시간 알림(새 글, 안 읽은 채팅 수)
- **날씨**: 행정구역 3단계 검색 → 기상청 초단기예보 조회, 날씨별 배경 애니메이션(비/눈/하늘), 시간대 자동 다크모드
- **관리자**: 회원·게시글·댓글·채팅방 관리, 로그인 이력 조회

---

## 11. 배포 계획 (연습용 · 2026-08-26 결정 · **2026-08-29 전수 점검 반영**)

### 11.1 구성

```
                    사용자 브라우저
                          │
        ┌─────────────────┴─────────────────┐
        │                                   │
  Cloudflare Pages                    Oracle Cloud (Always Free VM)
  https://<앱>.pages.dev              https://api.<도메인>
  React 빌드 정적 배포                 Spring Boot(8081) + MySQL 8
        │                                   │
        └──────── axios (CORS) ─────────────┘
```

**프론트와 백엔드의 호스트가 다른 구조**입니다. 이 선택 하나 때문에 CORS·쿠키·업로드
세 가지가 전부 영향을 받습니다(§11.2). 도메인을 하나로 합치는 구성(`/api/*` 프록시)이면
셋 다 사라지지만, 여기서는 분리 배포를 연습하는 것이 목적입니다.

둘은 **같은 상위 도메인의 서브도메인**으로 잡습니다 — 예) 프론트 `weathertw.com`,
백엔드 `api.weathertw.com`. 브라우저는 서브도메인이 다르면 별개 오리진으로 보므로
CORS·쿠키 문제는 그대로 있지만, 나중에 리프레쉬 토큰을
`Domain=.weathertw.com; HttpOnly; Secure; SameSite=None` 쿠키로 옮기는 선택지가 열려 있습니다.
그때 바꿀 곳은 `oauth2successfilter` 한 메서드입니다.

| 항목 | 선택 | 메모 |
|---|---|---|
| 프론트 호스팅 | Cloudflare Pages | 빌드 `npm run build`, 출력 디렉터리 `build` |
| 백엔드 | Oracle Cloud Always Free VM | Spring Boot 실행 |
| DB | 같은 VM의 MySQL 8 | 스키마 `spring5fs`, `weatherregion` 데이터 적재 필요 |
| 프론트 HTTPS | Cloudflare 자동 | |
| 백엔드 HTTPS | **필수** | Cloudflare Tunnel 권장(§11.4) |
| 인증 토큰 | 응답 헤더(`Authorization`/`Refreshtoken`) | 도메인이 갈라져도 문제 없음 |
| `userinfo` | 응답 헤더(`userinfo`, URL 인코딩 JSON) | §11.2-② |

### 11.2 배포 전에 반드시 처리할 것

로컬에서는 멀쩡한데 **배포하는 순간 깨지는 것들**입니다.
**2026-08-30 전수 재점검 기준**이며 상태를 함께 적었습니다 — ✅ 해결 · ◐ 절반 · ❌ 미해결.

> 코드에서 할 수 있는 일은 전부 끝났습니다. 남은 ◐ 두 개는 **도메인 확정과 오라클 인스턴스**를
> 기다리는 항목이고, 코드가 아니라 계정·서버 작업입니다.

---

**① ✅ OAuth 로그인 토큰 전달** — 쿠키에서 **URL 프래그먼트**로 옮겼습니다.

`oauth2successfilter` 가 토큰을 쿠키에 심지 않고
`{프론트주소}/oauthsuccess#token=…&refresh=…&userinfo=…` 로 리다이렉트합니다.
프래그먼트는 서버로 전송되지 않으므로 서버 로그·Referer 에 남지 않습니다(쿼리스트링은 남습니다).
프론트는 `customhook/oauthTokens.js` 가 저장 직후 주소창에서 지웁니다.

> ⚠ **착지 라우트(`/oauthsuccess`)에 로그인 가드를 걸면 안 됩니다.**
> 여기 도착하는 시점에는 아직 토큰이 없습니다 — 토큰을 *만드는* 화면이기 때문입니다.
> `LoginRoute` 안에 두면 렌더 전에 `/` 로 튕기고 `useEffect` 가 돌지 않아
> 프래그먼트가 통째로 버려집니다(= 소셜 로그인이 조용히 실패).
>
> 예전에 이게 통과됐던 건 `LoginRoute` 의 조건이 **항상 참인 버그**였기 때문입니다
> (`role === "ROLE_User" || "ROLE_Admin"`). 가드를 고치는 순간 이 라우트가 막히므로
> **§9 의 라우트 가드 수정과 반드시 같이 처리해야 합니다.**
> 신규 소셜 가입자가 가는 `/signup/extrainfo` 도 같은 이유로 가드 밖에 있어야 합니다.

**② ✅ `userinfo` 를 응답 헤더로 통일**

쿠키를 굽던 곳이 다섯 군데였습니다 — `authenticationfilter`(로그인),
`Authorizationdfilter`(토큰 재발급), `MemberController` 세 곳(`/userdata`·`memberupdate`·`profileupdate`).
전부 **응답 헤더 `userinfo`**(URL 인코딩된 JSON)로 바꾸고,
만드는 코드도 `tools/Userinfoheader` 한 곳으로 모았습니다.
프론트는 `customhook/userinfoheader.js` 가 받아서 **자기 도메인 쿠키**에 넣습니다.
저장되는 이름·모양이 예전과 같아 이 값을 읽는 화면들은 손대지 않았습니다.

> 모으면서 같이 잡힌 것 — `memberupdate` 가 만드는 JSON 에만 `userid` 가 빠져 있었습니다.
> 회원정보를 수정하고 나면 `userid` 로 캐시 키를 만드는 화면(채팅방·팔로우 목록)이 조용히 깨졌습니다.
> `/userdata` 는 쿠키 이름이 `usernifo` 오타였고 값도 JSON 이 아니었습니다.

**③ ✅ 프론트 API 주소 — `src/config/api.js` 한 곳**

`package.json` 의 `"proxy"` 를 지웠고, `REACT_APP_API_URL` 로 덮어씁니다.
`API_BASE` · `WS_BASE`(http→ws, https→wss 자동) · `apiUrl()` 을 내보냅니다.
`src` 전체에 남은 `localhost:8081` 은 이 파일의 **기본값 한 줄뿐**입니다.

```
frontend/bootproject/.env.development   REACT_APP_API_URL=http://localhost:8081
frontend/bootproject/.env.production    REACT_APP_API_URL=          ← 도메인 확정 후 채움
```

`.env.production` 을 커밋하는 대신 **Cloudflare Pages 빌드 환경변수**로 넣는 편이 낫습니다.
비워두면 기본값(`localhost:8081`)으로 빌드되므로 **배포 전에 반드시 확인**하세요.

**④ ✅ 업로드 이미지**

- 저장 경로가 `app.upload.public-dir` 하나로 통일돼 있습니다(저장·삭제·청소·다운로드 전부).
- `WebConfig.addResourceHandlers()` 가 `/noticeimages/**` · `/userprofileimg/**` ·
  `/userbackgroundimg/**` 를 서버에서 직접 내보냅니다.
- 프론트는 `UI/profileimage.js` 가 **API 도메인 기준 절대 URL**을 만듭니다.
  기본 프로필 이미지만 프론트 정적 자산(`public/img/Noprofile.png`)이라 `PUBLIC_URL` 을 씁니다.
- 첨부파일 다운로드(`List/Detachlistitem.jsx`)도 `API_BASE` 기준으로 고쳤습니다.
- 윈도우 경로 기본값은 **`prod` 프로필에서 기본값 없이** 재정의했습니다.
  `UPLOAD_PUBLIC_DIR` 를 빠뜨리면 조용히 엉뚱한 곳에 쓰는 대신 **부팅이 실패**합니다.
- **`Authorizationdfilter` 가 이 경로들을 먼저 통과시킵니다.** 이게 없으면 이미지가 전부
  `200 + Content-Length: 0` 으로 나갑니다(아래).

> ⚠ **이미지를 서버가 내보내기 시작하면 인가 필터를 반드시 뚫어줘야 합니다.**
> `<img src>` 요청에는 브라우저가 `Authorization` 헤더를 **절대** 붙이지 않습니다.
> 그런데 `/userprofileimg/**` 등은 `/open/` 이 아니라서 `Authorizationdfilter` 의
> "토큰 없음" 분기로 내려가는데, 그 분기가 `chain.doFilter()` 도 에러 응답도 없이
> 그냥 끝나버립니다 → **200 인데 본문이 0 바이트.** 이미지가 통째로 안 보입니다.
>
> 예전에 안 걸렸던 이유는 프론트가 `PUBLIC_URL` 기준이라
> **리액트 개발서버가 public 폴더에서 직접 내보냈기** 때문입니다 — 백엔드를 아예 안 거쳤습니다.
> `API_BASE` 로 바꾸는 순간(=배포 준비의 핵심) 이 필터를 타게 됩니다.
> 그래서 `/refresh` 처럼 앞에서 통과시키는 분기를 넣었습니다:
>
> ```java
> else if(isuploadedimage(request.getServletPath())) {
>     chain.doFilter(request, response);
> }
> ```
>
> 참고 — 예전 형식으로 저장된 프로필 파일(`uuid_이메일`, **확장자 없음**)은
> `Content-Type: application/octet-stream` 으로 나갑니다. 스프링이 확장자로 타입을 정하기 때문입니다.
> 브라우저는 `<img>` 로는 그냥 그려주므로 실사용에 문제는 없고,
> 지금 저장 방식(`uuid.확장자`)으로 올린 파일은 `image/png` 로 정상적으로 나갑니다.

**⑤ ✅ CORS 오리진 — 한 곳으로**

`app.cors.allowed-origins`(환경변수 `APP_CORS_ORIGINS`, 쉼표 구분) 하나만 봅니다.
`securityconfig` · `WebConfig` · STOMP · SockJS 가 전부 이 값을 읽습니다.
`MemberController` 의 클래스 레벨 `@CrossOrigin("https://localhost:3000")` 은 삭제했습니다
(프로토콜·포트가 둘 다 틀린 죽은 값인데다 클래스 레벨이라 전역 설정을 덮어썼습니다).
`WebConfig` 의 끝 슬래시(`…:3001/`)도 없앴습니다 — Origin 헤더에는 슬래시가 없어 영원히 미매치였습니다.

> 같이 고친 것 — `WebConfig` 의 `exposedHeaders("*")`. `allowCredentials(true)` 인 요청에서는
> 브라우저가 `Access-Control-Expose-Headers: *` 를 무시합니다.
> 시큐리티 쪽과 같은 목록(`Authorization`·`Refreshtoken`·`userinfo`)을 명시했습니다.

**⑥ ✅ OAuth `redirect-uri` 환경변수화**

`${APP_BASE_URL:http://localhost:8081}/callback/google` · `/callback/naver`.
`APP_BASE_URL` 하나만 바꾸면 메일 인증 링크와 콜백 주소가 함께 따라갑니다.

> ❗ **콘솔 등록은 남아 있습니다.** 구글·네이버 개발자 콘솔의 "승인된 리디렉션 URI"에
> `https://api.<도메인>/callback/google` · `/callback/naver` 를 넣어야 합니다.
> **도메인이 확정된 뒤에만 가능한 작업**입니다.

**⑦ ✅ 메일·리다이렉트 주소**

`app.base-url`(메일 인증 링크) · `app.frontend-url`(프론트 복귀)로 분리 완료.
`MemberController.verifyemail()` · `oauth2successfilter` · `oauth2failservice` 가 모두 이 값을 씁니다.
`localhost` 하드코딩은 남아 있지 않습니다.

**⑧ ◐ 저장소**

*해결된 부분* — 루트 저장소에 `.gitignore` 가 **아예 없어서** 이클립스 워크스페이스 잔여물이
그대로 추적되고 있었습니다. `.gitignore` 를 만들고 추적을 해제했습니다.

```
추적 파일 7,933개  →  246개
  .metadata / .plugins   7,687개  해제
  업로드 이미지            4개    해제 (프론트 저장소 쪽은 이미 정리돼 있었음)
```

*통합은 필요 없습니다* — git 저장소가 3개이고 원격도 3개지만, **배포에 쓰는 두 개가
각자 독립적으로 완결돼 있습니다.**

| 저장소 | 원격 | 최상위 | 배포 |
|---|---|---|---|
| `backend/` | `jjh1232/Weatherbackend` | `reactboot/` | VM 에서 클론 → `cd reactboot` |
| `frontend/bootproject/` | `jjh1232/weather_react` | **`package.json`** | Pages 에 그대로 연결 |
| 루트 | `jjh1232/weather` | 문서 + 프론트 부분 미러 | 배포에 안 씀 |

`weather_react` 는 CRA 프로젝트가 저장소 최상위라 Pages 가 **루트 디렉터리 지정 없이**
바로 빌드합니다. 통합하지 않아도 배포에 지장이 없습니다.

*남은 정리(선택)* — 루트 저장소가 프론트 파일 108개를 중복 추적하고 있습니다.
배포를 막지는 않지만, 같은 파일이 두 저장소에 서로 다른 상태로 남아 헷갈립니다.
루트를 문서 전용으로 두고 그 108개를 추적 해제하는 것이 깔끔합니다.

**⑨ ✅ 프록시 뒤 클라이언트 IP**

`tools/ClientIp` 하나로 모았습니다 — `CF-Connecting-IP` → `X-Forwarded-For`(맨 앞) →
`X-Real-IP` → `getRemoteAddr()`.
요청 제한(`RateLimitInterceptor`) · 로그인 이력(`HistoryService`) · 요청 로깅(`LoggingAspect`)이
같은 값을 봅니다. 예전엔 요청 제한만 `CF-Connecting-IP` 를 보고 나머지 둘은
`X-Forwarded-For` 만 봐서, **로그인 이력에 클라우드플레어 IP 가 기록**됐습니다.

`server.forward-headers-strategy: NATIVE` 도 넣었습니다.
없으면 `request.isSecure()` 가 false 로 잡혀 서버가 만드는 리다이렉트가 `http://` 로 나갑니다.

**⑩ ◐ 백엔드 HTTPS**

코드 쪽 준비는 끝났습니다 — `WS_BASE` 가 `https` 면 자동으로 `wss` 가 되고,
`forward-headers-strategy` 로 프록시 뒤에서도 스킴을 제대로 인식합니다.
**남은 건 서버 작업**입니다(Cloudflare Tunnel · SSL 모드 Full (strict)). → §11.4

**⑪ ✅ 시크릿**

`application-secret.yml`(gitignore) + 환경변수로 전부 분리돼 있습니다.
서버에서는 systemd `EnvironmentFile=/etc/weathertw/app.env` 로 주입합니다.

**⑫ ✅ 운영 전환 — `application-prod.yml`**

로컬 편의 설정 중 배포하면 위험한 것만 되돌리는 프로필을 만들었습니다.
`SPRING_PROFILES_ACTIVE=prod` 로 켭니다.

| 항목 | 로컬 | prod |
|---|---|---|
| 에러 응답 | `include-message: always`, `include-exception: true` | 둘 다 끔 |
| SQL 로그 | `show_sql: true` | 끔 |
| 업로드 경로 | 윈도우 기본값 | `${UPLOAD_PUBLIC_DIR}` **필수**(없으면 부팅 실패) |
| 로그 레벨 | 기본 | `INFO` |
| 메일 미리보기 | 열림 | `@Profile("!prod")` 로 등록 안 됨 |

프로필과 무관하게 함께 처리한 것:

- **WebSocket 오리진** — `setAllowedOrigins("*")` / `setAllowedOriginPatterns("*")` 를
  `app.cors.allowed-origins` 로 바꿨습니다. HTTP 는 CORS 로 막아두고 WS 만 전 세계에 열려 있었습니다.
- **업로드 용량** — `max-file-size: 10MB` / `max-request-size: 30MB`.
  명시하지 않으면 기본값이 **파일 1MB** 라 요즘 휴대폰 사진 한 장에도 막힙니다.

**토큰 쿠키 플래그는 해당 없음이 됐습니다.** 이제 백엔드는 인증 쿠키를 굽지 않습니다
(토큰은 헤더·프래그먼트, `userinfo` 는 헤더). 쿠키를 만드는 쪽은 프론트 JS 라
`HttpOnly` 를 붙일 수 없습니다 — 아래 "배포 후에도 남는 숙제"로 넘깁니다.

### 11.3 Oracle Cloud Always Free 메모

| 항목 | 내용 |
|---|---|
| 인스턴스 | AMD micro 2대(각 1/8 OCPU · 1GB RAM) **또는** ARM Ampere A1 최대 4 OCPU · 24GB |
| 권장 | **ARM (사실상 필수)** — Spring Boot + MySQL + **Redis** 를 한 대에 올려야 해서 1GB로는 불가능 |
| 주의 | ARM 인스턴스는 리전 용량 부족으로 생성이 자주 실패함 → **가장 먼저 신청해 두고 재시도**할 것 |
| 스토리지 | 블록 볼륨 총 200GB |
| 아웃바운드 | 월 10TB |
| 방화벽 | **VCN 보안 목록**과 **VM 내부 iptables** 를 둘 다 열어야 함(자주 놓치는 부분) |
| ARM일 때 | JDK 17 · MySQL 8 · **Redis** 모두 arm64 빌드로 설치 |

**설치 목록** — 셋 다 필요합니다.

| 구성요소 | 비고 |
|---|---|
| JDK 17 | `java.version` 17 고정 |
| MySQL 8 | 스키마 `spring5fs` + **`weatherregion` 데이터 적재 필수**(없으면 날씨 기능 전체가 안 됨) |
| **Redis** | **없으면 애플리케이션이 아예 기동되지 않습니다** — §4.6 참고. 기존 문서에 빠져 있던 항목입니다 |

> Redis는 캐시 용도만이 아니라 **STOMP 세션 상태와 조회수 버퍼**를 들고 있습니다(§4.6).
> 외부 접근이 필요 없으므로 `bind 127.0.0.1` + `requirepass` 로 잠가 두세요.

### 11.4 Cloudflare 메모

- **SSL 모드는 Full (strict).** Flexible 은 브라우저↔CF 구간만 암호화하고
  CF↔서버는 평문이라 전송구간 암호화를 만족한다고 보기 어렵습니다.
- **백엔드는 Cloudflare Tunnel 로 빼는 것을 권장.** VM에 포트를 열지 않아도 되고
  인증서 발급·갱신을 신경 쓸 필요가 없습니다.
- **SPA 라우팅**: Pages에 `public/_redirects` 파일로 `/*  /index.html  200` 을 넣어야 합니다.
  없으면 `/login` 같은 주소에서 새로고침 시 404가 납니다.
- 요청 제한 인터셉터는 `CF-Connecting-IP` 헤더를 우선 사용하도록 이미 작성돼 있습니다
  (안 그러면 모든 사용자가 Cloudflare IP 하나로 잡혀 한 사람 때문에 전체가 막힙니다).
  로그인 이력·AOP 로깅도 같은 판별을 쓰도록 `tools/ClientIp` 로 통일했습니다 → §11.2-⑨.
- **Pages 빌드 설정**: 빌드 명령 `npm run build`, 출력 디렉터리 `build`,
  루트 디렉터리는 저장소 구성에 따라 `frontend/bootproject`.
  `REACT_APP_API_URL`은 Pages 대시보드의 **빌드 환경변수**로 넣는 것을 권장합니다.

### 11.5 법적 고지 (배포 시 의무)

- 회원가입에 **필수 동의 3종**(이용약관 · 개인정보 수집이용 · 만 14세 이상)이 붙어 있고,
  서버(`Memberform` 의 `@AssertTrue`)에서도 검증합니다. 동의 시각은 `member.agreedat` 에 기록됩니다.
- `/terms`, `/privacy` 문서 페이지가 로그인 없이 열립니다. 문의처는 `app.contact-email`.
- **수집 항목이 바뀌면 처리방침도 같이 고쳐야 합니다.** 그게 고지의 핵심입니다.

### 11.6 실행 순서 (2026-08-30 재점검 반영)

**STEP 1·2(코드)는 전부 끝났습니다.** 로컬에서 백엔드 컴파일과 프론트 프로덕션 빌드가
둘 다 통과합니다. 남은 것은 계정·서버 작업입니다.

```
[끝남]  STEP 1  백엔드 코드      §11.2 ①②④⑤⑥⑦⑨⑫
[끝남]  STEP 2  프론트 코드      §11.2 ③④ + 라우트 가드
[대기]  STEP 0  도메인 확보 · 오라클 ARM 인스턴스 · 저장소 통합
[대기]  STEP 3  서버 구축        JDK 17 · MySQL 8 · Redis
[대기]  STEP 4  백엔드 배포      Tunnel · systemd · OAuth 콘솔 등록
[대기]  STEP 5  프론트 배포      Cloudflare Pages
[대기]  STEP 6  통합 점검
```

---

**STEP 0 — 지금 걸어둘 것 (코드와 무관, 병렬)**

1. **오라클 ARM 인스턴스 신청** — 용량 부족으로 며칠씩 실패하는 게 정상입니다. 계속 재시도.
2. **도메인 확보 + Cloudflare 네임서버 연결** — 이게 정해져야
   `APP_BASE_URL` · `APP_FRONTEND_URL` · `APP_CORS_ORIGINS` · `REACT_APP_API_URL` ·
   OAuth 콘솔 승인 URI 가 전부 확정됩니다. 프론트 `weathertw.com` / 백엔드 `api.weathertw.com` 처럼
   **같은 상위 도메인의 서브도메인**으로 잡습니다.
3. **저장소 통합 방향 결정** — Pages 는 저장소 하나에만 연결됩니다(§11.2-⑧).
   IDE 잔여물 추적 해제는 이미 끝났습니다.

**STEP 1 · 2 — 코드 (완료)**

무엇을 어떻게 고쳤는지는 §11.2 에 항목별로 적어 두었습니다.
도메인이 바뀌어도 **코드는 다시 손대지 않습니다** — 전부 환경변수로 빠져 있습니다.

로컬 검증은 이 두 가지로 합니다.

```bash
# 백엔드
cd backend/reactboot && ./mvnw package -DskipTests

# 프론트 — proxy 없는 프로덕션 환경을 로컬에서 재현하는 유일한 방법
cd frontend/bootproject && npm run build && npx serve -s build
```

**STEP 3 — 서버 구축**

- VM 에 JDK 17 · MySQL 8 · **Redis** 설치 (ARM 이면 전부 arm64 빌드)
- `spring5fs` 스키마 생성 + **`weatherregion` 데이터 적재**(없으면 날씨 기능 전체가 안 됨)
- Redis 없으면 애플리케이션이 아예 기동되지 않습니다(§4.6). `bind 127.0.0.1` + `requirepass`
- 방화벽은 **VCN 보안목록과 VM 내부 iptables 를 둘 다**

**STEP 4 — 백엔드 배포**

- `./mvnw package -DskipTests` (테스트는 DB·Redis·시크릿을 모두 요구합니다 — §9-10)
- systemd `EnvironmentFile=/etc/weathertw/app.env` 로 주입 — 목록은 아래
- **Cloudflare Tunnel** 로 `api.<도메인>` HTTPS 노출 · SSL 모드 **Full (strict)**
- **구글·네이버 콘솔에 `https://api.<도메인>/callback/google` · `/callback/naver` 등록**

`/etc/weathertw/app.env` 에 들어갈 값:

| 변수 | 비고 |
|---|---|
| `SPRING_PROFILES_ACTIVE=prod` | **필수.** 없으면 에러 스택트레이스가 그대로 나가고 메일 미리보기가 열립니다 |
| `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD` | |
| `REDIS_HOST` `REDIS_PORT` | |
| `MAIL_USERNAME` `MAIL_PASSWORD` | Gmail 앱 비밀번호 |
| `GOOGLE_CLIENT_ID` `GOOGLE_CLIENT_SECRET` | |
| `NAVER_CLIENT_ID` `NAVER_CLIENT_SECRET` | |
| `JWT_SECRET` | `openssl rand -base64 64` |
| `WEATHER_API_KEY` | 기상청 일반 인증키(Encoding) |
| `APP_BASE_URL=https://api.<도메인>` | 메일 인증 링크 + **OAuth 콜백 주소**가 함께 따라갑니다 |
| `APP_FRONTEND_URL=https://<도메인>` | 로그인/인증 후 복귀 주소 |
| `APP_CORS_ORIGINS=https://<도메인>` | 쉼표로 여러 개. **끝 슬래시 금지** |
| `UPLOAD_PUBLIC_DIR=/opt/weathertw/uploads` | prod 에서는 **없으면 부팅 실패** |
| `APP_CONTACT_EMAIL` | 선택 |
| `MAX_FILE_SIZE` `MAX_REQUEST_SIZE` | 선택(기본 10MB / 30MB) |

**STEP 5 — 프론트 배포**

- 저장소 통합 확인(§11.2-⑧) — Pages 는 저장소 하나에 연결됩니다
- Pages 연결: 빌드 `npm run build` · 출력 `build` · 루트 `frontend/bootproject`
- 빌드 환경변수 `REACT_APP_API_URL=https://api.<도메인>`
  (`.env.production` 에 커밋하는 것보다 이쪽이 낫습니다)

**STEP 6 — 통합 점검**

일반 로그인 / 로그아웃 · 소셜 로그인(구글·네이버) · 회원가입 + 인증메일 링크 →
프론트 복귀 · 게시글 이미지 업로드 후 목록·상세 표시 · 프로필 사진 변경 ·
채팅(STOMP) · SSE 알림 · 날씨 3단계 검색 · `/login` 새로고침 404 여부 · 관리자 페이지 접근 제어.

---

> **핵심 요약**: 코드는 도메인을 모르는 채로도 끝낼 수 있었고, 실제로 끝냈습니다.
> 이제 도메인 문자열 하나가 정해지면 환경변수만 채우면 됩니다.
