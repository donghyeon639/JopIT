package com.main.jobit.domain.user;

import com.main.jobit.domain.job.JobCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// 서비스의 핵심 회원 엔티티. 일반 가입과 소셜 가입 회원을 공통으로 표현한다.
// 클래스명이 Users인 이유: "user"가 PostgreSQL 예약어라 테이블/엔티티 충돌을 피하기 위함(테이블명도 "users").
// 세터를 열지 않고 의미 있는 도메인 메서드(updateProfile/changeNickname 등)로만 상태를 바꾸는 응집형 설계.
@Entity
@Table(name = "users")
@Getter
// 외부에서 빈 엔티티를 임의 생성하지 못하도록 기본 생성자는 JPA 전용(PROTECTED)으로 제한
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {

    // PK는 자동 증가 정수 대신 UUID 사용 — 식별자 추측/스캔을 어렵게 해 보안상 이점이 있고 분산 환경 친화적.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 로그인 아이디. 유니크 제약이 걸려 있어 중복 가입을 DB 차원에서도 차단한다.
    // 소셜 가입자는 "provider_socialId" 형태로 자동 생성된 값이 들어간다(SocialAuthService 참고).
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    // BCrypt 등으로 인코딩된 비밀번호 해시. 평문은 절대 저장하지 않는다.
    // 소셜 회원은 비밀번호 로그인을 쓰지 않으므로 랜덤 UUID를 인코딩한 더미 값이 채워진다.
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    // 권한 등급. EnumType.STRING으로 저장하고 DB 기본값도 'USER'로 지정해 누락 시 안전하게 일반 사용자가 된다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) not null default 'USER'")
    private Role role;
// 사용자가 선택한 직군(백엔드/프런트엔드 등). 소셜 신규 가입 직후에는 null일 수 있어 추가 정보 입력(setup) 단계로 유도한다.
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "job_category_id")
private JobCategory jobCategory;

// 가입 시각. updatable=false로 최초 1회만 기록되고 이후 수정되지 않는다.
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

// 빌더 생성자. role이 명시되지 않으면 일반 사용자(USER)로 안전 기본값 처리.
@Builder
public Users(String username, String password, String nickname, Role role,
             JobCategory jobCategory) {
    this.username = username;
    this.password = password;
    this.nickname = nickname;
    this.role = role != null ? role : Role.USER;
    this.jobCategory = jobCategory;
}

// 영속화 직전 콜백. createdAt 자동 세팅 및 role 누락 시 USER 보정 — 빌더를 거치지 않는 경로까지 방어.
@PrePersist
void onCreate() {
    if (createdAt == null) {
        createdAt = LocalDateTime.now();
    }
    if (role == null) {
        role = Role.USER;
    }
}


    // 소셜 회원의 추가 정보 입력 단계에서 닉네임과 직군을 한 번에 갱신(setup 플로우).
    public void updateProfile(String nickname, JobCategory jobCategory) {
        this.nickname = nickname;
        this.jobCategory = jobCategory;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    // 이미 인코딩이 끝난 해시만 받는다. 평문이 들어오지 않도록 호출부에서 인코딩 책임을 진다.
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 권한 변경은 관리자 전용 기능(AdminUserController)에서만 호출되는 민감 동작.
    public void changeRole(Role role) {
        this.role = role;
    }
}