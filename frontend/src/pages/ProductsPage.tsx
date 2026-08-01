import { useState, type FormEvent } from 'react';
import { Link } from 'react-router';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { fetchCategories, fetchProducts } from '../api/catalog';
import { formatMoney } from '../lib/format';
import AddToCartButton from '../cart/AddToCartButton';
import './ProductsPage.css';

const PAGE_SIZE = 20;

export default function ProductsPage() {
  const [page, setPage] = useState(0);
  const [category, setCategory] = useState<number | null>(null);
  const [searchInput, setSearchInput] = useState('');
  const [q, setQ] = useState('');

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
    setPage(0);
    setQ(searchInput);
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

      <form className="products-filters" onSubmit={submitSearch}>
        <input
          type="search"
          placeholder="Search products…"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
        />
        <select
          value={category ?? ''}
          onChange={(e) => {
            setPage(0);
            setCategory(e.target.value ? Number(e.target.value) : null);
          }}
        >
          <option value="">All categories</option>
          {categoriesQuery.data?.map((c) => (
            <option key={c.id} value={c.id}>
              {c.parentName ? `${c.parentName} › ${c.name}` : c.name}
            </option>
          ))}
        </select>
        <button type="submit">Search</button>
      </form>

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
                  <AddToCartButton productId={p.id} />
                </li>
              );
            })}
          </ul>

          <div className="pager">
            <button
              type="button"
              disabled={page === 0 || productsQuery.isFetching}
              onClick={() => setPage((n) => Math.max(0, n - 1))}
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
              onClick={() => setPage((n) => n + 1)}
            >
              Next ›
            </button>
          </div>
        </>
      )}
    </section>
  );
}
