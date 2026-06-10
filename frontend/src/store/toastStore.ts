import { create } from 'zustand';

export type ToastVariant = 'info' | 'success' | 'error';

export interface Toast {
  id: string;
  title: string;
  message: string;
  variant: ToastVariant;
}

interface ToastState {
  toasts: Toast[];
  push: (toast: Omit<Toast, 'id'> & { id?: string }) => void;
  dismiss: (id: string) => void;
}

let seq = 0;
const AUTO_DISMISS_MS = 6000;

export const useToastStore = create<ToastState>((set, get) => ({
  toasts: [],

  push: ({ id, title, message, variant }) => {
    const toastId = id ?? `toast-${++seq}`;
    if (get().toasts.some((t) => t.id === toastId)) return;
    set((s) => ({
      toasts: [{ id: toastId, title, message, variant }, ...s.toasts].slice(0, 4),
    }));
    setTimeout(() => get().dismiss(toastId), AUTO_DISMISS_MS);
  },

  dismiss: (id) =>
    set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })),
}));

/** 컴포넌트 밖(queryClient, api 레이어 등)에서도 쓸 수 있는 헬퍼 */
export const toast = {
  success: (title: string, message = '') =>
    useToastStore.getState().push({ title, message, variant: 'success' }),
  error: (title: string, message = '') =>
    useToastStore.getState().push({ title, message, variant: 'error' }),
  info: (title: string, message = '') =>
    useToastStore.getState().push({ title, message, variant: 'info' }),
};
