package com.main.jobit.domain.study.dto;

import com.main.jobit.domain.study.StudyMode;
import com.main.jobit.domain.study.StudyType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * 모집글 생성 요청 DTO. 컨트롤러에서 @Valid로 검증되며, 메시지는 그대로 프론트에 노출되는 한국어 안내문이다.
 * capacity 2~20, 마감일은 오늘 이후, 기술스택/포지션은 1개 이상 등 비즈니스 제약을 Bean Validation으로 선언한다.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class StudyCreateRequest {

    @NotNull(message = "유형을 선택해주세요.")
    private StudyType type;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 120, message = "제목은 120자 이내로 입력해주세요.")
    private String title;

    @NotBlank(message = "소개를 입력해주세요.")
    @Size(max = 2000, message = "소개는 2,000자 이내로 입력해주세요.")
    private String summary;

    @NotNull(message = "진행 방식을 선택해주세요.")
    private StudyMode mode;

    // 정원은 작성자 본인을 제외한 모집 인원 개념. 너무 작거나 큰 모집을 막기 위해 2~20으로 제한.
    @Min(value = 2, message = "정원은 2명 이상이어야 합니다.")
    @Max(value = 20, message = "정원은 20명 이하여야 합니다.")
    private int capacity;

    @NotNull(message = "마감일을 선택해주세요.")
    @FutureOrPresent(message = "마감일은 오늘 이후로 설정해주세요.")
    private LocalDate deadline;

    // Set으로 받아 동일 태그 중복 입력을 자연스럽게 제거. 최소 1개는 선택하도록 강제.
    @NotEmpty(message = "기술 스택을 1개 이상 선택해주세요.")
    private Set<String> techStacks = new HashSet<>();

    @NotEmpty(message = "포지션을 1개 이상 선택해주세요.")
    private Set<String> positions = new HashSet<>();
}
