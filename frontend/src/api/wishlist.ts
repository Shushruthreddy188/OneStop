import { apiClient } from './client';

export interface WishlistItem {
  id: number;
  productId: number;
  productName: string | null;
  sellingPrice: number | null;
  mrp: number | null;
}

export interface Wishlist {
  items: WishlistItem[];
  count: number;
}

export async function getWishlist(): Promise<Wishlist> {
  const { data } = await apiClient.get<Wishlist>('/api/wishlist');
  return data;
}

export async function addToWishlist(productId: number): Promise<Wishlist> {
  const { data } = await apiClient.post<Wishlist>('/api/wishlist/items', { productId });
  return data;
}

export async function removeFromWishlist(productId: number): Promise<Wishlist> {
  const { data } = await apiClient.delete<Wishlist>(`/api/wishlist/items/${productId}`);
  return data;
}
