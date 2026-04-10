import { api } from './client';
import type { GroupChatMessage } from '../types';

export const groupChatApi = {
  getMessages(groupId: string, limit = 50): Promise<GroupChatMessage[]> {
    return api.get<GroupChatMessage[]>(
      `/api/group-chat/${groupId}/messages?limit=${limit}`,
    );
  },
};
