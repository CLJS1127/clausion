import { useQuery } from '@tanstack/react-query';
import { coursesApi } from '../api/courses';
import { useCourseStore } from '../store/courseStore';
import type { Course } from '../types';
import { useAuthStore } from '../store/authStore';

export interface MyEnrollment {
  enrollmentId: number;
  courseId: number;
  studentId: number;
  status: string;
}

export function useCourses() {
  return useQuery<Course[]>({
    queryKey: ['courses'],
    queryFn: () => coursesApi.getCourses(),
    staleTime: 5 * 60 * 1000,
  });
}

export function useMyEnrollments() {
  const role = useAuthStore((state) => state.user?.role);
  return useQuery<MyEnrollment[]>({
    queryKey: ['my-enrollments'],
    queryFn: () => coursesApi.getMyEnrollments(),
    enabled: role === 'STUDENT',
    staleTime: 5 * 60 * 1000,
  });
}

export function useCourseId(): string | undefined {
  const role = useAuthStore((state) => state.user?.role);
  const { data: courses } = useCourses();
  const { data: enrollments = [] } = useMyEnrollments();
  const { selectedCourseId, setSelectedCourseId } = useCourseStore();

  if (role === 'STUDENT') {
    // 승인(ACTIVE)된 수강 과정만 대상으로 한다
    const activeCourseIds = enrollments
      .filter((enrollment) => enrollment.status === 'ACTIVE')
      .map((enrollment) => enrollment.courseId.toString());

    // 사이드바에서 선택한 과정이 유효하면 그대로 사용
    if (selectedCourseId && activeCourseIds.includes(selectedCourseId)) {
      return selectedCourseId;
    }
    return activeCourseIds[0];
  }

  // If a course is explicitly selected and it still exists in the list, use it
  if (selectedCourseId && courses?.some((c) => String(c.id) === selectedCourseId)) {
    return selectedCourseId;
  }

  // Auto-select the first course with enrollments, or just the first one
  const fallback = courses?.find((c) => (c.enrollmentCount ?? 0) > 0) ?? courses?.[0];
  const fallbackId = fallback?.id?.toString();

  // Persist the auto-selected course
  if (fallbackId && fallbackId !== selectedCourseId) {
    setSelectedCourseId(fallbackId);
  }

  return fallbackId;
}
