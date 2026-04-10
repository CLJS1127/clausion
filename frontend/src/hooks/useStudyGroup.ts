import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { studyGroupApi } from '../api/studyGroup';
import type { StudyGroupMember, StudyGroup } from '../types';

export function useStudyGroup(studentId?: string, courseId?: string) {
  const queryClient = useQueryClient();

  // Queries
  const matchesQuery = useQuery<StudyGroupMember[]>({
    queryKey: ['study-group-matches', studentId, courseId],
    queryFn: () => studyGroupApi.getMatches(studentId!, courseId!),
    enabled: !!studentId && !!courseId,
  });

  const myGroupsQuery = useQuery<StudyGroup[]>({
    queryKey: ['my-study-groups'],
    queryFn: () => studyGroupApi.getMyGroups(),
  });

  // Mutations
  const joinGroupMutation = useMutation({
    mutationFn: (groupId: string) => studyGroupApi.joinGroup(groupId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-study-groups'] });
      queryClient.invalidateQueries({ queryKey: ['study-group-matches'] });
    },
  });

  const leaveGroupMutation = useMutation({
    mutationFn: (groupId: string) => studyGroupApi.leaveGroup(groupId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-study-groups'] });
    },
  });

  const createGroupMutation = useMutation({
    mutationFn: (data: {
      courseId: string;
      name: string;
      description: string;
      maxMembers: number;
    }) => studyGroupApi.createStudyGroup(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-study-groups'] });
    },
  });

  return {
    matches: matchesQuery.data ?? [],
    matchesLoading: matchesQuery.isLoading,
    myGroups: myGroupsQuery.data ?? [],
    myGroupsLoading: myGroupsQuery.isLoading,
    joinGroup: joinGroupMutation.mutate,
    leaveGroup: leaveGroupMutation.mutate,
    createGroup: createGroupMutation.mutate,
    isJoining: joinGroupMutation.isPending,
    isLeaving: leaveGroupMutation.isPending,
  };
}
