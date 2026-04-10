import { api } from './client';
import type { ReviewTask } from '../types';

export interface WeekDaySummary {
  date: string;
  dayLabel: string;
  total: number;
  completed: number;
  status: 'completed' | 'partial' | 'missed' | 'today' | 'future';
}

export const reviewsApi = {
  getTodayReviews(): Promise<ReviewTask[]> {
    return api.get<ReviewTask[]>('/api/reviews/today');
  },

  completeReview(reviewId: string): Promise<ReviewTask> {
    return api.put<ReviewTask>(`/api/reviews/${reviewId}/complete`);
  },

  getByStudent(studentId: string, courseId: string): Promise<ReviewTask[]> {
    return api.get<ReviewTask[]>(`/api/reviews/by-student?studentId=${studentId}&courseId=${courseId}`);
  },

  getWeekSummary(): Promise<WeekDaySummary[]> {
    return api.get<WeekDaySummary[]>('/api/reviews/week-summary');
  },
};
