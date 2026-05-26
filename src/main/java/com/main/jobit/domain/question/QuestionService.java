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

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionCategoryRepository questionCategoryRepository;

    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "difficulty");

    @Transactional(readOnly = true)
    public QuestionPagedResponse<QuestionSummaryResponse> list(
            UUID categoryId, Difficulty difficulty, String search, Pageable pageable) {

        Pageable safe = sanitize(pageable);
        String q = (search == null || search.isBlank()) ? null : search.trim();

        Page<Question> page;
        if (q != null) {
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
        } else if (categoryId != null && difficulty != null) {
            page = questionRepository.findByQuestionCategoryIdAndDifficulty(categoryId, difficulty, safe);
        } else if (categoryId != null) {
            page = questionRepository.findByQuestionCategoryId(categoryId, safe);
        } else if (difficulty != null) {
            page = questionRepository.findByDifficulty(difficulty, safe);
        } else {
            page = questionRepository.findAll(safe);
        }
        return QuestionPagedResponse.from(page.map(QuestionSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public QuestionDetailResponse getById(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
        return QuestionDetailResponse.from(question);
    }

    @Transactional
    public QuestionDetailResponse create(QuestionCreateRequest request) {
        QuestionCategory category = questionCategoryRepository.findById(request.getQuestionCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("질문 카테고리를 찾을 수 없습니다."));
        Question question = request.toEntity(category);
        return QuestionDetailResponse.from(questionRepository.save(question));
    }

    @Transactional
    public QuestionDetailResponse update(UUID id, QuestionCreateRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
        QuestionCategory category = questionCategoryRepository.findById(request.getQuestionCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("질문 카테고리를 찾을 수 없습니다."));
        question.update(category, request.getTitle(), request.getHint(),
                request.getModelAnswer(), request.getDifficulty());
        return QuestionDetailResponse.from(question);
    }

    @Transactional
    public void delete(UUID id) {
        if (!questionRepository.existsById(id)) {
            throw new IllegalArgumentException("문제를 찾을 수 없습니다.");
        }
        questionRepository.deleteById(id);
    }

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