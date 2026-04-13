import { api } from './client';
import type { Consultation } from '../types';

export const consultationsApi = {
  getConsultations(role: 'student' | 'instructor' = 'student'): Promise<Consultation[]> {
    return api.get<Consultation[]>(`/api/consultations?role=${role}`);
  },

  createConsultation(data: {
    studentId: number;
    instructorId: number;
    courseId: number;
    scheduledAt: string;
  }): Promise<Consultation> {
    return api.post<Consultation>('/api/consultations', data);
  },

  getConsultationBriefing(consultationId: string): Promise<{
    studentSummary: string;
    riskAreas: string[];
    suggestedTopics: string[];
    actionHistory: string[];
  }> {
    return api.get(`/api/consultations/${consultationId}/briefing`);
  },

  requestConsultation(data: { courseId: number; reason?: string }): Promise<{ id: number; status: string }> {
    return api.post('/api/consultations/request', data);
  },

  acceptConsultation(consultationId: string): Promise<Consultation> {
    return api.put<Consultation>(`/api/consultations/${consultationId}/accept`);
  },

  rejectConsultation(consultationId: string): Promise<Consultation> {
    return api.put<Consultation>(`/api/consultations/${consultationId}/reject`);
  },

  saveSummary(
    consultationId: string,
    data: { summaryText: string; causeAnalysis?: string; actionPlanJson: string },
  ): Promise<Consultation> {
    return api.post<Consultation>(
      `/api/consultations/${consultationId}/summary`,
      data,
    );
  },

  endVideo(consultationId: string): Promise<{ consultationId: number; roomName: string; status: string }> {
    return api.post(`/api/consultations/${consultationId}/end-video`);
  },
};
