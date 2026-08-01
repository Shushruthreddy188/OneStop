import { apiClient } from './client';

export interface CartItem {
  itemId: number;
  productId: number;
  productName: string | null;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface Cart {
  cartId: number;
  items: CartItem[];
  subtotal: number;
  totalItems: number;
}

export async function getCart(): Promise<Cart> {
  const { data } = await apiClient.get<Cart>('/api/cart');
  return data;
}

export async function addItem(productId: number, quantity = 1): Promise<Cart> {
  const { data } = await apiClient.post<Cart>('/api/cart/items', { productId, quantity });
  return data;
}

export async function updateItem(itemId: number, quantity: number): Promise<Cart> {
  const { data } = await apiClient.patch<Cart>(`/api/cart/items/${itemId}`, { quantity });
  return data;
}

export async function removeItem(itemId: number): Promise<Cart> {
  const { data } = await apiClient.delete<Cart>(`/api/cart/items/${itemId}`);
  return data;
}

export async function clearCart(): Promise<Cart> {
  const { data } = await apiClient.delete<Cart>('/api/cart');
  return data;
}
