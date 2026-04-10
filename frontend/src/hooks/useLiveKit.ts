import { useState, useCallback, useRef, useEffect } from 'react';

interface UseLiveKitOptions {
  consultationId: number;
  role: 'student' | 'instructor';
}

interface UseLiveKitReturn {
  isConnected: boolean;
  isConnecting: boolean;
  localVideoRef: React.RefObject<HTMLVideoElement | null>;
  remoteVideoRef: React.RefObject<HTMLVideoElement | null>;
  isMicEnabled: boolean;
  isCameraEnabled: boolean;
  connect: () => Promise<void>;
  disconnect: () => void;
  toggleMic: () => void;
  toggleCamera: () => void;
  error: string | null;
}

export function useLiveKit({
  consultationId,
  role,
}: UseLiveKitOptions): UseLiveKitReturn {
  const [isConnected, setIsConnected] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const [isMicEnabled, setIsMicEnabled] = useState(true);
  const [isCameraEnabled, setIsCameraEnabled] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const localVideoRef = useRef<HTMLVideoElement | null>(null);
  const remoteVideoRef = useRef<HTMLVideoElement | null>(null);
  const localStreamRef = useRef<MediaStream | null>(null);

  const connect = useCallback(async () => {
    if (isConnected || isConnecting) return;

    setIsConnecting(true);
    setError(null);

    try {
      // 1. Request LiveKit token from backend
      const BASE_URL = import.meta.env.VITE_API_URL ?? '';
      const token = localStorage.getItem('token');
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      };
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      let _livekitToken: string | null = null;
      try {
        const res = await fetch(`${BASE_URL}/api/livekit/token`, {
          method: 'POST',
          headers,
          body: JSON.stringify({ consultationId, role }),
        });
        if (res.ok) {
          const data = await res.json();
          _livekitToken = data.token;
        }
      } catch {
        // Backend may not be available; continue with local-only demo mode
      }

      // 2. Get local media stream
      const stream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true,
      });

      localStreamRef.current = stream;

      // 3. Attach local stream to local video element
      if (localVideoRef.current) {
        localVideoRef.current.srcObject = stream;
      }

      // 4. In a full implementation, we would connect to LiveKit server
      //    using _livekitToken. For demo, we mirror local to remote after delay
      //    to simulate a remote participant.
      if (!_livekitToken) {
        setTimeout(() => {
          if (remoteVideoRef.current && localStreamRef.current) {
            // Clone the local stream for demo "remote" view
            remoteVideoRef.current.srcObject = localStreamRef.current;
          }
        }, 1500);
      }

      setIsConnected(true);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : 'Failed to connect';
      if (message.includes('NotAllowedError') || message.includes('Permission')) {
        setError('카메라/마이크 접근 권한이 필요합니다.');
      } else {
        setError(message);
      }
    } finally {
      setIsConnecting(false);
    }
  }, [consultationId, role, isConnected, isConnecting]);

  const disconnect = useCallback(() => {
    // Stop all tracks
    if (localStreamRef.current) {
      localStreamRef.current.getTracks().forEach((track) => track.stop());
      localStreamRef.current = null;
    }

    // Clear video elements
    if (localVideoRef.current) {
      localVideoRef.current.srcObject = null;
    }
    if (remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = null;
    }

    setIsConnected(false);
    setIsMicEnabled(true);
    setIsCameraEnabled(true);
  }, []);

  const toggleMic = useCallback(() => {
    if (!localStreamRef.current) return;
    const audioTracks = localStreamRef.current.getAudioTracks();
    const newState = !isMicEnabled;
    audioTracks.forEach((track) => {
      track.enabled = newState;
    });
    setIsMicEnabled(newState);
  }, [isMicEnabled]);

  const toggleCamera = useCallback(() => {
    if (!localStreamRef.current) return;
    const videoTracks = localStreamRef.current.getVideoTracks();
    const newState = !isCameraEnabled;
    videoTracks.forEach((track) => {
      track.enabled = newState;
    });
    setIsCameraEnabled(newState);
  }, [isCameraEnabled]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (localStreamRef.current) {
        localStreamRef.current.getTracks().forEach((track) => track.stop());
        localStreamRef.current = null;
      }
    };
  }, []);

  return {
    isConnected,
    isConnecting,
    localVideoRef,
    remoteVideoRef,
    isMicEnabled,
    isCameraEnabled,
    connect,
    disconnect,
    toggleMic,
    toggleCamera,
    error,
  };
}
