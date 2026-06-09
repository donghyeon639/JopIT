package com.main.jobit.domain.question;

import com.main.jobit.domain.category.QuestionCategory;
import com.main.jobit.domain.category.QuestionCategoryRepository;
import com.main.jobit.domain.question.dto.QuestionCreateRequest;
import com.main.jobit.domain.question.dto.QuestionDetailResponse;
import com.main.jobit.domain.question.dto.QuestionPagedResponse;
import com.main.jobit.domain.question.dto.QuestionSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

// 문제 도메인 비즈니스 로직. 목록/상세 조회는 일반·관리자 컨트롤러가 공유하고,
// 생성/수정/삭제는 관리자 컨트롤러에서만 호출한다.
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionCategoryRepository questionCategoryRepository;

    // 페이지 크기 상한 — 클라이언트가 size를 크게 보내도 50으로 제한해 과도한 조회를 막는다.
    private static final int MAX_PAGE_SIZE = 50;
    // 정렬 허용 필드 화이트리스트 — 임의 컬럼명으로 정렬해 SQL/성능 문제를 일으키지 못하도록 제한.
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "difficulty");

    // 문제 목록 조회(읽기 전용 트랜잭션). categoryId/difficulty/search는 모두 선택적 필터.
    // 어떤 조합이 들어오든 적절한 레포지토리 메서드로 분기해 호출한다.
    @Transactional(readOnly = true)
    public QuestionPagedResponse<QuestionSummaryResponse> list(
            UUID categoryId, Difficulty difficulty, String search, Pageable pageable) {

        Pageable safe = sanitize(pageable); // 정렬 화이트리스트 + 크기 상한 적용
        // 검색어 정규화: null/공백이면 검색 안 함(null), 아니면 trim한 값으로.
        String q = (search == null || search.isBlank()) ? null : search.trim();

        // 아래 분기는 (검색어 유무) → (카테고리/난이도 조합) 순으로 가장 구체적인 조건부터 매칭한다.
        Page<Question> page;
        if (q != null) { // 검색어가 있는 경우 (제목 부분검색 메서드 계열)
            if (categoryId != null && difficulty != null) {
                page = questionRepository.findByQuestionCategoryIdAndDifficultyAndTitleContainingIgnoreCase(
                        categoryId, difficulty, q, safe);
            } else if (categoryId != null) {
                page = questionRepository.findByQuestionCategoryIdAndTitleContainingIgnoreCase(
                        categoryId, q, safe);
            } else if (difficulty != null) {
                page = questionRepository.findByDifficultyAndTitleContainingIgnoreCase(difficulty, q, safe);
            } else {
                page = questionRepository.findByTitleContainingIgnoreCase(q, safe);
            }
        } else if (categoryId != null && difficulty != null) { // 검색어 없음, 카테고리+난이도 필터
            page = questionRepository.findByQuestionCategoryIdAndDifficulty(categoryId, difficulty, safe);
        } else if (categoryId != null) {
            page = questionRepository.findByQuestionCategoryId(categoryId, safe);
        } else if (difficulty != null) {
            page = questionRepository.findByDifficulty(difficulty, safe);
        } else {
            page = questionRepository.findAll(safe); // 필터 전혀 없음
        }
        // 엔티티 페이지 → 요약 DTO 페이지로 매핑 후 커스텀 페이지 응답으로 감싼다(목록이라 모범답안 제외).
        return QuestionPagedResponse.from(page.map(QuestionSummaryResponse::from));
    }

    // 단건 상세 조회. 없으면 IllegalArgumentException(전역 예외 핸들러가 적절한 상태코드로 변환).
    @Transactional(readOnly = true)
    public QuestionDetailResponse getById(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
        return QuestionDetailResponse.from(question);
    }

    // 문제 생성(관리자). 참조 카테고리가 실제로 존재하는지 먼저 검증한 뒤 저장한다.
    @Transactional
    public QuestionDetailResponse create(QuestionCreateRequest request) {
        QuestionCategory category = questionCategoryRepository.findById(request.getQuestionCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("질문 카테고리를 찾을 수 없습니다."));
        Question question = request.toEntity(category);
        return QuestionDetailResponse.from(questionRepository.save(question));
    }

    // 문제 수정(관리자). 대상 문제와 새 카테고리 둘 다 존재 검증 후 도메인 update로 반영.
    @Transactional
    public QuestionDetailResponse update(UUID id, QuestionCreateRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
        QuestionCategory category = questionCategoryRepository.findById(request.getQuestionCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("질문 카테고리를 찾을 수 없습니다."));
        // 영속 상태 엔티티 변경 → 트랜잭션 커밋 시 더티 체킹으로 UPDATE (별도 save 불필요)
        question.update(category, request.getTitle(), request.getHint(),
                request.getModelAnswer(), request.getDifficulty());
        return QuestionDetailResponse.from(question);
    }

    // 문제 삭제(관리자). 존재하지 않으면 404성 예외를 던지도록 먼저 existsById로 확인.
    @Transactional
    public void delete(UUID id) {
        if (!questionRepository.existsById(id)) {
            throw new IllegalArgumentException("문제를 찾을 수 없습니다.");
        }
        questionRepository.deleteById(id);
    }

    // 클라이언트가 보낸 Pageable을 안전하게 정제한다.
    // 1) 정렬: 화이트리스트(createdAt/difficulty)에 없는 정렬 키는 전부 제거.
    // 2) 정렬이 비면 기본값 createdAt DESC(최신순) 적용.
    // 3) 페이지 크기: 1~MAX_PAGE_SIZE(50) 범위로 clamp해 과도한 조회 방지.
    private Pageable sanitize(Pageable raw) {
        Sort sanitized = Sort.by(
                raw.getSort().stream()
                        .filter(order -> ALLOWED_SORT_FIELDS.contains(order.getProperty()))
                        .toList()
        );
        if (sanitized.isEmpty()) {
            sanitized = Sort.by(Sort.Order.desc("createdAt"));
        }
        int size = Math.clamp(raw.getPageSize(), 1, MAX_PAGE_SIZE);
        return PageRequest.of(raw.getPageNumber(), size, sanitized);
    }
}