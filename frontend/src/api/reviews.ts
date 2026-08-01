import { apiClient } from './client';

export interface ReviewSummary {
  productId: number;
  count: number;
  average: number;
}

export interface Review {
  id: number;
  customerId: number;
  rating: number;
  title: string | null;
  body: string | null;
  createdAt: string;
  mine: boolean;
}

export interface PagedReviews {
  content: Review[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  summary: ReviewSummary;
}

export interface CreateReviewPayload {
  productId: number;
  rating: number;
  title?: string;
  body?: string;
}

export async function fetchReviews(productId: number, page = 0, size = 10): Promise<PagedReviews> {
  const { data } = await apiClient.get<PagedReviews>('/api/reviews', {
    params: { productId, page, size },
  });
  return data;
}

export async function fetchReviewSummary(productId: number): Promise<ReviewSummary> {
  const { data } = await apiClient.get<ReviewSummary>('/api/reviews/summary', {
    params: { productId },
  });
  return data;
}

export async function submitReview(payload: CreateReviewPayload): Promise<Review> {
  const { data } = await apiClient.post<Review>('/api/reviews', payload);
  return data;
}
