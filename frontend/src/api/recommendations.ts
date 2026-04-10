import { api } from './client';
import type { Recommendation } from '../types';

export const recommendationsApi = {
  getRecommendations(studentId: string, courseId?: string): Promise<Recommendation[]> {
    const params = courseId ? `?courseId=${courseId}` : '';
    return api.get<Recommendation[]>(`/api/recommendations/${studentId}${params}`);
  },
};
