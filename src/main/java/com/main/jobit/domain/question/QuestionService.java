package com.main.jobit.domain.question;

import com.main.jobit.domain.category.QuestionCategory;
import com.main.jobit.domain.category.QuestionCategoryRepository;
import com.main.jobit.domain.question.dto.QuestionCreateRequest;
import com.main.jobit.domain.question.dto.QuestionDetailResponse;
import com.main.jobit.domain.question.dto.QuestionSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionCategoryRepository questionCategoryRepository;

    @Transactional(readOnly = true)
    public List<QuestionSummaryResponse> getAll() {
        return questionRepository.findAll().stream()
                .map(QuestionSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionSummaryResponse> getByCategory(UUID categoryId) {
        return questionRepository.findByQuestionCategoryId(categoryId).stream()
                .map(QuestionSummaryResponse::from)
                .toList();
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
}