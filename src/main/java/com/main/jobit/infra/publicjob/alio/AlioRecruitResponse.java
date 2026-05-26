package com.main.jobit.infra.publicjob.alio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * ALIO ({@code opendata.alio.go.kr/new/v1/recruit/list.do}) POST 응답.
 * 실제 응답 구조: {@code {"result": [ { ... }, ... ]}} — result가 배열 자체.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlioRecruitResponse(
        List<Item> result,
        Integer totalCount,
        Integer pageNo,
        Integer numOfRows
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            Long recrutPblntSn,
            String recrutPbancTtl,
            String instNm,
            String ncsCdLst,
            String recrutSeNm,
            String hireTypeLst,
            String workRgnLst,
            String workRgnNmLst,
            String acbgCondNmLst,
            Integer recrutNope,
            String pbancBgngYmd,
            String pbancEndYmd,
            String srcUrl,
            String ongoingYn
    ) {}
}