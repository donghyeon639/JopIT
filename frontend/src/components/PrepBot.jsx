import React from "react";

// DevPrep AI 캐릭터 — "프렙(Prep)" 선생님 로봇
// 2등신 비율 (머리:몸 = 1:1), 귀엽고 친근한 톤
// expression: happy | thinking | celebrate | wave | teach | sleep
// size: 픽셀 크기
const PrepBot = ({ expression = "happy", size = 120, accent = "#2563EB" }) => {
  const w = size;
  const h = size * 1.35;

  // 표정별 눈/입
  const faces = {
    happy: { eye: "dot", mouth: "smile", brow: false },
    thinking: { eye: "dot", mouth: "neutral", brow: true },
    celebrate: { eye: "happy", mouth: "open", brow: false },
    wave: { eye: "wink", mouth: "smile", brow: false },
    teach: { eye: "dot", mouth: "smile", brow: false, accessory: "pointer" },
    sleep: { eye: "closed", mouth: "neutral", brow: false },
  };
  const f = faces[expression] || faces.happy;

  return (
    <svg viewBox="0 0 120 162" width={w} height={h} style={{ display: "block" }}>
      <defs>
        <linearGradient id={`pb-body-${expression}`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#FFFFFF" />
          <stop offset="1" stopColor="#E8EEF7" />
        </linearGradient>
        <linearGradient id={`pb-head-${expression}`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#FFFFFF" />
          <stop offset="1" stopColor="#DCE5F2" />
        </linearGradient>
        <radialGradient id={`pb-screen-${expression}`} cx="0.5" cy="0.4" r="0.7">
          <stop offset="0" stopColor="#1E40AF" />
          <stop offset="1" stopColor="#0B1A3A" />
        </radialGradient>
        <filter id={`pb-shadow-${expression}`} x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="2" stdDeviation="2" floodColor="#1D2939" floodOpacity="0.12"/>
        </filter>
      </defs>

      {/* 그림자 */}
      <ellipse cx="60" cy="156" rx="32" ry="4" fill="#1D2939" opacity="0.10"/>

      {/* === 몸통 (하반신, 작게) === */}
      {/* 다리 */}
      <rect x="44" y="128" width="10" height="18" rx="4" fill="#94A3B8"/>
      <rect x="66" y="128" width="10" height="18" rx="4" fill="#94A3B8"/>
      {/* 발 */}
      <ellipse cx="49" cy="150" rx="9" ry="4" fill="#475569"/>
      <ellipse cx="71" cy="150" rx="9" ry="4" fill="#475569"/>

      {/* 몸통 */}
      <g filter={`url(#pb-shadow-${expression})`}>
        <rect x="34" y="84" width="52" height="50" rx="14"
              fill={`url(#pb-body-${expression})`}
              stroke="#CBD5E1" strokeWidth="1.2"/>
      </g>
      {/* 가슴 LED 패널 */}
      <rect x="46" y="98" width="28" height="14" rx="4" fill="#0F172A"/>
      <circle cx="52" cy="105" r="1.8" fill="#10B981"/>
      <circle cx="58" cy="105" r="1.8" fill={accent}/>
      <circle cx="64" cy="105" r="1.8" fill="#F59E0B"/>
      <circle cx="70" cy="105" r="1.8" fill="#EC4899"/>
      {/* 넥타이 (선생님 컨셉) */}
      <path d="M60 84 L56 90 L60 96 L64 90 Z" fill={accent}/>
      <rect x="58" y="96" width="4" height="8" fill={accent} rx="1"/>

      {/* 팔 */}
      {expression === "wave" ? (
        <g>
          {/* 왼팔 (자연스럽게) */}
          <rect x="26" y="92" width="10" height="22" rx="5" fill="#E2E8F0" stroke="#CBD5E1" strokeWidth="1"/>
          <circle cx="31" cy="116" r="6" fill="#FFFFFF" stroke="#CBD5E1" strokeWidth="1"/>
          {/* 오른팔 (들어서 인사) */}
          <g transform="rotate(-30 89 92)">
            <rect x="84" y="68" width="10" height="26" rx="5" fill="#E2E8F0" stroke="#CBD5E1" strokeWidth="1"/>
            <circle cx="89" cy="68" r="7" fill="#FFFFFF" stroke="#CBD5E1" strokeWidth="1"/>
          </g>
        </g>
      ) : expression === "celebrate" ? (
        <g>
          {/* 양팔 위로 */}
          <g transform="rotate(40 36 92)">
            <rect x="31" y="68" width="10" height="26" rx="5" fill="#E2E8F0" stroke="#CBD5E1" strokeWidth="1"/>
            <circle cx="36" cy="68" r="7" fill="#FFFFFF" stroke="#CBD5E1" strokeWidth="1"/>
          </g>
          <g transform="rotate(-40 84 92)">
            <rect x="79" y="68" width="10" height="26" rx="5" fill="#E2E8F0" stroke="#CBD5E1" strokeWidth="1"/>
            <circle cx="84" cy="68" r="7" fill="#FFFFFF" stroke="#CBD5E1" strokeWidth="1"/>
          </g>
        </g>
      ) : expression === "teach" ? (
        <g>
          {/* 왼팔 */}
          <rect x="26" y="92" width="10" height="22" rx="5" fill="#E2E8F0" stroke="#CBD5E1" strokeWidth="1"/>
          <circle cx="31" cy="116" r="6" fill="#FFFFFF" stroke="#CBD5E1" strokeWidth="1"/>
          {/* 오른팔: 포인터 들고 가리키기 */}
          <g transform="rotate(-50 89 92)">
            <rect x="84" y="74" width="10" height="22" rx="5" fill="#E2E8F0" stroke="#CBD5E1" strokeWidth="1"/>
            <circle cx="89" cy="74" r="6" fill="#FFFFFF" stroke="#CBD5E1" strokeWidth="1"/>
            {/* 포인터 막대 */}
            <rect x="88" y="50" width="2" height="22" fill="#475569"/>
            <circle cx="89" cy="49" r="3" fill={accent}/>
          </g>
        </g>
      ) : (
        <g>
          {/* 양팔 늘어뜨림 */}
          <rect x="26" y="92" width="10" height="22" rx="5" fill="#E2E8F0" stroke="#CBD5E1" strokeWidth="1"/>
          <circle cx="31" cy="116" r="6" fill="#FFFFFF" stroke="#CBD5E1" strokeWidth="1"/>
          <rect x="84" y="92" width="10" height="22" rx="5" fill="#E2E8F0" stroke="#CBD5E1" strokeWidth="1"/>
          <circle cx="89" cy="116" r="6" fill="#FFFFFF" stroke="#CBD5E1" strokeWidth="1"/>
        </g>
      )}

      {/* === 머리 (크게, 2등신) === */}
      {/* 안테나 */}
      <line x1="60" y1="14" x2="60" y2="6" stroke="#94A3B8" strokeWidth="2" strokeLinecap="round"/>
      <circle cx="60" cy="5" r="3.5" fill={accent}>
        {expression === "thinking" && (
          <animate attributeName="opacity" values="1;0.3;1" dur="1.2s" repeatCount="indefinite"/>
        )}
      </circle>

      {/* 학사모 (선생님 컨셉) */}
      <g>
        <rect x="38" y="20" width="44" height="4" fill="#1E293B"/>
        <polygon points="34,22 86,22 80,16 40,16" fill="#0F172A"/>
        <circle cx="60" cy="19" r="1.5" fill="#F59E0B"/>
        {/* 술 */}
        <line x1="60" y1="19" x2="76" y2="14" stroke="#F59E0B" strokeWidth="1.5"/>
        <circle cx="76" cy="14" r="2" fill="#F59E0B"/>
      </g>

      {/* 머리 본체 */}
      <g filter={`url(#pb-shadow-${expression})`}>
        <rect x="22" y="24" width="76" height="62" rx="20"
              fill={`url(#pb-head-${expression})`}
              stroke="#CBD5E1" strokeWidth="1.2"/>
      </g>

      {/* 귀(스피커) */}
      <rect x="18" y="46" width="6" height="16" rx="2" fill="#94A3B8"/>
      <rect x="96" y="46" width="6" height="16" rx="2" fill="#94A3B8"/>
      <circle cx="21" cy="50" r="0.8" fill="#475569"/>
      <circle cx="21" cy="54" r="0.8" fill="#475569"/>
      <circle cx="21" cy="58" r="0.8" fill="#475569"/>
      <circle cx="99" cy="50" r="0.8" fill="#475569"/>
      <circle cx="99" cy="54" r="0.8" fill="#475569"/>
      <circle cx="99" cy="58" r="0.8" fill="#475569"/>

      {/* 얼굴 화면 */}
      <rect x="32" y="34" width="56" height="42" rx="12"
            fill={`url(#pb-screen-${expression})`}/>

      {/* 눈썹 (생각 중일 때만) */}
      {f.brow && (
        <g stroke="#FFFFFF" strokeWidth="2" strokeLinecap="round" fill="none">
          <path d="M42 44 L50 42"/>
          <path d="M70 42 L78 44"/>
        </g>
      )}

      {/* 눈 */}
      {f.eye === "dot" && (
        <g fill="#FFFFFF">
          <circle cx="46" cy="52" r="4">
            <animate attributeName="ry" values="4;0.5;4" dur="3s" begin="2s" repeatCount="indefinite"/>
          </circle>
          <circle cx="74" cy="52" r="4">
            <animate attributeName="ry" values="4;0.5;4" dur="3s" begin="2s" repeatCount="indefinite"/>
          </circle>
          {/* 반사광 */}
          <circle cx="47.5" cy="50.5" r="1.2" fill="#BFDBFE"/>
          <circle cx="75.5" cy="50.5" r="1.2" fill="#BFDBFE"/>
        </g>
      )}
      {f.eye === "happy" && (
        <g stroke="#FFFFFF" strokeWidth="2.5" fill="none" strokeLinecap="round">
          <path d="M42 53 Q46 48 50 53"/>
          <path d="M70 53 Q74 48 78 53"/>
        </g>
      )}
      {f.eye === "wink" && (
        <g>
          <circle cx="46" cy="52" r="4" fill="#FFFFFF"/>
          <circle cx="47.5" cy="50.5" r="1.2" fill="#BFDBFE"/>
          <path d="M70 53 Q74 48 78 53" stroke="#FFFFFF" strokeWidth="2.5" fill="none" strokeLinecap="round"/>
        </g>
      )}
      {f.eye === "closed" && (
        <g stroke="#FFFFFF" strokeWidth="2.5" fill="none" strokeLinecap="round">
          <path d="M42 53 Q46 56 50 53"/>
          <path d="M70 53 Q74 56 78 53"/>
        </g>
      )}

      {/* 볼 (홍조) */}
      <ellipse cx="40" cy="62" rx="3" ry="2" fill="#F472B6" opacity="0.55"/>
      <ellipse cx="80" cy="62" rx="3" ry="2" fill="#F472B6" opacity="0.55"/>

      {/* 입 */}
      {f.mouth === "smile" && (
        <path d="M52 64 Q60 70 68 64" stroke="#FFFFFF" strokeWidth="2" fill="none" strokeLinecap="round"/>
      )}
      {f.mouth === "neutral" && (
        <line x1="55" y1="66" x2="65" y2="66" stroke="#FFFFFF" strokeWidth="2" strokeLinecap="round"/>
      )}
      {f.mouth === "open" && (
        <ellipse cx="60" cy="66" rx="4" ry="3" fill="#FFFFFF"/>
      )}

      {/* sleep zzz */}
      {expression === "sleep" && (
        <g fill="#94A3B8" fontFamily="sans-serif" fontWeight="700">
          <text x="92" y="32" fontSize="10">z</text>
          <text x="100" y="24" fontSize="8">z</text>
        </g>
      )}
    </svg>
  );
};

// 말풍선 + 캐릭터 헬퍼 — "프렙쌤이 한마디"
const PrepCoach = ({ expression = "teach", title, message, accent = "#2563EB", size = 84, layout = "row" }) => {
  if (layout === "card") {
    return (
      <div style={{
        background: "linear-gradient(135deg, #EFF6FF 0%, #F5F3FF 100%)",
        border: "1px solid #BFDBFE", borderRadius: 16, padding: 18,
        display: "flex", gap: 14, alignItems: "flex-start"
      }}>
        <div style={{ flexShrink: 0 }}><PrepBot expression={expression} size={size} accent={accent}/></div>
        <div style={{ flex: 1, paddingTop: 4 }}>
          {title && <div style={{ fontSize: 13, fontWeight: 700, color: "#1D4ED8", marginBottom: 4,
                                    display: "flex", alignItems: "center", gap: 6 }}>
            <span>프렙쌤</span>
            <span style={{ width: 4, height: 4, borderRadius: 999, background: "#93C5FD" }}/>
            <span style={{ color: "#475569", fontWeight: 500 }}>{title}</span>
          </div>}
          <div style={{ fontSize: 14, lineHeight: 1.6, color: "#1E293B" }}>{message}</div>
        </div>
      </div>
    );
  }
  // 기본: 가로 배치
  return (
    <div style={{ display: "flex", alignItems: "flex-end", gap: 12 }}>
      <div style={{ flexShrink: 0 }}><PrepBot expression={expression} size={size} accent={accent}/></div>
      <div style={{
        position: "relative", background: "#fff", border: "1px solid #E2E8F0",
        borderRadius: 16, padding: "12px 16px", maxWidth: 340,
        boxShadow: "0 2px 8px -2px rgba(16,24,40,0.08)"
      }}>
        {/* 말풍선 꼬리 */}
        <div style={{ position: "absolute", left: -7, bottom: 14,
                        width: 14, height: 14, background: "#fff",
                        borderLeft: "1px solid #E2E8F0", borderBottom: "1px solid #E2E8F0",
                        transform: "rotate(45deg)" }}/>
        {title && <div style={{ fontSize: 12, fontWeight: 700, color: "#1D4ED8", marginBottom: 2 }}>{title}</div>}
        <div style={{ fontSize: 13, lineHeight: 1.55, color: "#1E293B" }}>{message}</div>
      </div>
    </div>
  );
};

export { PrepBot };
export { PrepCoach };