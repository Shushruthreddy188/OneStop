import { apiClient } from './client';

export interface RecentOrder {
  id: number;
  customerId: number;
  status: string;
  total: number;
  paymentMethod: string;
}

export interface DashboardData {
  productCount: number;
  categoryCount: number;
  couponCount: number;
  orderCount: number;
  confirmedOrderCount: number;
  revenue: number;
  recentOrders: RecentOrder[];
}

export interface CouponSummary {
  code: string;
  description: string | null;
  discountType: string;
  discountValue: number;
  minOrderAmount: number | null;
}

export interface CreateCouponPayload {
  code: string;
  description?: string;
  discountType: string;
  discountValue: number;
  minOrderAmount?: number;
  maxDiscount?: number;
}

export async function fetchDashboard(): Promise<DashboardData> {
  const { data } = await apiClient.get<DashboardData>('/api/admin/dashboard');
  return data;
}

export async function listAdminCoupons(): Promise<CouponSummary[]> {
  const { data } = await apiClient.get<CouponSummary[]>('/api/admin/coupons');
  return data;
}

export async function createCoupon(payload: CreateCouponPayload): Promise<CouponSummary> {
  const { data } = await apiClient.post<CouponSummary>('/api/admin/coupons', payload);
  return data;
}
