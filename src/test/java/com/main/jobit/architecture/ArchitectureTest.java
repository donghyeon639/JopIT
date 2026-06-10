package com.main.jobit.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 - 모듈 경계를 컴파일/테스트 시점에 강제하는 ArchUnit 룰.

 - 패키지 구조:
 -   com.main.jobit.domain  — 비즈니스 도메인
 -   com.main.jobit.ai      — AI 어댑터 + 포트 (bedrock/feedback/port)
 -   com.main.jobit.infra   — 외부 시스템 어댑터 (현재 비어 있음)
 - com.main.jobit.global  — 횡단 관심사 (config/exception/security)
 -
 - 단계 3 시점의 기본 룰:
 -   1) domain은 ai.bedrock(LLM 구체 어댑터)를 직접 의존하지 않는다. ai.port 인터페이스만 통한다.
 -   2) ai.bedrock 어댑터는 도메인을 모른다.
 -   3) infra 어댑터는 도메인을 모른다.

 -  다음 작업(이벤트 기반 분리)에서 추가할 룰:
 -   - ai.feedback 전체가 domain을 직접 의존하지 않는다 (현재 AiFeedbackService -> Answer 잔여 의존).
 -   - 패키지 단위 순환 의존성 금지(slices().beFreeOfCycles()).
 */
@AnalyzeClasses(
        packages = "com.main.jobit",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule 도메인은_LLM_구체_어댑터를_직접_의존하지_않는다 =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..ai.bedrock..")
                    .because("도메인은 ai.port의 LlmPort 인터페이스만 의존해야 한다. 어댑터 교체 비용을 0으로 유지.");

    @ArchTest
    static final ArchRule LLM_어댑터는_도메인을_직접_의존하지_않는다 =
            noClasses()
                    .that().resideInAPackage("..ai.bedrock..")
                    .should().dependOnClassesThat().resideInAPackage("..domain..")
                    .because("AI 어댑터는 도메인을 모른다. 도메인이 들어오는 순간 모듈 분리가 불가능해진다.");

    @ArchTest
    static final ArchRule infra는_도메인을_직접_의존하지_않는다 =
            noClasses()
                    .that().resideInAPackage("..infra..")
                    .should().dependOnClassesThat().resideInAPackage("..domain..")
                    .because("infra 어댑터는 도메인을 모른다.");
}