import { api } from './client';

export interface HeatmapEntry {
  studentId: number;
  studentName: string;
  overallRiskScore: number;
  masteryScore: number;
  motivationScore: number;
  trendDirection: 'IMPROVING' | 'STABLE' | 'DECLINING' | null;
}

export interface StudentTwinEntry {
  studentId: number;
  studentName: string;
  masteryScore: number;
  executionScore: number;
  retentionRiskScore: number;
  motivationScore: number;
  consultationNeedScore: number;
  overallRiskScore: number;
  aiInsight: string | null;
  trendDirection: 'IMPROVING' | 'STABLE' | 'DECLINING' | null;
  updatedAt: string | null;
}

export interface EnrollmentEntry {
  enrollmentId: number;
  studentId: number;
  studentName: string;
  studentEmail: string;
  status: string;
  enrolledAt: string | null;
}

export const instructorApi = {
  getCourseHeatmap(courseId: string): Promise<HeatmapEntry[]> {
    return api.get<HeatmapEntry[]>(`/api/instructor/course/${courseId}/heatmap`);
  },

  getCourseStudents(courseId: string): Promise<StudentTwinEntry[]> {
    return api.get<StudentTwinEntry[]>(`/api/instructor/course/${courseId}/students`);
  },

  getEnrollments(courseId: string, status?: string): Promise<EnrollmentEntry[]> {
    const params = status ? `?status=${status}` : '';
    return api.get<EnrollmentEntry[]>(`/api/instructor/course/${courseId}/enrollments${params}`);
  },

  approveEnrollment(courseId: string, enrollmentId: number): Promise<EnrollmentEntry> {
    return api.put<EnrollmentEntry>(`/api/instructor/course/${courseId}/enrollments/${enrollmentId}/approve`);
  },

  rejectEnrollment(courseId: string, enrollmentId: number): Promise<EnrollmentEntry> {
    return api.put<EnrollmentEntry>(`/api/instructor/course/${courseId}/enrollments/${enrollmentId}/reject`);
  },
};
