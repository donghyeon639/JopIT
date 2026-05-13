import React from "react";

const Icon = ({ d, size = 20, fill = "none", stroke = "currentColor", sw = 1.75, children }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill={fill} stroke={stroke}
       strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round">
    {d ? <path d={d} /> : children}
  </svg>
);

const IconHome         = (p) => <Icon {...p} d="M3 12l9-9 9 9M5 10v10h14V10" />;
const IconList         = (p) => <Icon {...p} d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01" />;
const IconSpark        = (p) => <Icon {...p}><path d="M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M5.6 18.4l2.1-2.1M16.3 7.7l2.1-2.1"/></Icon>;
const IconBookmark     = (p) => <Icon {...p} d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />;
const IconBuilding     = (p) => <Icon {...p} d="M3 21h18M5 21V7l7-4 7 4v14M9 9h.01M9 13h.01M9 17h.01M15 9h.01M15 13h.01M15 17h.01" />;
const IconHeart        = (p) => <Icon {...p} d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />;
const IconHeartFill    = (p) => <Icon {...p} fill="#EF4444" stroke="#EF4444" d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />;
const IconChevronRight = (p) => <Icon {...p} d="M9 18l6-6-6-6" />;
const IconChevronDown  = (p) => <Icon {...p} d="M6 9l6 6 6-6" />;
const IconCheck        = (p) => <Icon {...p} d="M20 6L9 17l-5-5" />;
const IconSearch       = (p) => <Icon {...p}><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></Icon>;
const IconBell         = (p) => <Icon {...p} d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 0 1-3.46 0" />;
const IconFilter       = (p) => <Icon {...p} d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" />;
const IconClock        = (p) => <Icon {...p}><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></Icon>;
const IconArrowRight   = (p) => <Icon {...p} d="M5 12h14M13 5l7 7-7 7" />;
const IconArrowLeft    = (p) => <Icon {...p} d="M19 12H5M12 19l-7-7 7-7" />;
const IconPlay         = (p) => <Icon {...p} fill="currentColor" d="M5 3l14 9-14 9V3z" />;
const IconCode         = (p) => <Icon {...p} d="M16 18l6-6-6-6M8 6l-6 6 6 6" />;
const IconUser         = (p) => <Icon {...p}><circle cx="12" cy="8" r="4"/><path d="M4 21v-1a8 8 0 0 1 16 0v1"/></Icon>;
const IconLock         = (p) => <Icon {...p}><rect x="4" y="11" width="16" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/></Icon>;
const IconMail         = (p) => <Icon {...p}><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 7l9 7 9-7"/></Icon>;

const IconGoogle = (p) => (
  <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24">
    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84A10.99 10.99 0 0 0 12 23z"/>
    <path fill="#FBBC05" d="M5.84 14.09A6.6 6.6 0 0 1 5.48 12c0-.73.13-1.43.36-2.09V7.07H2.18A10.99 10.99 0 0 0 1 12c0 1.77.42 3.45 1.18 4.93l3.66-2.84z"/>
    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84C6.71 7.31 9.14 5.38 12 5.38z"/>
  </svg>
);

const IconGithub = (p) => (
  <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24" fill="#181717">
    <path d="M12 .5C5.65.5.5 5.65.5 12c0 5.08 3.29 9.39 7.86 10.92.58.11.79-.25.79-.55 0-.27-.01-1.17-.02-2.12-3.2.7-3.87-1.36-3.87-1.36-.52-1.32-1.27-1.67-1.27-1.67-1.04-.71.08-.7.08-.7 1.15.08 1.76 1.18 1.76 1.18 1.02 1.74 2.69 1.24 3.34.95.1-.74.4-1.24.72-1.53-2.55-.29-5.24-1.28-5.24-5.69 0-1.26.45-2.28 1.18-3.09-.12-.29-.51-1.46.11-3.04 0 0 .97-.31 3.18 1.18a11 11 0 0 1 5.79 0c2.21-1.49 3.18-1.18 3.18-1.18.62 1.58.23 2.75.11 3.04.74.81 1.18 1.83 1.18 3.09 0 4.42-2.69 5.39-5.25 5.68.41.35.78 1.05.78 2.12 0 1.53-.01 2.77-.01 3.14 0 .3.21.66.79.55C20.21 21.39 23.5 17.08 23.5 12 23.5 5.65 18.35.5 12 .5z"/>
  </svg>
);

const IconKakao = (p) => (
  <svg width={p.size || 18} height={p.size || 18} viewBox="0 0 24 24">
    <circle cx="12" cy="12" r="11" fill="#FEE500" />
    <path fill="#191919" d="M7.7 8.5h2.1v2.8l2.3-2.8h2.6L12 11.6l2.9 3.9h-2.6L10 12.4v3.1H7.7z" />
  </svg>
);

export {
  Icon,
  IconHome, IconList, IconSpark, IconBookmark, IconBuilding,
  IconHeart, IconHeartFill, IconChevronRight, IconChevronDown, IconCheck,
  IconSearch, IconBell, IconFilter, IconClock,
  IconArrowRight, IconArrowLeft, IconPlay, IconCode,
  IconUser, IconLock, IconMail, IconGoogle, IconGithub, IconKakao,
};