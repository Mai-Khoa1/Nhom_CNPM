import { Role, UserStatus } from './enums';

export interface UserCreateRequest {
  username: string;
  password: string;
  fullName: string;
  email: string;
  phone?: string;
  role: Role;
}

export interface UserUpdateRequest {
  fullName?: string;
  email?: string;
  phone?: string;
}

export interface UserResponse {
  id: number;
  username: string;
  fullName: string;
  email: string;
  phone?: string;
  role: Role;
  status: UserStatus;
  avatarUrl?: string;
  createdAt: string;
}