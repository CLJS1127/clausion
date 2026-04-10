import { useQuery } from '@tanstack/react-query';
import { coursesApi } from '../api/courses';
import type { Course } from '../types';

export function useCourses() {
  return useQuery<Course[]>({
    queryKey: ['courses'],
    queryFn: () => coursesApi.getCourses(),
    staleTime: 5 * 60 * 1000,
  });
}

export function useCourseId(): string | undefined {
  const { data: courses } = useCourses();
  return courses?.[0]?.id?.toString();
}
