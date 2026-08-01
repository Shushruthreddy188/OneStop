import { apiClient } from './client';

export interface User {
  id: number;
  email: string;
  firstName: string | null;
  lastName: string | null;
  phone: string | null;
  status: string;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: User;
}

export interface Address {
  id: number;
  line1: string;
  line2: string | null;
  city: string;
  state: string | null;
  postalCode: string | null;
  country: string;
  isDefault: boolean;
}

export interface RegisterPayload {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface UpdateProfilePayload {
  firstName?: string;
  lastName?: string;
  phone?: string;
}

export interface AddressPayload {
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  postalCode?: string;
  country: string;
  isDefault: boolean;
}

export async function register(payload: RegisterPayload): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/auth/register', payload);
  return data;
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/auth/login', payload);
  return data;
}

export async function getMe(): Promise<User> {
  const { data } = await apiClient.get<User>('/api/users/me');
  return data;
}

export async function updateProfile(payload: UpdateProfilePayload): Promise<User> {
  const { data } = await apiClient.put<User>('/api/users/me', payload);
  return data;
}

export async function listAddresses(): Promise<Address[]> {
  const { data } = await apiClient.get<Address[]>('/api/users/me/addresses');
  return data;
}

export async function addAddress(payload: AddressPayload): Promise<Address> {
  const { data } = await apiClient.post<Address>('/api/users/me/addresses', payload);
  return data;
}
