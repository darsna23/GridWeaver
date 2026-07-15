import React, { useState } from 'react';
import { useGridSocket } from '../hooks/useGridSocket.js';
import GridMap from './GridMap.jsx';
import StatCard from './StatCard.jsx';
import NodeStatesPanel from './NodeStatesPanel.jsx';
import PowerByZonePanel from './PowerByZonePanel.jsx';
import EventLogPanel from './EventLogPanel.jsx';
import {
  ServerCog, Wifi, Zap, ShieldCheck
} from 'lucide-react';

export default function Dashboard() {
  const { overview, connected } = useGridSocket();// function
  
  const [mapMode, setMapMode] = useState('grid'); // 'grid' | 'heatmap'

  const now = new Date();
  const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });

  const totalNodes = overview?.totalNodes ?? 0;
  const onlineNodes = overview?.onlineNodes ?? 0;
  const onlinePct = totalNodes ? ((onlineNodes / totalNodes) * 100).toFixed(1) : '0.0';
  const activePower = overview?.activePowerMw ?? 0;
  const stability = overview?.gridStabilityPercent ?? 0;
  const status = overview?.systemStatus ?? 'Connecting';

  return (
    <div className="gw-app">
      <header className="gw-header">
        <div className="gw-brand">
          <span className="gw-logo">⌁</span>
          <h1>GRIDWEAVER <span className="gw-brand-sub">DASHBOARD</span></h1>
        </div>
        <div className="gw-header-right">
          <span className={`gw-live-dot ${connected ? 'live' : 'offline'}`} />
          <span className="gw-live-label">{connected ? 'Live' : 'Reconnecting…'}</span>
          <span className={`gw-status-pill ${status.toLowerCase()}`}>
            <span className="dot" /> System {status}
          </span>
          <span className="gw-clock">🕐 {timeStr}</span>
        </div>
      </header>

      <section className="gw-kpi-row">
        <StatCard
          icon={<ServerCog size={22} />}
          label="TOTAL NODES"
          value={totalNodes.toLocaleString()}
          footer="Grid Overview"
          footerTone="neutral"
        />
        <StatCard
          icon={<Wifi size={22} />}
          label="ONLINE NODES"
          value={onlineNodes.toLocaleString()}
          footer={`${onlinePct}% Online`}
          footerTone="good"
        />
        <StatCard
          icon={<Zap size={22} />}
          label="ACTIVE POWER"
          value={`${activePower.toFixed(1)} MW`}
          footer="Grid Load"
          footerTone="warn"
        />
        <StatCard
          icon={<ShieldCheck size={22} />}
          label="GRID STABILITY"
          value={`${stability.toFixed(1)}%`}
          footer={`Status: ${status}`}
          footerTone="good"
        />
      </section>

      <section className="gw-main-grid">
        <div className="gw-map-panel">
          <GridMap nodes={overview?.nodes ?? []} mode={mapMode} onModeChange={setMapMode} />
        </div>

        <div className="gw-side-panels">
          <NodeStatesPanel counts={overview?.nodeStateCounts} />
          <PowerByZonePanel zonePower={overview?.powerByZone} />
          <EventLogPanel events={overview?.recentEvents} />
        </div>
      </section>
    </div>
  );
}
