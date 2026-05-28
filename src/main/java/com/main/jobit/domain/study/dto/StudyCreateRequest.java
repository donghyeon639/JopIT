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

    @Min(value = 2, message = "정원은 2명 이상이어야 합니다.")
    @Max(value = 20, message = "정원은 20명 이하여야 합니다.")
    private int capacity;

    @NotNull(message = "마감일을 선택해주세요.")
    @FutureOrPresent(message = "마감일은 오늘 이후로 설정해주세요.")
    private LocalDate deadline;

    @NotEmpty(message = "기술 스택을 1개 이상 선택해주세요.")
    private Set<String> techStacks = new HashSet<>();

    @NotEmpty(message = "포지션을 1개 이상 선택해주세요.")
    private Set<String> positions = new HashSet<>();
}
