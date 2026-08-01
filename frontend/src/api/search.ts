import { apiClient } from './client';

export interface Suggestion {
  productId: number;
  name: string;
}

export async function fetchSuggestions(q: string, limit = 8): Promise<Suggestion[]> {
  if (!q || q.trim().length < 2) return [];
  const { data } = await apiClient.get<Suggestion[]>('/api/search/suggest', {
    params: { q: q.trim(), limit },
  });
  return data;
}
