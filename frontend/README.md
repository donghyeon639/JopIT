# DevPrep — React App

Vite + React 18로 변환된 DevPrep 프로토타입.

## 실행

```bash
npm install
npm run dev
```

브라우저에서 `http://localhost:5173` 자동으로 열립니다.

## 빌드

```bash
npm run build
npm run preview
```

## 폴더 구조

```
src/
  main.jsx              # 진입점
  App.jsx               # 라우팅 (state 기반)
  components/           # 공통 컴포넌트 (PrepBot, TopNav, Footer, Icons, Badges)
  screens/              # 화면 단위 컴포넌트
    Landing.jsx
    LevelCheck.jsx
    Auth.jsx
    Dashboard.jsx
    QuestionList.jsx
    Solve.jsx
    AIFeedback.jsx
    Community.jsx
  styles/
    global.css
    design-system.css   # 토큰 + 유틸리티 클래스
```

## 다음 단계

- React Router 도입 (현재는 state switch 기반)
- TypeScript 마이그레이션
- API 연동 (현재는 모두 더미 데이터)
- 디자인 토큰을 CSS-in-JS 또는 Tailwind로 통합 고려
