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

@Service
@RequiredArgsConstructor
public class StudyJoinRequestService {

    private final StudyRepository studyRepository;
    private final StudyJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudyJoinRequestResponse apply(UUID studyId, StudyApplyRequest req, String username) {
        Study study = findStudy(studyId);
        Users applicant = findUser(username);

        if (study.isOwnedBy(applicant.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "본인이 작성한 모집글에는 신청할 수 없습니다.");
        }
        if (study.isClosed()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 마감된 모집글입니다.");
        }
        if (joinRequestRepository.existsByStudyIdAndApplicantId(studyId, applicant.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신청한 모집글입니다.");
        }
        long accepted = joinRequestRepository.countByStudyIdAndStatus(studyId, JoinRequestStatus.ACCEPTED);
        if (accepted >= study.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "정원이 모두 찼습니다.");
        }

        StudyJoinRequest saved = joinRequestRepository.save(
                StudyJoinRequest.builder()
                        .study(study)
                        .applicant(applicant)
                        .message(req.getMessage())
                        .build());
        return StudyJoinRequestResponse.from(saved);
    }

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

    @Transactional
    public StudyJoinRequestResponse decide(UUID studyId, UUID appId, JoinRequestAction action, String username) {
        Study study = findStudy(studyId);
        Users currentUser = findUser(username);
        if (!study.isOwnedBy(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 신청을 처리할 수 있습니다.");
        }

        StudyJoinRequest jr = joinRequestRepository.findById(appId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "신청을 찾을 수 없습니다."));
        if (!jr.getStudy().getId().equals(studyId)) {
            // 경로의 studyId와 신청이 가리키는 스터디가 다른 경우 (보안상 NOT_FOUND로 응답)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신청을 찾을 수 없습니다.");
        }
        if (!jr.isPending()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 신청입니다.");
        }

        if (action == JoinRequestAction.ACCEPT) {
            long accepted = joinRequestRepository.countByStudyIdAndStatus(studyId, JoinRequestStatus.ACCEPTED);
            if (accepted >= study.getCapacity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "정원이 모두 찼습니다.");
            }
            jr.accept();
        } else {
            jr.reject();
        }
        return StudyJoinRequestResponse.from(jr);
    }

    // ===== 내부 헬퍼 =====

    private Study findStudy(UUID id) {
        return studyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "스터디를 찾을 수 없습니다."));
    }

    private Users findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."));
    }
}
