import { apiClient } from './client';

export interface Availability {
  productId: number;
  availableQuantity: number;
  reservedQuantity: number;
}

export async function fetchAvailability(productId: number): Promise<Availability> {
  const { data } = await apiClient.get<Availability>(`/api/inventory/products/${productId}`);
  return data;
}
