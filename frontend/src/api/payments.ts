import { apiClient } from './client';

export interface Payment {
  id: number;
  orderId: number;
  amount: number;
  currency: string;
  method: string;
  status: string;
  provider: string;
  providerRef: string | null;
  createdAt: string;
}

export async function fetchOrderPayments(orderId: number): Promise<Payment[]> {
  const { data } = await apiClient.get<Payment[]>(`/api/payments/order/${orderId}`);
  return data;
}
