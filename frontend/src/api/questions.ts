import { api } from './client';
import type { PracticeEvaluation, PracticeQuestion, Question } from '../types';

export const questionsApi = {
  // Backend requires courseId as Long @RequestParam
  getQuestions(courseId: string, params?: {
    skillId?: string;
    approvalStatus?: Question['approvalStatus'];
  }): Promise<Question[]> {
    const query = new URLSearchParams({ courseId });
    if (params?.skillId) query.set('skillId', params.skillId);
    if (params?.approvalStatus) query.set('approvalStatus', params.approvalStatus);
    return api.get<Question[]>(`/api/questions?${query.toString()}`);
  },

  // Backend returns JobIdResponse { jobId: Long }
  generateQuestions(courseId: string, data: {
    skillId?: number;
    difficulty?: string;
    count: number;
  }): Promise<{ jobId: number }> {
    return api.post<{ jobId: number }>(`/api/courses/${courseId}/questions/generate`, data);
  },

  getPracticeQuestion(params: {
    courseId: string;
    skillId?: string | null;
    reviewTaskId?: string | null;
  }): Promise<PracticeQuestion> {
    const query = new URLSearchParams({ courseId: params.courseId });
    if (params.skillId) query.set('skillId', params.skillId);
    if (params.reviewTaskId) query.set('reviewTaskId', params.reviewTaskId);
    return api.get<PracticeQuestion>(`/api/questions/practice?${query.toString()}`);
  },

  evaluatePracticeAnswer(data: {
    reviewTaskId?: string | null;
    courseId: string;
    skillId?: string | null;
    questionType: string;
    questionContent: string;
    referenceAnswer: string;
    explanation: string;
    studentAnswer: string;
  }): Promise<PracticeEvaluation> {
    return api.post<PracticeEvaluation>('/api/questions/practice/evaluate', {
      reviewTaskId: data.reviewTaskId ? Number(data.reviewTaskId) : null,
      courseId: Number(data.courseId),
      skillId: data.skillId ? Number(data.skillId) : null,
      questionType: data.questionType,
      questionContent: data.questionContent,
      referenceAnswer: data.referenceAnswer,
      explanation: data.explanation,
      studentAnswer: data.studentAnswer,
    });
  },

  approveQuestion(questionId: string): Promise<Question> {
    return api.put<Question>(`/api/questions/${questionId}/approve`);
  },

  rejectQuestion(questionId: string): Promise<Question> {
    return api.put<Question>(`/api/questions/${questionId}/reject`);
  },

  createQuestion(data: {
    courseId: string;
    skillId?: string;
    questionType: string;
    difficulty: string;
    content: string;
    answer: string;
    explanation: string;
  }): Promise<Question> {
    return api.post<Question>('/api/questions', {
      ...data,
      courseId: Number(data.courseId),
      skillId: data.skillId ? Number(data.skillId) : null,
    });
  },

  updateQuestion(questionId: string, data: {
    questionType?: string;
    difficulty?: string;
    content?: string;
    answer?: string;
    explanation?: string;
  }): Promise<Question> {
    return api.put<Question>(`/api/questions/${questionId}`, data);
  },

  deleteQuestion(questionId: string): Promise<void> {
    return api.delete<void>(`/api/questions/${questionId}`);
  },
};
