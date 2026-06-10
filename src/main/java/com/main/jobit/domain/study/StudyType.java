package com.main.jobit.domain.study;

// 모집글 유형. 순수 "스터디"인지, 결과물을 만드는 "프로젝트"인지 구분한다.
// 목록 필터(typeEquals)와 화면 표시에 사용되며, DB에는 EnumType.STRING으로 저장된다.
public enum StudyType {
    STUDY,    // 학습 중심 스터디 모집
    PROJECT   // 사이드/토이 프로젝트 팀원 모집
}