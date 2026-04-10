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

export function useNotifications(): UseNotificationsReturn {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);
  const reconnectTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const connect = useCallback(() => {
    const BASE_URL = import.meta.env.VITE_API_URL ?? '';
    const token = localStorage.getItem('token');

    // Build SSE URL with token as query param (EventSource does not support headers)
    const url = `${BASE_URL}/api/notifications/stream${token ? `?token=${encodeURIComponent(token)}` : ''}`;

    const es = new EventSource(url);
    eventSourceRef.current = es;

    es.onopen = () => {
      setIsConnected(true);
    };

    es.onmessage = (event) => {
      try {
        const notification: Notification = JSON.parse(event.data);
        setNotifications((prev) => [notification, ...prev]);
      } catch {
        // Ignore unparseable messages (e.g. heartbeat pings)
      }
    };

    // Handle named event types
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
      // Server may send initial batch of unread notifications
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

      // Reconnect after 5 seconds
      reconnectTimeoutRef.current = setTimeout(() => {
        connect();
      }, 5000);
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

    // Notify backend
    const BASE_URL = import.meta.env.VITE_API_URL ?? '';
    const token = localStorage.getItem('token');
    fetch(`${BASE_URL}/api/notifications/${id}/read`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    }).catch(() => {
      // Fire-and-forget
    });
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
    }).catch(() => {
      // Fire-and-forget
    });
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
