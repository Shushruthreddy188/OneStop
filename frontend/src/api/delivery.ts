import { AxiosError } from 'axios';
import { apiClient } from './client';

export interface ShipmentEvent {
  status: string;
  note: string | null;
  occurredAt: string;
}

export interface Shipment {
  id: number;
  orderId: number;
  status: string;
  courier: string | null;
  trackingNumber: string | null;
  createdAt: string;
  events: ShipmentEvent[];
}

/** Returns null when there's no shipment yet (the event hasn't been consumed). */
export async function fetchDeliveryByOrder(orderId: number): Promise<Shipment | null> {
  try {
    const { data } = await apiClient.get<Shipment>(`/api/deliveries/order/${orderId}`);
    return data;
  } catch (err) {
    if (err instanceof AxiosError && err.response?.status === 404) return null;
    throw err;
  }
}
