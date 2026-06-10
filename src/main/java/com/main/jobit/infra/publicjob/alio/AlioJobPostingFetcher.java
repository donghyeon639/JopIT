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

    // ALIO 날짜 문자열(yyyyMMdd) 파싱용 포맷터.
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 고용형태 코드(R10xx) -> 사람이 읽을 한글 라벨 매핑. 응답엔 코드만 오므로 여기서 디코드.
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
    private final String serviceKey;          // 외부 API 인증 키. 절대 하드코딩 금지 — 설정에서 주입.
    private final List<String> ncsCodes;       // 필터링할 NCS 직무 코드 목록(CSV로 받아 파싱).

    // 생성자에서 설정값 주입 + RestClient 구성. 모든 외부 의존값은 application 설정에서 받는다(기본값은 dev 편의용).
    public AlioJobPostingFetcher(
            @Value("${publicjob.alio.base-url:https://opendata.alio.go.kr}") String baseUrl,
            @Value("${publicjob.alio.list-path:/new/v1/recruit/list.do}") String listPath,
            @Value("${publicjob.alio.api-key:}") String serviceKey,                         // 기본 빈 문자열 — 미설정 시 isConfigured()=false
            @Value("${publicjob.alio.ncs-codes:R600020}") String ncsCodesCsv) {             // 기본 정보통신(R600020) 직무
        this.baseUrl = baseUrl;
        this.listPath = listPath;
        this.serviceKey = serviceKey == null ? "" : serviceKey.trim();
        // CSV를 trim + 빈값 제거해 코드 리스트로 변환. 비어 있으면 전체 조회(필터 없음)로 동작.
        this.ncsCodes = (ncsCodesCsv == null || ncsCodesCsv.isBlank())
                ? List.of()
                : java.util.Arrays.stream(ncsCodesCsv.split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).toList();

        // 외부 호출이 멈추면 스케줄러 스레드를 점유하므로 연결/읽기 타임아웃을 짧게 건다.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "JobIT/1.0 (+https://job-it.site)")   // 일부 공공 API는 UA 없으면 차단
                .requestFactory(factory)
                .build();
    }

    // 도메인 enum 이름과 일치하는 소스 식별자. (infra가 도메인 enum을 직접 참조하지 않도록 문자열로 노출)
    @Override
    public String source() {
        return "PUBLIC_DATA";
    }

    // API 키가 있어야 호출 가능. 키 미설정 환경에서는 SyncService가 이 어댑터를 건너뛴다.
    @Override
    public boolean isConfigured() {
        return !serviceKey.isBlank();
    }

    // 포트 구현 진입점. NCS 코드별로 나눠 호출한 결과를 합쳐 한 페이지 분량으로 돌려준다.
    @Override
    public List<NormalizedJob> fetchPage(int pageNo, int numOfRows) {
        // NCS 코드 미설정 시 전체. 설정 시 코드별로 호출 후 합치고 externalId 기준 중복 제거.
        if (ncsCodes.isEmpty()) {
            return parsePage(pageNo, numOfRows, null);
        }
        // 여러 코드 결과를 externalId 키로 묶어 중복 제거. 충돌 시 먼저 들어온 것 유지((a,b)->a),
        // LinkedHashMap으로 등장 순서를 보존한다.
        return ncsCodes.stream()
                .flatMap(code -> parsePage(pageNo, numOfRows, code).stream())
                .collect(java.util.stream.Collectors.toMap(
                        NormalizedJob::externalId, j -> j, (a, b) -> a,
                        java.util.LinkedHashMap::new))
                .values()
                .stream()
                .toList();
    }

    // 원문 응답을 받아 JSON 파싱 + 정규화까지 수행. 어떤 단계에서 실패해도 빈 목록으로 폴백(동기화 전체를 중단시키지 않음).
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
                    .filter(j -> j.externalId() != null && !j.externalId().isBlank())   // ID 없는 항목은 업서트 불가하므로 제외
                    .toList();
        } catch (Exception e) {
            log.warn("ALIO 응답 파싱 실패: {}. raw={}", e.getMessage(), snippet(raw));
            return List.of();
        }
    }

    // 요청한 NCS 코드가 응답 항목의 코드 목록에 실제 포함되는지 확인하는 안전망.
    // (요청 파라미터가 무시되고 전체가 오는 경우를 대비해 응답 쪽에서 한 번 더 거른다)
    private boolean matchesNcs(AlioRecruitResponse.Item item, String ncsCode) {
        if (ncsCode == null) return true;   // 필터 미지정이면 모두 통과.
        String lst = item.ncsCdLst();
        return lst != null && lst.contains(ncsCode);
    }

    /** 진단용 — ALIO 응답 원문을 그대로 반환 (NCS 필터 미적용). */
    public String fetchPageRaw(int pageNo, int numOfRows) {
        return fetchPageRaw(pageNo, numOfRows, null);
    }

    // 실제 HTTP POST 호출. form-urlencoded 본문으로 파라미터를 보내고 응답 원문(String)을 반환. 실패 시 null.
    public String fetchPageRaw(int pageNo, int numOfRows, String ncsCode) {
        if (!isConfigured()) {
            log.warn("publicjob.alio.api-key 미설정. ALIO 호출 skip.");
            return null;
        }

        // LinkedHashMap으로 파라미터 순서를 보존(serviceKey -> pageNo -> ... ). 디버깅·재현성 목적.
        java.util.LinkedHashMap<String, String> params = new java.util.LinkedHashMap<>();
        params.put("serviceKey", serviceKey);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("numOfRows", String.valueOf(numOfRows));
        if (ncsCode != null && !ncsCode.isBlank()) {
            params.put("ncsCode", ncsCode);
        }
        String formBody = formEncode(params);

        // serviceKey는 로그에 절대 노출하지 않는다(key=*** 로 마스킹).
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

    // ALIO 항목 1건을 도메인 친화 NormalizedJob으로 변환. 소스 고유 스키마를 공통 모델로 흡수하는 핵심 매핑.
    private NormalizedJob normalize(AlioRecruitResponse.Item item) {
        String externalId = item.recrutPblntSn() == null ? null : String.valueOf(item.recrutPblntSn());
        String location = firstNonBlank(item.workRgnNmLst(), item.workRgnLst());   // 지역명 우선, 없으면 코드.
        String careerLevel = item.recrutSeNm();
        String employmentType = decodeHireType(item.hireTypeLst());                // 코드를 한글 라벨로.

        // salaryRange/postedAt은 ALIO 응답에 마땅한 필드가 없어 null로 둔다.
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

    // 고용형태 코드를 한글 라벨로 변환. 매핑에 없는 코드는 원본 코드를 그대로 노출(데이터 유실 방지).
    private String decodeHireType(String code) {
        if (code == null || code.isBlank()) return null;
        return HIRE_TYPE_NAMES.getOrDefault(code, code);
    }

    // 파라미터 맵을 application/x-www-form-urlencoded 문자열로 직렬화. 키·값 모두 URL 인코딩.
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

    // 두 값 중 비어있지 않은 첫 값을 고른다(지역명 -> 지역코드 폴백 등에 사용).
    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    // null을 빈 문자열로 정규화(제목·기관명 등 비어도 되는 필드의 NPE 방지).
    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    // 로그용 — 응답 원문이 길 때 앞 500자만 잘라 남긴다(로그 폭주 방지).
    private String snippet(String s) {
        if (s == null) return "";
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }

    // yyyyMMdd 문자열을 LocalDateTime(해당일 00:00)으로 변환. 숫자 외 문자는 제거하고, 파싱 실패 시 null.
    private LocalDateTime parseYmd(String ymd) {
        if (ymd == null || ymd.isBlank()) return null;
        try {
            return LocalDate.parse(ymd.replaceAll("[^0-9]", ""), YMD).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }
}