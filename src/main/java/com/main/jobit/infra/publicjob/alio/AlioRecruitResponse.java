package com.main.jobit.infra.publicjob.alio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


// @JsonIgnoreProperties(ignoreUnknown=true): 응답에 우리가 안 쓰는 필드가 추가돼도 역직렬화 실패하지 않도록.
//   외부 API 스키마 변경에 대한 내성을 확보하는 안전장치.
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlioRecruitResponse(
        List<Item> result,     // 실제 채용 항목 배열(응답 본문에서 "result"가 배열 자체)
        Integer totalCount,    // 전체 건수(페이징 판단용)
        Integer pageNo,        // 현재 페이지 번호
        Integer numOfRows      // 페이지당 행 수
) {
    // 채용 1건. 필드명은 ALIO 원본 키를 그대로 따른다(가독성보다 매핑 정확성 우선).
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            Long recrutPblntSn,     // 채용 공고 일련번호(고유 ID로 사용)
            String recrutPbancTtl,  // 공고 제목
            String instNm,          // 기관명
            String ncsCdLst,        // NCS 분류 코드 목록(직무 필터링에 사용)
            String recrutSeNm,      // 채용 구분명(신입/경력 등)
            String hireTypeLst,     // 고용 형태 코드(R10xx) — 어댑터에서 한글로 디코드
            String workRgnLst,      // 근무 지역 코드 목록
            String workRgnNmLst,    // 근무 지역명 목록
            String acbgCondNmLst,   // 학력 조건명
            Integer recrutNope,     // 채용 인원
            String pbancBgngYmd,    // 공고 시작일(yyyyMMdd 문자열)
            String pbancEndYmd,     // 공고 마감일(yyyyMMdd 문자열)
            String srcUrl,          // 원문/지원 링크
            String ongoingYn        // 진행중 여부(Y/N)
    ) {}
}