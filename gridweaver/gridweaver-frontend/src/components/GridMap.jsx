import React, { useEffect, useRef } from 'react';
import { MapContainer, TileLayer, CircleMarker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet.heat';

const STATE_COLOR = {
  NORMAL: '#2ecc71',
  CHARGING: '#f5a623',
  DISCHARGING: '#4aa8ff',
  FAULT: '#ff5c5c'
};

/** Renders (and live-updates) a leaflet.heat layer from the current nodes. */
function HeatLayer({ nodes, active }) {
  const map = useMap();
  const layerRef = useRef(null);

  useEffect(() => {
    if (!active) {
      if (layerRef.current) {
        map.removeLayer(layerRef.current);
        layerRef.current = null;
      }
      return;
    }
    const points = nodes.map((n) => [n.latitude, n.longitude, Math.min(1, n.powerMw / 1200)]);
    if (layerRef.current) {
      layerRef.current.setLatLngs(points);
    } else {
      layerRef.current = L.heatLayer(points, { radius: 22, blur: 18, maxZoom: 12 }).addTo(map);
    }
    return () => {
      if (layerRef.current) {
        map.removeLayer(layerRef.current);
        layerRef.current = null;
      }
    };
  }, [nodes, active, map]);

  return null;
}

export default function GridMap({ nodes, mode, onModeChange }) {
  const center = nodes.length
    ? [nodes[0].latitude, nodes[0].longitude]
    : [28.6139, 77.209];

  return (
    <div className="gw-map-wrapper">
      <div className="gw-map-toolbar">
        <button
          className={`gw-map-btn ${mode === 'heatmap' ? 'active' : ''}`}
          onClick={() => onModeChange('heatmap')}
        >
          🔥 Heatmap
        </button>
        <button
          className={`gw-map-btn ${mode === 'grid' ? 'active' : ''}`}
          onClick={() => onModeChange('grid')}
        >
          ▦ Grid
        </button>
        <button className="gw-map-btn" onClick={() => onModeChange('grid')}>
          ⌂ Reset
        </button>
      </div>

      <MapContainer center={center} zoom={11} className="gw-leaflet-container" preferCanvas>
        <TileLayer
          attribution='&copy; OpenStreetMap contributors, &copy; CARTO'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />

        {mode === 'grid' &&
          nodes.map((n) => (
            <CircleMarker
              key={n.nodeId}
              center={[n.latitude, n.longitude]}
              radius={Math.max(4, Math.min(14, n.powerMw / 120))}
              pathOptions={{
                color: STATE_COLOR[n.state] ?? '#2ecc71',
                fillColor: STATE_COLOR[n.state] ?? '#2ecc71',
                fillOpacity: 0.75,
                weight: 1
              }}
            >
              <Popup>
                <strong>{n.nodeId}</strong>
                <br />
                State: {n.state}
                <br />
                Power: {n.powerMw.toFixed(1)} MW
                <br />
                Load: {n.loadPercent.toFixed(0)}%
                <br />
                Battery: {n.batteryPercent.toFixed(0)}%
              </Popup>
            </CircleMarker>
          ))}

        <HeatLayer nodes={nodes} active={mode === 'heatmap'} />
      </MapContainer>

      <div className="gw-map-legend">
        <span><i className="dot" style={{ background: STATE_COLOR.NORMAL }} /> Normal</span>
        <span><i className="dot" style={{ background: STATE_COLOR.CHARGING }} /> Charging</span>
        <span><i className="dot" style={{ background: STATE_COLOR.DISCHARGING }} /> Discharging</span>
        <span><i className="dot" style={{ background: STATE_COLOR.FAULT }} /> Fault</span>
      </div>
    </div>
  );
}
