package com.main.jobit.domain.study;

import com.main.jobit.domain.study.dto.StudyApplyRequest;
import com.main.jobit.domain.study.dto.StudyJoinRequestResponse;
import com.main.jobit.domain.user.UserRepository;
import com.main.jobit.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * 스터디 참여 신청의 생명주기를 담당하는 서비스 — 신청(apply), 신청자 목록(listForOwner), 수락/거절(decide).
 * 신청 상태 전이(PENDING → ACCEPTED/REJECTED)와 정원·권한·중복에 대한 모든 검증이 이 클래스에 모여 있다.
 */
@Service
@RequiredArgsConstructor
public class StudyJoinRequestService {

    private final StudyRepository studyRepository;
    private final StudyJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;

    // 참여 신청. 아래 가드를 모두 통과해야 PENDING 신청이 생성된다. 위반 시 모두 409(CONFLICT).
    @Transactional
    public StudyJoinRequestResponse apply(UUID studyId, StudyApplyRequest req, String username) {
        Study study = findStudy(studyId);
        Users applicant = findUser(username);

        // 1) 본인 모집글에는 신청 불가 — 작성자는 이미 참여자.
        if (study.isOwnedBy(applicant.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "본인이 작성한 모집글에는 신청할 수 없습니다.");
        }
        // 2) 마감된 글에는 신청 불가.
        if (study.isClosed()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 마감된 모집글입니다.");
        }
        // 3) 중복 신청 차단(애플리케이션 레벨 사전 검사. 최종 보장은 DB 유니크 제약).
        if (joinRequestRepository.existsByStudyIdAndApplicantId(studyId, applicant.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신청한 모집글입니다.");
        }
        // 4) 정원 초과 차단. 정원은 ACCEPTED 기준으로 카운트(PENDING은 정원에 포함하지 않음).
        long accepted = joinRequestRepository.countByStudyIdAndStatus(studyId, JoinRequestStatus.ACCEPTED);
        if (accepted >= study.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "정원이 모두 찼습니다.");
        }

        // 모든 검증 통과 → PENDING 상태로 신청 저장(상태는 엔티티 빌더가 고정).
        StudyJoinRequest saved = joinRequestRepository.save(
                StudyJoinRequest.builder()
                        .study(study)
                        .applicant(applicant)
                        .message(req.getMessage())
                        .build());
        return StudyJoinRequestResponse.from(saved);
    }

    // 신청자 목록 조회(작성자 전용). applicant를 JOIN FETCH로 가져와 DTO 변환 시 LazyInitialization 회피.
    @Transactional(readOnly = true)
    public List<StudyJoinRequestResponse> listForOwner(UUID studyId, String username) {
        Study study = findStudy(studyId);
        Users currentUser = findUser(username);
        if (!study.isOwnedBy(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 신청자 목록을 볼 수 있습니다.");
        }
        return joinRequestRepository.findByStudyIdWithApplicant(studyId).stream()
                .map(StudyJoinRequestResponse::from)
                .toList();
    }

    // 신청 수락/거절 — 상태 머신의 전이 지점. 검증 순서가 중요하므로 단계별로 가드한다.
    @Transactional
    public StudyJoinRequestResponse decide(UUID studyId, UUID appId, JoinRequestAction action, String username) {
        Study study = findStudy(studyId);
        Users currentUser = findUser(username);
        // 작성자만 신청을 처리할 수 있다.
        if (!study.isOwnedBy(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 신청을 처리할 수 있습니다.");
        }

        StudyJoinRequest jr = joinRequestRepository.findById(appId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "신청을 찾을 수 없습니다."));
        // 경로의 studyId와 신청이 가리키는 스터디가 다른 경우 — 다른 스터디의 신청을 조작하려는 시도일 수 있어
        // 존재를 드러내지 않도록 403이 아닌 NOT_FOUND로 응답(IDOR 방지).
        if (!jr.getStudy().getId().equals(studyId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신청을 찾을 수 없습니다.");
        }
        // 이미 ACCEPTED/REJECTED로 확정된 신청은 재처리 불가 — PENDING에서만 전이 허용(멱등성 X, 명시적 충돌).
        if (!jr.isPending()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 신청입니다.");
        }

        if (action == JoinRequestAction.ACCEPT) {
            // 수락 시점에 다시 정원을 확인 — apply 이후 다른 신청들이 먼저 수락되어 정원이 찼을 수 있으므로.
            long accepted = joinRequestRepository.countByStudyIdAndStatus(studyId, JoinRequestStatus.ACCEPTED);
            if (accepted >= study.getCapacity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "정원이 모두 찼습니다.");
            }
            jr.accept();   // 더티 체킹으로 커밋 시 status=ACCEPTED 반영
        } else {
            jr.reject();   // REJECT — 거절은 정원과 무관하므로 추가 검사 없음
        }
        return StudyJoinRequestResponse.from(jr);
    }

    // ===== 내부 헬퍼 =====

    // 대상 스터디 조회. 없으면 404.
    private Study findStudy(UUID id) {
        return studyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."));
    }

    // 인증 사용자 조회. 토큰은 있으나 사용자가 없으면 401.
    private Users findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."));
    }
}
