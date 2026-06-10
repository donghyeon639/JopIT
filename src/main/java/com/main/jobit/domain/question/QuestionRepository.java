package com.main.jobit.domain.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// Question 영속성 레포지토리.
// 핵심: 모든 조회 메서드에 @EntityGraph(questionCategory)를 걸어 카테고리를 즉시 함께 fetch한다.
// → 목록을 DTO로 변환할 때 카테고리명을 접근해도 N+1 쿼리가 발생하지 않도록 하는 의도.
// 필터 조합(카테고리/난이도/제목검색)별로 메서드를 나눠 두고, 서비스에서 조건 유무에 따라 분기 호출한다.
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    // 조건 없음: 전체 목록(JpaRepository의 findAll을 오버라이드해 EntityGraph 적용).
    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findAll(Pageable pageable);

    // 카테고리만 필터
    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findByQuestionCategoryId(UUID questionCategoryId, Pageable pageable);

    // 난이도만 필터
    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findByDifficulty(Difficulty difficulty, Pageable pageable);

    // 카테고리 + 난이도
    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findByQuestionCategoryIdAndDifficulty(UUID questionCategoryId, Difficulty difficulty, Pageable pageable);

    // 제목 검색(대소문자 무시, 부분 일치 LIKE %title%)
    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 카테고리 + 제목 검색
    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findByQuestionCategoryIdAndTitleContainingIgnoreCase(
            UUID questionCategoryId, String title, Pageable pageable);

    // 난이도 + 제목 검색
    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findByDifficultyAndTitleContainingIgnoreCase(
            Difficulty difficulty, String title, Pageable pageable);

    // 카테고리 + 난이도 + 제목 검색(모든 필터 동시 적용)
    @EntityGraph(attributePaths = "questionCategory")
    Page<Question> findByQuestionCategoryIdAndDifficultyAndTitleContainingIgnoreCase(
            UUID questionCategoryId, Difficulty difficulty, String title, Pageable pageable);
}