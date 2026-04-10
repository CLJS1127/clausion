import { api } from './client';

export interface JobStatus {
  id: number;
  jobType: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  inputPayload: unknown;
  resultPayload: unknown;
  errorMessage: string | null;
  createdAt: string;
  completedAt: string | null;
}

export const jobsApi = {
  getJobStatus(jobId: number): Promise<JobStatus> {
    return api.get<JobStatus>(`/api/jobs/${jobId}/status`);
  },
};

/**
 * Poll a job until it completes or fails.
 */
export async function pollJob(
  jobId: number,
  opts?: { intervalMs?: number; timeoutMs?: number },
): Promise<JobStatus> {
  const interval = opts?.intervalMs ?? 1500;
  const timeout = opts?.timeoutMs ?? 120_000;
  const start = Date.now();

  while (Date.now() - start < timeout) {
    const status = await jobsApi.getJobStatus(jobId);
    if (status.status === 'COMPLETED' || status.status === 'FAILED') {
      return status;
    }
    await new Promise((r) => setTimeout(r, interval));
  }
  throw new Error('Job polling timed out');
}
