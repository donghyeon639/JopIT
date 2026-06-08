import { useEffect, useRef, useState } from "react";

/**
 * 브라우저 Web Speech API(webkitSpeechRecognition) 래퍼.
 * 음성을 텍스트로 변환(STT)한다. Chrome/Edge에서 동작하며, 미지원 브라우저에서는
 * supported=false로 떨어져 호출 측에서 폴백할 수 있다.
 *
 * 확정(final) 결과뿐 아니라 잠정(interim) 결과도 즉시 화면에 반영해 실시간 자막처럼 보여준다.
 * → 확정 누적은 finalRef에 모으고, transcript = 확정본 + 현재 잠정본 으로 매 이벤트마다 갱신.
 */
export function useSpeechRecognition({ lang = "ko-KR" } = {}) {
  const SpeechRecognition =
    typeof window !== "undefined"
      ? window.SpeechRecognition || window.webkitSpeechRecognition
      : null;
  const supported = !!SpeechRecognition;

  const recognitionRef = useRef(null);
  const finalRef = useRef(""); // 확정된 텍스트 누적
  const [listening, setListening] = useState(false);
  const [transcript, setTranscript] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (!supported) return;
    const rec = new SpeechRecognition();
    rec.lang = lang;
    rec.continuous = true;
    rec.interimResults = true;

    rec.onresult = (e) => {
      let interim = "";
      let finalChunk = "";
      for (let i = e.resultIndex; i < e.results.length; i++) {
        const text = e.results[i][0].transcript;
        if (e.results[i].isFinal) finalChunk += text;
        else interim += text;
      }
      if (finalChunk) {
        finalRef.current = (finalRef.current ? finalRef.current.trimEnd() + " " : "") + finalChunk.trim();
      }
      // 확정본 + 실시간 잠정본을 합쳐 매 이벤트마다 갱신 → 말하는 즉시 글자가 따라온다.
      const live = (finalRef.current + " " + interim).trim();
      setTranscript(live);
    };

    rec.onerror = (e) => {
      if (e.error !== "no-speech" && e.error !== "aborted") {
        setError("음성 인식 오류: " + (e.error || "알 수 없음"));
      }
      setListening(false);
    };
    rec.onend = () => setListening(false);

    recognitionRef.current = rec;
    return () => {
      try { rec.stop(); } catch { /* noop */ }
    };
  }, [supported, lang]);

  const start = () => {
    if (!supported) return;
    setError("");
    try {
      recognitionRef.current.start();
      setListening(true);
    } catch { /* 이미 시작된 경우 무시 */ }
  };

  const stop = () => {
    try { recognitionRef.current?.stop(); } catch { /* noop */ }
    setListening(false);
  };

  const reset = () => {
    finalRef.current = "";
    setTranscript("");
  };

  return { supported, listening, transcript, error, start, stop, reset, setTranscript };
}
