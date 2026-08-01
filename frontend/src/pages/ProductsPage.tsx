import { useState, type FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { fetchCategories, fetchProducts } from '../api/catalog';
import { formatMoney } from '../lib/format';
import AddToCartButton from '../cart/AddToCartButton';
import WishlistButton from '../wishlist/WishlistButton';
import SearchBox from '../components/SearchBox';
import './ProductsPage.css';

const PAGE_SIZE = 20;

export default function ProductsPage() {
  // Filter/pagination state lives in the URL so it survives navigation:
  // going into a product and pressing Back restores the exact page + filters.
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Math.max(0, Number(searchParams.get('page') ?? '0') || 0);
  const category = searchParams.get('category') ? Number(searchParams.get('category')) : null;
  const q = searchParams.get('q') ?? '';
  const [searchInput, setSearchInput] = useState(q);

  function updateParams(next: { page?: number | null; category?: number | null; q?: string | null }) {
    const params = new URLSearchParams(searchParams);
    for (const [key, value] of Object.entries(next)) {
      if (value === null || value === undefined || value === '' || value === 0) {
        params.delete(key);
      } else {
        params.set(key, String(value));
      }
    }
    // replace: filter tweaks shouldn't each add a history entry; the product
    // click (a push) is what Back returns from — to this restored URL.
    setSearchParams(params, { replace: true });
  }

  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
    staleTime: 5 * 60 * 1000,
  });

  const productsQuery = useQuery({
    queryKey: ['products', { page, category, q }],
    queryFn: () => fetchProducts({ page, size: PAGE_SIZE, category, q }),
    placeholderData: keepPreviousData,
  });

  function submitSearch(e: FormEvent) {
    e.preventDefault();
    updateParams({ q: searchInput, page: null });
  }

  const data = productsQuery.data;

  return (
    <section>
      <div className="products-header">
        <h1>Products</h1>
        {data && (
          <span className="muted">
            {data.totalElements.toLocaleString('en-IN')} items
          </span>
        )}
      </div>

      <div className="products-filters">
        <SearchBox value={searchInput} onChange={setSearchInput} onSubmit={submitSearch} />
        <select
          value={category ?? ''}
          onChange={(e) =>
            updateParams({ category: e.target.value ? Number(e.target.value) : null, page: null })
          }
        >
          <option value="">All categories</option>
          {categoriesQuery.data?.map((c) => (
            <option key={c.id} value={c.id}>
              {c.parentName ? `${c.parentName} › ${c.name}` : c.name}
            </option>
          ))}
        </select>
        <button type="button" onClick={() => updateParams({ q: searchInput, page: null })}>Search</button>
      </div>

      {productsQuery.isLoading && <p>Loading…</p>}
      {productsQuery.isError && (
        <div>
          <p className="error">
            Could not load products: {(productsQuery.error as Error).message}. Is the
            backend running?
          </p>
          <button type="button" onClick={() => productsQuery.refetch()}>
            Retry
          </button>
        </div>
      )}

      {data && data.content.length === 0 && <p>No products match your filters.</p>}

      {data && data.content.length > 0 && (
        <>
          <ul className="product-grid">
            {data.content.map((p) => {
              const discounted = p.mrp != null && p.mrp > p.sellingPrice;
              const savePct = discounted && p.mrp
                ? Math.round(((p.mrp - p.sellingPrice) / p.mrp) * 100)
                : 0;
              return (
                <li key={p.id} className="product-card">
                  <Link to={`/products/${p.id}`} className="product-name">
                    {p.name}
                  </Link>
                  <div className="muted small">
                    {p.brandName ?? 'Unbranded'}
                    {p.packageSize ? ` · ${p.packageSize}` : ''}
                  </div>
                  <div className="muted small">{p.categoryName}</div>
                  <div className="price-row">
                    <span className="price">{formatMoney(p.sellingPrice)}</span>
                    {discounted && <span className="mrp">{formatMoney(p.mrp)}</span>}
                    {savePct > 0 && <span className="save-badge">{savePct}% off</span>}
                  </div>
                  <div className="card-actions">
                    <AddToCartButton productId={p.id} />
                    <WishlistButton productId={p.id} />
                  </div>
                </li>
              );
            })}
          </ul>

          <div className="pager">
            <button
              type="button"
              disabled={page === 0 || productsQuery.isFetching}
              onClick={() => updateParams({ page: page - 1 })}
            >
              ‹ Prev
            </button>
            <span className="muted">
              Page {data.page + 1} of {data.totalPages.toLocaleString('en-IN')}
              {productsQuery.isFetching && ' · updating…'}
            </span>
            <button
              type="button"
              disabled={data.last || productsQuery.isFetching}
              onClick={() => updateParams({ page: page + 1 })}
            >
              Next ›
            </button>
          </div>
        </>
      )}
    </section>
  );
}
