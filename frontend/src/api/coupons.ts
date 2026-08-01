import { apiClient } from './client';

export interface CouponValidation {
  valid: boolean;
  code: string;
  discountType: string | null;
  discountValue: number | null;
  discountAmount: number;
  message: string;
}

export interface CouponOffer {
  code: string;
  description: string | null;
  discountType: string;
  discountValue: number;
  minOrderAmount: number;
}

export async function validateCoupon(code: string, orderAmount: number): Promise<CouponValidation> {
  const { data } = await apiClient.post<CouponValidation>('/api/coupons/validate', {
    code,
    orderAmount,
  });
  return data;
}

export async function listCoupons(): Promise<CouponOffer[]> {
  const { data } = await apiClient.get<CouponOffer[]>('/api/coupons');
  return data;
}
