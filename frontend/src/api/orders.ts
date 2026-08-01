import { apiClient } from './client';

export interface OrderItem {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface OrderAddress {
  recipientName: string;
  phone: string | null;
  line1: string;
  line2: string | null;
  city: string;
  state: string | null;
  postalCode: string | null;
  country: string;
}

export interface Order {
  id: number;
  status: string;
  subtotal: number;
  tax: number;
  deliveryFee: number;
  total: number;
  paymentMethod: string;
  items: OrderItem[];
  address: OrderAddress | null;
}

export interface OrderSummary {
  id: number;
  status: string;
  total: number;
  paymentMethod: string;
  itemCount: number;
}

export interface CheckoutRequest {
  recipientName: string;
  phone?: string;
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  postalCode?: string;
  country: string;
  paymentMethod: string;
  idempotencyKey: string;
}

export async function placeOrder(payload: CheckoutRequest): Promise<Order> {
  const { data } = await apiClient.post<Order>('/api/orders', payload);
  return data;
}

export async function listOrders(): Promise<OrderSummary[]> {
  const { data } = await apiClient.get<OrderSummary[]>('/api/orders');
  return data;
}

export async function getOrder(id: number): Promise<Order> {
  const { data } = await apiClient.get<Order>(`/api/orders/${id}`);
  return data;
}

export async function cancelOrder(id: number): Promise<Order> {
  const { data } = await apiClient.post<Order>(`/api/orders/${id}/cancel`, {});
  return data;
}
