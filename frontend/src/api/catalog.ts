import { apiClient } from './client';

export interface ProductSummary {
  id: number;
  sku: string;
  name: string;
  brandName: string | null;
  categoryName: string | null;
  packageSize: string | null;
  imageUrl: string | null;
  mrp: number | null;
  sellingPrice: number;
  status: string;
}

export interface ProductDetail extends ProductSummary {
  description: string | null;
  brandId: number | null;
  categoryId: number | null;
}

export interface Paged<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface Category {
  id: number;
  name: string;
  parentId: number | null;
  parentName: string | null;
}

export interface ProductQuery {
  page?: number;
  size?: number;
  category?: number | null;
  brand?: number | null;
  q?: string;
}

function cleanParams(p: ProductQuery): Record<string, string | number> {
  const out: Record<string, string | number> = {};
  if (p.page != null) out.page = p.page;
  if (p.size != null) out.size = p.size;
  if (p.category != null) out.category = p.category;
  if (p.brand != null) out.brand = p.brand;
  if (p.q && p.q.trim()) out.q = p.q.trim();
  return out;
}

export async function fetchProducts(params: ProductQuery): Promise<Paged<ProductSummary>> {
  const { data } = await apiClient.get<Paged<ProductSummary>>('/api/products', {
    params: cleanParams(params),
  });
  return data;
}

export async function fetchProduct(id: number): Promise<ProductDetail> {
  const { data } = await apiClient.get<ProductDetail>(`/api/products/${id}`);
  return data;
}

export async function fetchCategories(): Promise<Category[]> {
  const { data } = await apiClient.get<Category[]>('/api/categories');
  return data;
}
