import { create } from 'zustand';

interface ChatbotState {
  isOpen: boolean;
  unreadCount: number;
  toggle: () => void;
  open: () => void;
  close: () => void;
  setUnreadCount: (count: number) => void;
}

export const useChatbotStore = create<ChatbotState>((set) => ({
  isOpen: false,
  unreadCount: 0,
  toggle: () => set((s) => ({ isOpen: !s.isOpen })),
  open: () => set({ isOpen: true }),
  close: () => set({ isOpen: false }),
  setUnreadCount: (count) => set({ unreadCount: count }),
}));
