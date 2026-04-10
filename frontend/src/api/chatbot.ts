import { api } from './client';
import type { Conversation, ChatMessage } from '../types';

// Backend response types
interface BackendMessageResponse {
  id: number;
  role: string;
  content: string;
  inlineCards?: Record<string, unknown>[];
  tokenCount: number;
  createdAt: string;
}

interface BackendConversationResponse {
  id: number;
  studentId: number;
  courseId: number;
  title: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  messageCount: number;
}

interface BackendConversationDetailResponse extends BackendConversationResponse {
  messages: BackendMessageResponse[];
}

function mapMessage(m: BackendMessageResponse): ChatMessage {
  return {
    id: String(m.id),
    role: m.role as ChatMessage['role'],
    content: m.content,
    timestamp: m.createdAt,
    inlineCards: m.inlineCards?.map((c) => ({
      type: (c.type as 'review_steps' | 'resource_link' | 'action_confirm') ?? 'resource_link',
      data: c,
    })),
  };
}

function mapConversation(c: BackendConversationResponse): Conversation {
  return {
    id: String(c.id),
    studentId: String(c.studentId),
    courseId: String(c.courseId),
    title: c.title,
    twinContextJson: '',
    status: c.status,
    createdAt: c.createdAt,
    updatedAt: c.updatedAt,
  };
}

export const chatbotApi = {
  async getConversations(): Promise<Conversation[]> {
    const list = await api.get<BackendConversationResponse[]>('/api/chatbot/conversations');
    return list.map(mapConversation);
  },

  async createConversation(data: { courseId?: number; title?: string }): Promise<Conversation> {
    const res = await api.post<BackendConversationResponse>('/api/chatbot/conversations', data);
    return mapConversation(res);
  },

  async getConversation(conversationId: string): Promise<Conversation> {
    const res = await api.get<BackendConversationDetailResponse>(
      `/api/chatbot/conversations/${conversationId}`,
    );
    const conv = mapConversation(res);
    conv.messages = res.messages?.map(mapMessage) ?? [];
    return conv;
  },

  async sendMessage(conversationId: string, content: string): Promise<ChatMessage> {
    const res = await api.post<BackendMessageResponse>(
      `/api/chatbot/conversations/${conversationId}/messages`,
      { content },
    );
    return mapMessage(res);
  },

  deleteConversation(conversationId: string): Promise<void> {
    return api.delete<void>(`/api/chatbot/conversations/${conversationId}`);
  },
};
