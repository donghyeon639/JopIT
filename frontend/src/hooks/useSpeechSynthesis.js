import { useCallback, useEffect, useRef, useState } from "react";

/**
 * 브라우저 SpeechSynthesis(TTS) 래퍼. AI 면접관이 인사말·질문을 음성으로 읽어주도록 한다.
 * 한국어 음성이 있으면 우선 사용하고, 없으면 같은 언어 계열 → 기본 음성으로 폴백한다.
 * 미지원 브라우저에서는 supported=false로 떨어져 호출 측에서 텍스트만 보여주면 된다.
 */
export function useSpeechSynthesis({ lang = "ko-KR" } = {}) {
  const synth = typeof window !== "undefined" ? window.speechSynthesis : null;
  const supported = !!synth;

  const voiceRef = useRef(null);
  const [speaking, setSpeaking] = useState(false);

  // 사용 가능한 음성 목록은 비동기로 로드되므로 voiceschanged 이벤트로도 갱신한다.
  useEffect(() => {
    if (!supported) return;
    const pickVoice = () => {
      const voices = synth.getVoices();
      const base = lang.split("-")[0];
      voiceRef.current =
        voices.find((v) => v.lang === lang) ||
        voices.find((v) => v.lang?.startsWith(base)) ||
        null;
    };
    pickVoice();
    synth.addEventListener?.("voiceschanged", pickVoice);
    return () => synth.removeEventListener?.("voiceschanged", pickVoice);
  }, [supported, lang]);

  const cancel = useCallback(() => {
    if (!supported) return;
    try { synth.cancel(); } catch { /* noop */ }
    setSpeaking(false);
  }, [supported]);

  const speak = useCallback((text) => {
    if (!supported || !text) return;
    try { synth.cancel(); } catch { /* noop */ } // 이전 발화 중단 후 새로 말한다
    const u = new SpeechSynthesisUtterance(text);
    u.lang = lang;
    if (voiceRef.current) u.voice = voiceRef.current;
    u.rate = 1.0;
    u.pitch = 1.0;
    u.onstart = () => setSpeaking(true);
    u.onend = () => setSpeaking(false);
    u.onerror = () => setSpeaking(false);
    synth.speak(u);
  }, [supported, lang]);

  // 언마운트 시 남은 발화 정리.
  useEffect(() => () => { try { synth?.cancel(); } catch { /* noop */ } }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return { supported, speaking, speak, cancel };
}