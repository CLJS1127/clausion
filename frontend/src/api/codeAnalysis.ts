import { api } from './client';
import type { CodeFeedback } from '../types';

// Backend response types
export interface SubmissionResponse {
  submissionId: number;
  jobId: number;
  status: string;
}

export interface SubmissionDetailResponse {
  id: number;
  studentId: number;
  courseId: number;
  skillId: number;
  codeContent: string;
  language: string;
  status: string;
  feedbacks: CodeFeedback[];
  createdAt: string;
}

export const codeAnalysisApi = {
  submitCode(data: {
    courseId: number;
    skillId?: number | null;
    codeContent: string;
    language: string;
  }): Promise<SubmissionResponse> {
    return api.post<SubmissionResponse>('/api/code-analysis/submit', data);
  },

  // Backend returns SubmissionDetailResponse with nested feedbacks
  async getFeedback(submissionId: string): Promise<CodeFeedback[]> {
    const detail = await api.get<SubmissionDetailResponse>(
      `/api/code-analysis/${submissionId}/feedback`,
    );
    return detail.feedbacks ?? [];
  },

  getCodeHistory(studentId: string): Promise<SubmissionDetailResponse[]> {
    return api.get<SubmissionDetailResponse[]>(
      `/api/code-analysis/history?studentId=${studentId}`,
    );
  },
};
