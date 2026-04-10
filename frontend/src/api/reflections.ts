import { api } from './client';
import type { Reflection } from '../types';

export const reflectionsApi = {
  createReflection(data: {
    courseId: number;
    content: string;
    stuckPoint?: string;
    selfConfidenceScore: number;
  }): Promise<Reflection> {
    return api.post<Reflection>('/api/reflections', data);
  },

  getReflections(studentId: string): Promise<Reflection[]> {
    return api.get<Reflection[]>(`/api/reflections?studentId=${studentId}`);
  },
};
