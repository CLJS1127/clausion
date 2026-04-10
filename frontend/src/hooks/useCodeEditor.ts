import { create } from 'zustand';
import type { CodeFeedback } from '../types';

interface CodeEditorState {
  code: string;
  language: string;
  feedbacks: CodeFeedback[];
  isSubmitting: boolean;
  activeLineHighlight: number | null;

  setCode: (code: string) => void;
  setLanguage: (language: string) => void;
  setFeedbacks: (feedbacks: CodeFeedback[]) => void;
  setIsSubmitting: (isSubmitting: boolean) => void;
  setActiveLineHighlight: (line: number | null) => void;
  reset: () => void;
}

export const useCodeEditor = create<CodeEditorState>((set) => ({
  code: `function solution(arr) {
  // 여기에 코드를 작성하세요
  let result = [];
  for (let i = 0; i < arr.length; i++) {
    if (arr[i] > 0) {
      result.push(arr[i] * 2);
    }
  }
  return result;
}`,
  language: 'javascript',
  feedbacks: [],
  isSubmitting: false,
  activeLineHighlight: null,

  setCode: (code) => set({ code }),
  setLanguage: (language) => set({ language }),
  setFeedbacks: (feedbacks) => set({ feedbacks }),
  setIsSubmitting: (isSubmitting) => set({ isSubmitting }),
  setActiveLineHighlight: (line) => set({ activeLineHighlight: line }),
  reset: () =>
    set({
      code: '',
      feedbacks: [],
      isSubmitting: false,
      activeLineHighlight: null,
    }),
}));
