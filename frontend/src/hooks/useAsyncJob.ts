import { useEffect, useRef, useState, useCallback } from 'react';
import { jobsApi } from '../api/jobs';
import type { JobStatus } from '../api/jobs';

interface UseAsyncJobOptions {
  /** Polling interval in ms (default 2000) */
  interval?: number;
  /** Stop polling after this many ms (default 300000 = 5 min) */
  timeout?: number;
  /** Whether to start polling immediately (default true when jobId is provided) */
  enabled?: boolean;
}

interface UseAsyncJobResult {
  job: JobStatus | null;
  status: JobStatus['status'] | null;
  result: unknown;
  error: Error | null;
  isPolling: boolean;
  /** Manually restart polling */
  refetch: () => void;
}

export function useAsyncJob(
  jobId: string | null | undefined,
  options: UseAsyncJobOptions = {},
): UseAsyncJobResult {
  const {
    interval = 2000,
    timeout = 300_000,
    enabled = true,
  } = options;

  const [job, setJob] = useState<JobStatus | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const [isPolling, setIsPolling] = useState(false);

  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const abortRef = useRef(false);

  const cleanup = useCallback(() => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
    setIsPolling(false);
  }, []);

  const poll = useCallback(async () => {
    if (!jobId || abortRef.current) return;

    try {
      const result = await jobsApi.getJobStatus(Number(jobId));
      if (abortRef.current) return;

      setJob(result);
      setError(null);

      // Stop polling on terminal states
      if (result.status === 'COMPLETED' || result.status === 'FAILED') {
        cleanup();
      }
    } catch (err) {
      if (abortRef.current) return;
      setError(err instanceof Error ? err : new Error(String(err)));
    }
  }, [jobId, cleanup]);

  const startPolling = useCallback(() => {
    if (!jobId || !enabled) return;

    abortRef.current = false;
    setIsPolling(true);
    setError(null);

    // Immediate first fetch
    poll();

    // Set up interval
    timerRef.current = setInterval(poll, interval);

    // Set up timeout
    timeoutRef.current = setTimeout(() => {
      cleanup();
      setError(new Error(`Job polling timed out after ${timeout}ms`));
    }, timeout);
  }, [jobId, enabled, poll, interval, timeout, cleanup]);

  // Start/stop polling when jobId or enabled changes
  useEffect(() => {
    if (jobId && enabled) {
      startPolling();
    } else {
      cleanup();
    }

    return () => {
      abortRef.current = true;
      cleanup();
    };
  }, [jobId, enabled, startPolling, cleanup]);

  return {
    job,
    status: job?.status ?? null,
    result: job?.resultPayload ?? null,
    error,
    isPolling,
    refetch: startPolling,
  };
}
