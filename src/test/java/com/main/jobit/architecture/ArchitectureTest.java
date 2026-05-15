package com.main.jobit.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 모듈 경계를 컴파일/테스트 시점에 강제하는 ArchUnit 룰.
 *
 * 단계 2 시점에는 폴더 재배치 전이라 도메인이 평탄하게 흩어져 있다.
 * 따라서 지금은 가장 핵심 룰 두 가지만 활성화한다:
 *   1) 도메인 코드가 LLM 구체 어댑터(ClaudeCliService)를 직접 import 금지 — LlmPort만 허용.
 *   2) AI 어댑터(Claude*)는 다른 어떤 도메인 엔티티/서비스도 직접 의존 금지.
 *      (단, AiFeedbackService -> Answer 의존은 아직 끊기지 않았으므로 룰 2는 어댑터 한정)
 *
 * 단계 3(폴더 재배치) 후에는 패키지 표현을 com.main.jobit.domain.. / ai.. / infra.. / global..
 * 로 갈아끼우고, 순환 의존성 금지(slices().beFreeOfCycles()) 같은 강한 룰을 추가한다.
 */
@AnalyzeClasses(
        packages = "com.main.jobit",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class ArchitectureTest {

    private static final String[] DOMAIN_PACKAGES = {
            "com.main.jobit.answer..",
            "com.main.jobit.question..",
            "com.main.jobit.comment..",
            "com.main.jobit.user..",
            "com.main.jobit.resume..",
            "com.main.jobit.mypage..",
            "com.main.jobit.job..",
            "com.main.jobit.techtrend..",
            "com.main.jobit.category..",
            "com.main.jobit.auth..",
            "com.main.jobit.admin.."
    };

    @ArchTest
    static final ArchRule 도메인은_LLM_구체_어댑터를_직접_참조하지_않는다 =
            noClasses()
                    .that()
                    .resideInAnyPackage(DOMAIN_PACKAGES)
                    .should()
                    .dependOnClassesThat()
                    .haveFullyQualifiedName("com.main.jobit.aifeedback.ClaudeCliService")
                    .because("도메인은 LlmPort 인터페이스만 의존해야 한다. 어댑터 교체 비용을 0으로 유지.");

    @ArchTest
    static final ArchRule LLM_어댑터는_도메인_컨트롤러나_레포지토리를_의존하지_않는다 =
            noClasses()
                    .that()
                    .haveSimpleName("ClaudeCliService")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(DOMAIN_PACKAGES)
                    .because("AI 어댑터는 도메인을 모른다. 도메인 코드가 들어오는 순간 모듈 분리가 불가능해진다.");
}