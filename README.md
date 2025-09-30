# 🏦 Account System Spring Boot

Spring Boot 기반의 계좌 관리 시스템입니다. 사용자 계좌 생성, 조회, 거래, 계좌 해지 등의 기능을 제공합니다.

## 🚀 주요 기능

### 계좌 관리
- ✅ 계좌 생성 (사용자당 최대 10개)
- ✅ 계좌 조회
- ✅ 계좌 해지 (잔액 0일 때만 가능)

### 거래 관리
- ✅ 잔액 사용 (1원 ~ 1,000,000원)
- ✅ 잔액 사용 취소
- ✅ 거래 내역 조회

### 사용자 관리
- ✅ 사용자 등록
- ✅ 사용자별 계좌 목록 조회

## 🛠️ 기술 스택

- **Java 17**
- **Spring Boot 3.5.5**
- **Spring Data JPA**
- **H2 Database** (인메모리)
- **Redis** (캐싱)
- **Swagger/OpenAPI 3.0** (API 문서화)
- **Lombok**
- **JUnit 5** (테스트)

## 📋 API 엔드포인트

### 계좌 관리
```
POST   /api/accounts          # 계좌 생성
GET    /api/accounts/{userId} # 사용자 계좌 조회
DELETE /api/accounts          # 계좌 해지
```

### 거래 관리
```
POST   /api/transactions/use   # 잔액 사용
POST   /api/transactions/cancel # 잔액 사용 취소
GET    /api/transactions/{id} # 거래 조회
```

## 🚀 실행 방법

### 1. 프로젝트 클론
```bash
git clone https://github.com/devsehyunjin/account-system-springboot.git
cd account-system-springboot
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. API 문서 확인
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 🧪 테스트 실행

### 전체 테스트
```bash
./gradlew test
```

### 특정 테스트 클래스
```bash
./gradlew test --tests AccountServiceTest
```

### 테스트 커버리지
```bash
./gradlew jacocoTestReport
```

## 📊 테스트 커버리지

- **AccountService**: 26개 테스트 케이스
- **커버리지**: 모든 public 메서드 테스트 완료
- **테스트 패턴**: Given-When-Then 구조

### 테스트 케이스 목록
- 계좌 생성 (성공/실패)
- 계좌 해지 (성공/실패)
- 사용자 계좌 조회 (성공/실패)
- 잔액 사용 (성공/실패)
- 잔액 사용 취소 (성공/실패)
- 거래 조회 (성공/실패)

## 🗄️ 데이터베이스 스키마

### User (사용자)
- `id`: 사용자 ID (PK)
- `name`: 사용자 이름
- `created_at`: 생성일시

### Account (계좌)
- `id`: 계좌 ID (PK)
- `account_number`: 계좌번호 (10자리, 유니크)
- `user_id`: 사용자 ID (FK)
- `balance`: 잔액
- `status`: 계좌 상태 (ACTIVE/CLOSED)
- `created_at`: 생성일시
- `closed_at`: 해지일시

### Transaction (거래)
- `id`: 거래 ID (PK)
- `account_id`: 계좌 ID (FK)
- `amount`: 거래 금액
- `transaction_type`: 거래 유형 (USE/CANCEL)
- `transaction_result`: 거래 결과 (SUCCESS/FAIL)
- `transaction_date`: 거래일시

## 🔧 설정

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  h2:
    console:
      enabled: true
  data:
    redis:
      host: localhost
      port: 6379
```

## 📝 비즈니스 규칙

### 계좌 생성
- 사용자당 최대 10개 계좌 생성 가능
- 계좌번호는 10자리 랜덤 생성 (중복 불가)
- 초기 잔액 설정 가능

### 계좌 해지
- 잔액이 0원일 때만 해지 가능
- 계좌 소유주만 해지 가능
- 이미 해지된 계좌는 해지 불가

### 거래
- 거래 금액: 1원 ~ 1,000,000원
- 잔액 부족 시 거래 실패
- 해지된 계좌는 거래 불가
- 거래 취소 시 원거래 금액과 일치해야 함

## 🐛 문제 해결

### Redis 연결 오류
```bash
# Redis 서버 실행
redis-server
```

### 포트 충돌
```bash
# 다른 포트로 실행
java -jar -Dserver.port=8081 build/libs/account-system-springboot-0.0.1-SNAPSHOT.jar
```

## 📈 성능 최적화

- **Redis 캐싱**: 자주 조회되는 데이터 캐싱
- **JPA 지연 로딩**: N+1 문제 방지
- **트랜잭션 최적화**: 읽기 전용 트랜잭션 활용

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 👨‍💻 개발자

**devsehyunjin**
- GitHub: [@devsehyunjin](https://github.com/devsehyunjin)

---

⭐ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요!
