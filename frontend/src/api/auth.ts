import { api } from './client';
import type { LoginRequest, LoginResponse, RegisterRequest, User } from '../types';

export const authApi = {
  login(data: LoginRequest): Promise<LoginResponse> {
    return api.post<LoginResponse>('/api/auth/login', data);
  },

  register(data: RegisterRequest): Promise<LoginResponse> {
    return api.post<LoginResponse>('/api/auth/register', data);
  },

  getProfile(): Promise<User> {
    return api.get<User>('/api/auth/profile');
  },
};
