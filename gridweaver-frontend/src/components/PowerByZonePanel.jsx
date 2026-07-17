import React from 'react';

const ZONE_LABELS = {
  ZONE_A: 'Zone A',
  ZONE_B: 'Zone B',
  ZONE_C: 'Zone C',
  ZONE_D: 'Zone D',
  ZONE_E: 'Zone E'
};

export default function PowerByZonePanel({ zonePower }) {
  const entries = Object.entries(zonePower ?? {});
  const max = Math.max(1, ...entries.map(([, v]) => v));

  return (
    <div className="gw-panel">
      <div className="gw-panel-title">📊 POWER BY ZONE</div>
      <div className="gw-zone-list">
        {entries.map(([key, value]) => (
          <div className="gw-zone-row" key={key}>
            <span className="gw-zone-label">{ZONE_LABELS[key] ?? key}</span>
            <div className="gw-zone-bar-track">
              <div
                className="gw-zone-bar-fill"
                style={{ width: `${Math.max(4, (value / max) * 100)}%` }}
              />
            </div>
            <span className="gw-zone-value">{Math.round(value)} MW</span>
          </div>
        ))}
      </div>
    </div>
  );
}
