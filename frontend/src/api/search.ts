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

export interface SearchResultItem {
  productId: number;
  name: string;
  brandName: string | null;
  categoryName: string | null;
  packageSize: string | null;
  sellingPrice: number;
  mrp: number | null;
}

/** One facet value and how many results in the current query carry it. */
export interface SearchFacet {
  value: string;
  count: number;
}

export interface SearchResponse {
  content: SearchResultItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  /** Brand facet counts (empty when served by the Postgres fallback). */
  brands: SearchFacet[];
  categories: SearchFacet[];
}

export interface SearchParams {
  q: string;
  brand?: string | null;
  category?: string | null;
  page?: number;
  size?: number;
}

/** Typo-tolerant, faceted product search via the search-service (Elasticsearch). */
export async function fetchSearch(params: SearchParams): Promise<SearchResponse> {
  const query: Record<string, string | number> = { q: params.q.trim() };
  if (params.brand) query.brand = params.brand;
  if (params.category) query.category = params.category;
  if (params.page != null) query.page = params.page;
  if (params.size != null) query.size = params.size;
  const { data } = await apiClient.get<SearchResponse>('/api/search', { params: query });
  return data;
}
