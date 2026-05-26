package com.main.jobit.infra.publicjob.alio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.jobit.infra.publicjob.JobPostingFetcher;
import com.main.jobit.infra.publicjob.NormalizedJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 청년일자리지원 서비스 (opendata.alio.go.kr) 채용공시 어댑터.
 *
 * 실측한 실제 스펙(2026-05-26 기준):
 *  - 메서드: POST
 *  - 엔드포인트: /new/v1/recruit/list.do
 *  - 요청 본문: application/x-www-form-urlencoded — serviceKey, pageNo, numOfRows
 *  - 응답: application/json, 구조 {@code {"result":[ {...} ]}} (result가 배열 자체)
 */
@Slf4j
@Component
public class AlioJobPostingFetcher implements JobPostingFetcher {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Map<String, String> HIRE_TYPE_NAMES = Map.of(
            "R1010", "정규직",
            "R1020", "계약직",
            "R1030", "무기계약직",
            "R1040", "비정규직",
            "R1050", "청년인턴",
            "R1060", "청년인턴(체험형)",
            "R1070", "청년인턴(채용형)"
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;
    private final String listPath;
    private final String serviceKey;
    private final List<String> ncsCodes;

    public AlioJobPostingFetcher(
            @Value("${publicjob.alio.base-url:https://opendata.alio.go.kr}") String baseUrl,
            @Value("${publicjob.alio.list-path:/new/v1/recruit/list.do}") String listPath,
            @Value("${publicjob.alio.api-key:}") String serviceKey,
            @Value("${publicjob.alio.ncs-codes:R600020}") String ncsCodesCsv) {
        this.baseUrl = baseUrl;
        this.listPath = listPath;
        this.serviceKey = serviceKey == null ? "" : serviceKey.trim();
        this.ncsCodes = (ncsCodesCsv == null || ncsCodesCsv.isBlank())
                ? List.of()
                : java.util.Arrays.stream(ncsCodesCsv.split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).toList();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "JobIT/1.0 (+https://job-it.site)")
                .requestFactory(factory)
                .build();
    }

    @Override
    public String source() {
        return "PUBLIC_DATA";
    }

    @Override
    public boolean isConfigured() {
        return !serviceKey.isBlank();
    }

    @Override
    public List<NormalizedJob> fetchPage(int pageNo, int numOfRows) {
        // NCS 코드 미설정 시 전체. 설정 시 코드별로 호출 후 합치고 externalId 기준 중복 제거.
        if (ncsCodes.isEmpty()) {
            return parsePage(pageNo, numOfRows, null);
        }
        return ncsCodes.stream()
                .flatMap(code -> parsePage(pageNo, numOfRows, code).stream())
                .collect(java.util.stream.Collectors.toMap(
                        NormalizedJob::externalId, j -> j, (a, b) -> a,
                        java.util.LinkedHashMap::new))
                .values()
                .stream()
                .toList();
    }

    private List<NormalizedJob> parsePage(int pageNo, int numOfRows, String ncsCode) {
        String raw = fetchPageRaw(pageNo, numOfRows, ncsCode);
        if (raw == null || raw.isBlank()) return List.of();

        try {
            AlioRecruitResponse body = objectMapper.readValue(raw, AlioRecruitResponse.class);

            if (body == null || body.result() == null) {
                log.warn("ALIO 응답 파싱 결과 비어있음: pageNo={}, ncs={}, raw={}",
                        pageNo, ncsCode, snippet(raw));
                return List.of();
            }
            log.info("ALIO list.do 호출 완료: pageNo={}, ncs={}, size={}, totalCount={}",
                    pageNo, ncsCode, body.result().size(), body.totalCount());

            return body.result().stream()
                    .filter(item -> matchesNcs(item, ncsCode))   // 안전망: 응답에도 동일 코드 포함 확인
                    .map(this::normalize)
                    .filter(j -> j.externalId() != null && !j.externalId().isBlank())
                    .toList();
        } catch (Exception e) {
            log.warn("ALIO 응답 파싱 실패: {}. raw={}", e.getMessage(), snippet(raw));
            return List.of();
        }
    }

    private boolean matchesNcs(AlioRecruitResponse.Item item, String ncsCode) {
        if (ncsCode == null) return true;
        String lst = item.ncsCdLst();
        return lst != null && lst.contains(ncsCode);
    }

    /** 진단용 — ALIO 응답 원문을 그대로 반환 (NCS 필터 미적용). */
    public String fetchPageRaw(int pageNo, int numOfRows) {
        return fetchPageRaw(pageNo, numOfRows, null);
    }

    public String fetchPageRaw(int pageNo, int numOfRows, String ncsCode) {
        if (!isConfigured()) {
            log.warn("publicjob.alio.api-key 미설정. ALIO 호출 skip.");
            return null;
        }

        java.util.LinkedHashMap<String, String> params = new java.util.LinkedHashMap<>();
        params.put("serviceKey", serviceKey);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("numOfRows", String.valueOf(numOfRows));
        if (ncsCode != null && !ncsCode.isBlank()) {
            params.put("ncsCode", ncsCode);
        }
        String formBody = formEncode(params);

        log.info("ALIO POST {}{} pageNo={} numOfRows={} ncs={} (key=***)",
                baseUrl, listPath, pageNo, numOfRows, ncsCode);

        try {
            long start = System.currentTimeMillis();
            String body = restClient.post()
                    .uri(listPath)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(formBody)
                    .retrieve()
                    .body(String.class);
            long elapsed = System.currentTimeMillis() - start;
            log.info("ALIO 응답 수신: pageNo={}, length={} ({}ms)",
                    pageNo, body == null ? 0 : body.length(), elapsed);
            return body;
        } catch (Exception e) {
            log.warn("ALIO 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    private NormalizedJob normalize(AlioRecruitResponse.Item item) {
        String externalId = item.recrutPblntSn() == null ? null : String.valueOf(item.recrutPblntSn());
        String location = firstNonBlank(item.workRgnNmLst(), item.workRgnLst());
        String careerLevel = item.recrutSeNm();
        String employmentType = decodeHireType(item.hireTypeLst());

        return new NormalizedJob(
                externalId,
                nullSafe(item.recrutPbancTtl()),
                nullSafe(item.instNm()),
                location,
                careerLevel,
                employmentType,
                null,
                parseYmd(item.pbancBgngYmd()),
                parseYmd(item.pbancEndYmd()),
                item.srcUrl()
        );
    }

    private String decodeHireType(String code) {
        if (code == null || code.isBlank()) return null;
        return HIRE_TYPE_NAMES.getOrDefault(code, code);
    }

    private String formEncode(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private String snippet(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }

    private LocalDateTime parseYmd(String ymd) {
        if (ymd == null || ymd.isBlank()) return null;
        try {
            return LocalDate.parse(ymd.replaceAll("[^0-9]", ""), YMD).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }
}