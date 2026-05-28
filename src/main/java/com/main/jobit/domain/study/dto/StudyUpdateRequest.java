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
 * 수정 요청. PUT 시맨틱(전체 교체)에 맞춰 생성과 동일한 필드를 받는다.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class StudyUpdateRequest {

    @NotNull
    private StudyType type;

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 2000)
    private String summary;

    @NotNull
    private StudyMode mode;

    @Min(2) @Max(20)
    private int capacity;

    @NotNull
    @FutureOrPresent
    private LocalDate deadline;

    @NotEmpty
    private Set<String> techStacks = new HashSet<>();

    @NotEmpty
    private Set<String> positions = new HashSet<>();
}
