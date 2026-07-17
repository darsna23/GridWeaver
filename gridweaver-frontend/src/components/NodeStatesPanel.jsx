import React from 'react';

const STATE_META = [
  { key: 'NORMAL', label: 'Normal', color: '#2ecc71' },
  { key: 'CHARGING', label: 'Charging', color: '#f5a623' },
  { key: 'DISCHARGING', label: 'Discharging', color: '#4aa8ff' },
  { key: 'FAULT', label: 'Fault', color: '#ff5c5c' }
];

export default function NodeStatesPanel({ counts }) {
  return (
    <div className="gw-panel">
      <div className="gw-panel-title">📶 NODE STATES</div>
      <div className="gw-state-grid">
        {STATE_META.map((s) => (
          <div className="gw-state-item" key={s.key}>
            <span className="gw-state-dot" style={{ background: s.color }} />
            <span className="gw-state-name">{s.label}</span>
            <span className="gw-state-count">{(counts?.[s.key] ?? 0).toLocaleString()}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
