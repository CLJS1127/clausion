import { useState, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { chatbotApi } from '../api/chatbot';
import type { ChatMessage, Conversation } from '../types';

export function useChatbot(courseId?: number) {
  const queryClient = useQueryClient();
  const [currentConversationId, setCurrentConversationId] = useState<string | null>(null);

  // Fetch all conversations (backend filters by authenticated user)
  const { data: conversations = [] } = useQuery<Conversation[]>({
    queryKey: ['conversations'],
    queryFn: () => chatbotApi.getConversations(),
    staleTime: 30_000,
  });

  // Fetch current conversation with messages
  const { data: currentConversation } = useQuery<Conversation>({
    queryKey: ['conversation', currentConversationId],
    queryFn: () => chatbotApi.getConversation(currentConversationId!),
    enabled: !!currentConversationId,
    staleTime: 10_000,
  });

  const messages: ChatMessage[] = currentConversation?.messages ?? [];

  // Send message mutation
  const sendMutation = useMutation({
    mutationFn: async (content: string) => {
      let convId = currentConversationId;

      // Auto-create conversation if none exists
      if (!convId) {
        const newConv = await chatbotApi.createConversation({
          courseId,
          title: content.slice(0, 50),
        });
        convId = newConv.id;
        setCurrentConversationId(convId);
      }

      return chatbotApi.sendMessage(convId, content);
    },
    onSuccess: () => {
      if (currentConversationId) {
        queryClient.invalidateQueries({
          queryKey: ['conversation', currentConversationId],
        });
      }
      queryClient.invalidateQueries({
        queryKey: ['conversations'],
      });
    },
  });

  // Create new conversation
  const createNewConversation = useCallback(async () => {
    try {
      const newConv = await chatbotApi.createConversation({
        courseId,
        title: '새 대화',
      });
      setCurrentConversationId(newConv.id);
      queryClient.invalidateQueries({
        queryKey: ['conversations'],
      });
    } catch (err) {
      console.error('Failed to create conversation:', err);
    }
  }, [courseId, queryClient]);

  // Select existing conversation
  const selectConversation = useCallback((id: string) => {
    setCurrentConversationId(id);
  }, []);

  // Delete conversation
  const deleteConversation = useCallback(
    async (id: string) => {
      try {
        await chatbotApi.deleteConversation(id);
        if (currentConversationId === id) {
          setCurrentConversationId(null);
        }
        queryClient.invalidateQueries({
          queryKey: ['conversations'],
        });
      } catch (err) {
        console.error('Failed to delete conversation:', err);
      }
    },
    [currentConversationId, queryClient],
  );

  return {
    messages,
    conversations,
    currentConversationId,
    currentConversation,
    sendMessage: sendMutation.mutate,
    isSending: sendMutation.isPending,
    sendError: sendMutation.error,
    createNewConversation,
    selectConversation,
    deleteConversation,
  };
}
