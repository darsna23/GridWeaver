import { useEffect, useRef, useState } from 'react';

const WS_URL =
  (window.location.protocol === 'https:' ? 'wss://' : 'ws://') +
  window.location.host +
  '/ws/grid';

const API_URL = '/api/grid/overview';


export function useGridSocket() {
  const [overview, setOverview] = useState(null);
  const [connected, setConnected] = useState(false);
  const pollRef = useRef(null);
  const wsRef = useRef(null);

  useEffect(() => {
    let cancelled = false;

    async function pollOnce() {
      try {
        const res = await fetch(API_URL);
        if (res.ok) {
          const data = await res.json();
          if (!cancelled) setOverview(data);
        }
      } catch (_) {
        
      }
    }

    function connectSocket() {
      const ws = new WebSocket(WS_URL);
      wsRef.current = ws;

      ws.onopen = () => {
        if (cancelled) return;
        setConnected(true);
        if (pollRef.current) {
          clearInterval(pollRef.current);
          pollRef.current = null;
        }
      };

      ws.onmessage = (event) => {
        if (cancelled) return;
        try {
          setOverview(JSON.parse(event.data));
        } catch (_) {
          /* ignore malformed frame */
        }
      };

      ws.onclose = () => {
        if (cancelled) return;
        setConnected(false);
        if (!pollRef.current) {
          pollOnce();
          pollRef.current = setInterval(pollOnce, 3000);
        }
        setTimeout(connectSocket, 3000); // retry
      };

      ws.onerror = () => ws.close();
    }

    pollOnce();
    connectSocket();

    return () => {
      cancelled = true;
      wsRef.current?.close();
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, []);

  return { overview, connected };
}
