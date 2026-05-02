package com.main.prephub.user;

import com.main.prephub.job.JobCategory;
import com.main.prephub.job.JobDetail;
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

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) not null default 'USER'")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_category_id", nullable = false)
    private JobCategory jobCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_detail_id")
    private JobDetail jobDetail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Users(String username, String password, String nickname, Role role,
                 JobCategory jobCategory, JobDetail jobDetail) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : Role.USER;
        this.jobCategory = jobCategory;
        this.jobDetail = jobDetail;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (role == null) {
            role = Role.USER;
        }
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}