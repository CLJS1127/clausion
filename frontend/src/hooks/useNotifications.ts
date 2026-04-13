import { useState, useEffect, useCallback, useRef } from 'react';
import type { Notification } from '../types';

interface UseNotificationsReturn {
  notifications: Notification[];
  unreadCount: number;
  isConnected: boolean;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
  clearAll: () => void;
}

const MAX_RETRIES = 5;
const BASE_DELAY = 5_000; // 5s

export function useNotifications(): UseNotificationsReturn {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);
  const reconnectTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const retriesRef = useRef(0);

  const connect = useCallback(() => {
    const token = localStorage.getItem('token');
    if (!token) return; // 토큰 없으면 연결하지 않음

    const BASE_URL = import.meta.env.VITE_API_URL ?? '';
    const url = `${BASE_URL}/api/notifications/stream?token=${encodeURIComponent(token)}`;

    const es = new EventSource(url);
    eventSourceRef.current = es;

    es.onopen = () => {
      setIsConnected(true);
      retriesRef.current = 0; // 성공 시 재시도 카운터 리셋
    };

    es.onmessage = (event) => {
      try {
        const notification: Notification = JSON.parse(event.data);
        setNotifications((prev) => [notification, ...prev]);
      } catch {
        // heartbeat 등 무시
      }
    };

    es.addEventListener('notification', (event) => {
      try {
        const notification: Notification = JSON.parse(
          (event as MessageEvent).data,
        );
        setNotifications((prev) => [notification, ...prev]);
      } catch {
        // Ignore
      }
    });

    es.addEventListener('init', (event) => {
      try {
        const batch: Notification[] = JSON.parse(
          (event as MessageEvent).data,
        );
        if (Array.isArray(batch)) {
          setNotifications(batch);
        }
      } catch {
        // Ignore
      }
    });

    es.onerror = () => {
      setIsConnected(false);
      es.close();
      eventSourceRef.current = null;

      // 최대 재시도 횟수 초과 시 중단
      if (retriesRef.current >= MAX_RETRIES) {
        console.warn('[SSE] Max retries reached, stopping reconnection');
        return;
      }

      // 지수 백오프: 5s, 10s, 20s, 40s, 80s
      const delay = BASE_DELAY * Math.pow(2, retriesRef.current);
      retriesRef.current += 1;

      reconnectTimeoutRef.current = setTimeout(() => {
        connect();
      }, delay);
    };
  }, []);

  useEffect(() => {
    connect();

    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
        reconnectTimeoutRef.current = null;
      }
    };
  }, [connect]);

  const markAsRead = useCallback((id: string) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)),
    );

    const BASE_URL = import.meta.env.VITE_API_URL ?? '';
    const token = localStorage.getItem('token');
    fetch(`${BASE_URL}/api/notifications/${id}/read`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    }).catch(() => {});
  }, []);

  const markAllAsRead = useCallback(() => {
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));

    const BASE_URL = import.meta.env.VITE_API_URL ?? '';
    const token = localStorage.getItem('token');
    fetch(`${BASE_URL}/api/notifications/read-all`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    }).catch(() => {});
  }, []);

  const clearAll = useCallback(() => {
    setNotifications([]);
  }, []);

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return {
    notifications,
    unreadCount,
    isConnected,
    markAsRead,
    markAllAsRead,
    clearAll,
  };
}
