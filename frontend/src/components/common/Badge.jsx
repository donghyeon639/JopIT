import React from "react";

const DifficultyBadge = ({ level }) => {
  const map = {
    low:  { cls: "badge-low",  label: "쉬움" },
    mid:  { cls: "badge-mid",  label: "보통" },
    high: { cls: "badge-high", label: "어려움" },
  };
  const v = map[level] || map.mid;
  return <span className={"badge " + v.cls}>{v.label}</span>;
};

const CategoryBadge = ({ name, color = "blue" }) => {
  const cls =
    color === "blue"   ? "badge-blue"   :
    color === "purple" ? "badge-purple" :
    "badge-gray";
  return <span className={"badge " + cls}>{name}</span>;
};

export { DifficultyBadge, CategoryBadge };