import { create } from 'zustand';
import type { CodeFeedback } from '../types';

const CODE_TEMPLATES: Record<string, string> = {
  javascript: `function solution(arr) {
  // 여기에 코드를 작성하세요
  let result = [];
  for (let i = 0; i < arr.length; i++) {
    if (arr[i] > 0) {
      result.push(arr[i] * 2);
    }
  }
  return result;
}`,
  typescript: `function solution(arr: number[]): number[] {
  // 여기에 코드를 작성하세요
  const result: number[] = [];
  for (let i = 0; i < arr.length; i++) {
    if (arr[i] > 0) {
      result.push(arr[i] * 2);
    }
  }
  return result;
}`,
  python: `def solution(arr):
    """여기에 코드를 작성하세요"""
    result = []
    for x in arr:
        if x > 0:
            result.append(x * 2)
    return result`,
  java: `public class Solution {
    // 여기에 코드를 작성하세요
    public static int[] solution(int[] arr) {
        List<Integer> result = new ArrayList<>();
        for (int x : arr) {
            if (x > 0) {
                result.add(x * 2);
            }
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }
}`,
  cpp: `#include <vector>
using namespace std;

// 여기에 코드를 작성하세요
vector<int> solution(vector<int>& arr) {
    vector<int> result;
    for (int x : arr) {
        if (x > 0) {
            result.push_back(x * 2);
        }
    }
    return result;
}`,
};

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
  code: CODE_TEMPLATES.javascript,
  language: 'javascript',
  feedbacks: [],
  isSubmitting: false,
  activeLineHighlight: null,

  setCode: (code) => set({ code }),
  setLanguage: (language) =>
    set({
      language,
      code: CODE_TEMPLATES[language] ?? '',
      feedbacks: [],
      activeLineHighlight: null,
    }),
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
