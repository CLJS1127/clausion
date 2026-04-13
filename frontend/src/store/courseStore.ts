import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface CourseStore {
  selectedCourseId: string | null;
  setSelectedCourseId: (id: string | null) => void;
}

export const useCourseStore = create<CourseStore>()(
  persist(
    (set) => ({
      selectedCourseId: null,
      setSelectedCourseId: (id) => set({ selectedCourseId: id }),
    }),
    { name: 'clausion-course' },
  ),
);
