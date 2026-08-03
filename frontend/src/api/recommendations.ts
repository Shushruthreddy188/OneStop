import { apiClient } from './client';

export interface RecommendedProduct {
  productId: number;
  name: string;
  brandName: string | null;
  imageUrl: string | null;
  sellingPrice: number | null;
  mrp: number | null;
  reason: string;
}

export interface RecommendationRow {
  surface: string;
  title: string;
  /** true when a cold-start fallback (e.g. trending) was served. */
  coldStart: boolean;
  items: RecommendedProduct[];
}

/** Most-viewed products across all shoppers. Works anonymously. */
export async function fetchTrending(limit = 8): Promise<RecommendationRow> {
  const { data } = await apiClient.get<RecommendationRow>('/api/recommendations/trending', {
    params: { limit },
  });
  return data;
}

/**
 * Personalized row. The axios interceptor attaches the JWT when signed in, so
 * the server returns view-history-based picks; anonymous users get trending.
 */
export async function fetchForYou(limit = 8): Promise<RecommendationRow> {
  const { data } = await apiClient.get<RecommendationRow>('/api/recommendations/for-you', {
    params: { limit },
  });
  return data;
}
