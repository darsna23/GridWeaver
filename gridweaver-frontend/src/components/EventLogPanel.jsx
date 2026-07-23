import React from 'react';

function formatTime(ts) {
  if (!ts) return '';
  return new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

export default function EventLogPanel({ events }) {
  const list = events ?? [];

  return (
    <div className="gw-panel gw-event-panel">
      <div className="gw-panel-title">
        📋 EVENT LOG <span className="gw-panel-subtitle">LIVE AUDIT</span>
      </div>
      <div className="gw-event-list">
        {list.length === 0 && <div className="gw-event-empty">No transitions yet…</div>}
        {list.map((e, idx) => (
          <div className="gw-event-row" key={`${e.nodeId}-${e.timestamp}-${idx}`}>
            <span className="gw-event-time">{formatTime(e.timestamp)}</span>
            <span className="gw-event-node">{e.nodeId}</span>
            <span className={`gw-event-badge sev-${e.severity}`}>{e.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
