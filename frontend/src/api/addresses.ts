import { apiClient } from './client';

export interface Address {
  id: number;
  label: string | null;
  recipientName: string | null;
  phone: string | null;
  line1: string;
  line2: string | null;
  city: string;
  state: string | null;
  postalCode: string | null;
  country: string;
  isDefault: boolean;
}

export interface AddressPayload {
  label?: string;
  recipientName?: string;
  phone?: string;
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  postalCode?: string;
  country: string;
  isDefault: boolean;
}

export async function listAddresses(): Promise<Address[]> {
  const { data } = await apiClient.get<Address[]>('/api/addresses');
  return data;
}

export async function createAddress(payload: AddressPayload): Promise<Address> {
  const { data } = await apiClient.post<Address>('/api/addresses', payload);
  return data;
}

export async function updateAddress(id: number, payload: AddressPayload): Promise<Address> {
  const { data } = await apiClient.put<Address>(`/api/addresses/${id}`, payload);
  return data;
}

export async function deleteAddress(id: number): Promise<void> {
  await apiClient.delete(`/api/addresses/${id}`);
}
