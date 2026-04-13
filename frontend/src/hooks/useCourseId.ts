import { useQuery } from '@tanstack/react-query';
import { coursesApi } from '../api/courses';
import type { Course } from '../types';
import { useAuthStore } from '../store/authStore';

export function useCourses() {
  return useQuery<Course[]>({
    queryKey: ['courses'],
    queryFn: () => coursesApi.getCourses(),
    staleTime: 5 * 60 * 1000,
  });
}

export function useCourseId(): string | undefined {
  const role = useAuthStore((state) => state.user?.role);
  const { data: courses } = useCourses();
  const { data: enrollments = [] } = useQuery<
    { enrollmentId: number; courseId: number; studentId: number; status: string }[]
  >({
    queryKey: ['my-enrollments'],
    queryFn: () => coursesApi.getMyEnrollments(),
    enabled: role === 'STUDENT',
    staleTime: 5 * 60 * 1000,
  });

  if (role === 'STUDENT') {
    const activeEnrollment = enrollments.find((enrollment) => enrollment.status === 'ACTIVE');
    return activeEnrollment?.courseId?.toString();
  }

  return courses?.[0]?.id?.toString();
}
