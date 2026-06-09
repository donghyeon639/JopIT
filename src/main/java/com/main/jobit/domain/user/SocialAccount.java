package com.main.jobit.domain.user;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// OAuth2 소셜 로그인으로 연동된 외부 계정 정보를 저장하는 엔티티.
// 하나의 내부 Users 회원이 여러 소셜 계정(구글/카카오/깃허브)을 가질 수 있는 N:1 구조.
// (auth_provider, social_id) 복합 유니크 제약으로 동일 공급자의 같은 외부 계정이 중복 연동되는 것을 DB 차원에서 차단한다.
@Entity
@Table(name = "social_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"auth_provider", "social_id"})
})
@Getter
// JPA 프록시 생성을 위한 기본 생성자는 노출하되 외부에서 빈 객체를 만들지 못하도록 PROTECTED로 제한
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이 소셜 계정이 연결된 내부 회원. 지연 로딩으로 불필요한 조인을 피한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 소셜 로그인 공급자. EnumType.STRING으로 저장해 enum 순서가 바뀌어도 데이터가 깨지지 않게 한다.
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider provider;

    // 공급자가 발급한 외부 사용자 고유 식별자(예: 구글 sub, 카카오/깃허브 id).
    // provider와 묶여 외부 계정을 유일하게 식별하는 키 역할을 한다.
    @Column(name = "social_id", nullable = false, length = 100)
    private String socialId;

    @Builder
    public SocialAccount(Users user, AuthProvider provider, String socialId) {
        this.user = user;
        this.provider = provider;
        this.socialId = socialId;
    }
}
