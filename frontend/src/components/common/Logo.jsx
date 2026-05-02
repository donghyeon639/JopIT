import React from "react";

const Logo = ({ size = 28 }) => (
  <div className="dp-logo">
    <div className="dp-logo-mark" style={{ width: size, height: size, fontSize: size * 0.5 }}>D</div>
    <span>DevPrep</span>
  </div>
);

export default Logo;