import { useCallback, useEffect, useRef, useState } from "react";

/**
 * 화상 면접용 카메라 self-view 훅.
 * getUserMedia로 비디오 스트림만 받고(audio:false — 음성 인식이 마이크를 따로 잡으므로 충돌 방지),
 * 콜백 ref(setVideoEl)로 video 엘리먼트가 마운트될 때마다 현재 스트림을 자동 연결한다.
 * (환경 체크 화면 → 면접 화면처럼 video 노드가 바뀌어도 끊기지 않게)
 */
export function useCamera() {
  const streamRef = useRef(null);
  const videoElRef = useRef(null);
  const [error, setError] = useState("");

  const setVideoEl = useCallback((node) => {
    videoElRef.current = node;
    if (node && streamRef.current) node.srcObject = streamRef.current;
  }, []);

  const start = async () => {
    setError("");
    if (streamRef.current) {
      if (videoElRef.current) videoElRef.current.srcObject = streamRef.current;
      return;
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
      streamRef.current = stream;
      if (videoElRef.current) videoElRef.current.srcObject = stream;
    } catch {
      setError("카메라를 사용할 수 없어요. 브라우저 권한을 확인해주세요.");
    }
  };

  const stop = () => {
    streamRef.current?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;
    if (videoElRef.current) videoElRef.current.srcObject = null;
  };

  useEffect(() => () => stop(), []);

  return { setVideoEl, error, start, stop };
}
