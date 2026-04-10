import { api } from './client';
import type { StudentTwin, SkillMasterySnapshot } from '../types';

export const twinApi = {
  // Backend returns List<TwinResponse>, we take the first element
  async getStudentTwin(studentId: string): Promise<StudentTwin> {
    const list = await api.get<StudentTwin[]>(`/api/twin/${studentId}`);
    return list[0] ?? null;
  },

  // Backend returns List<SnapshotResponse> (skill mastery snapshots)
  getTwinHistory(studentId: string, courseId?: string): Promise<SkillMasterySnapshot[]> {
    const params = courseId ? `?courseId=${courseId}` : '';
    return api.get<SkillMasterySnapshot[]>(`/api/twin/${studentId}/history${params}`);
  },

  // Trigger manual twin inference refresh (instructor use)
  refreshTwin(studentId: string, courseId: string): Promise<{ status: string; message: string }> {
    return api.post(`/api/twin/${studentId}/courses/${courseId}/refresh`, {});
  },
};
