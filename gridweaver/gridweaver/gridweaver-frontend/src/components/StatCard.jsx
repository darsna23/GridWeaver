import React from 'react';

export default function StatCard({ icon, label, value, footer, footerTone = 'neutral' }) {
  return (
    <div className="gw-stat-card">
      <div className="gw-stat-icon">{icon}</div>
      <div className="gw-stat-label">{label}</div>
      <div className="gw-stat-value">{value}</div>
      <span className={`gw-stat-footer tone-${footerTone}`}>{footer}</span>
    </div>
  );
}
