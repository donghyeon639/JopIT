import { useEffect, useRef, useState } from "react";

/**
 * 브라우저 Web Speech API(webkitSpeechRecognition) 래퍼.
 * 음성을 텍스트로 변환(STT)한다. Chrome/Edge에서 동작하며, 미지원 브라우저에서는
 * supported=false로 떨어져 호출 측에서 텍스트 직접 입력으로 폴백할 수 있다.
 */
export function useSpeechRecognition({ lang = "ko-KR" } = {}) {
  const SpeechRecognition =
    typeof window !== "undefined"
      ? window.SpeechRecognition || window.webkitSpeechRecognition
      : null;
  const supported = !!SpeechRecognition;

  const recognitionRef = useRef(null);
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
      let finalText = "";
      for (let i = e.resultIndex; i < e.results.length; i++) {
        if (e.results[i].isFinal) finalText += e.results[i][0].transcript;
      }
      if (finalText) {
        setTranscript((prev) => (prev ? prev.trimEnd() + " " : "") + finalText.trim());
      }
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

  const reset = () => setTranscript("");

  return { supported, listening, transcript, error, start, stop, reset, setTranscript };
}
