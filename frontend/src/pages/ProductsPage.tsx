import { useState, type FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { fetchCategories, fetchProducts } from '../api/catalog';
import { fetchSearch, type SearchFacet } from '../api/search';
import { formatMoney } from '../lib/format';
import AddToCartButton from '../cart/AddToCartButton';
import WishlistButton from '../wishlist/WishlistButton';
import SearchBox from '../components/SearchBox';
import './ProductsPage.css';

const PAGE_SIZE = 20;

/** Normalized card shape shared by the catalog-browse and ES-search paths. */
interface Item {
  id: number;
  name: string;
  brandName: string | null;
  categoryName: string | null;
  packageSize: string | null;
  sellingPrice: number;
  mrp: number | null;
}

export default function ProductsPage() {
  // Filter/pagination state lives in the URL so it survives navigation:
  // going into a product and pressing Back restores the exact page + filters.
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Math.max(0, Number(searchParams.get('page') ?? '0') || 0);
  const category = searchParams.get('category') ? Number(searchParams.get('category')) : null;
  const q = searchParams.get('q') ?? '';
  // Search-mode facet selections (brand/category names, distinct from the
  // catalog category id used when browsing).
  const brandFacet = searchParams.get('b');
  const catFacet = searchParams.get('c');
  const [searchInput, setSearchInput] = useState(q);

  const inSearchMode = q.trim().length > 0;

  function updateParams(next: Record<string, number | string | null | undefined>) {
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

  // Browse mode: catalog listing. Search mode: Elasticsearch-backed results.
  const productsQuery = useQuery({
    queryKey: ['products', { page, category }],
    queryFn: () => fetchProducts({ page, size: PAGE_SIZE, category }),
    enabled: !inSearchMode,
    placeholderData: keepPreviousData,
  });

  const searchQuery = useQuery({
    queryKey: ['search', { q, brandFacet, catFacet, page }],
    queryFn: () => fetchSearch({ q, brand: brandFacet, category: catFacet, page, size: PAGE_SIZE }),
    enabled: inSearchMode,
    placeholderData: keepPreviousData,
  });

  function submitSearch(e: FormEvent) {
    e.preventDefault();
    // New query: reset page and any previously chosen facets.
    updateParams({ q: searchInput, page: null, b: null, c: null });
  }

  function toggleFacet(key: 'b' | 'c', value: string) {
    const current = searchParams.get(key);
    updateParams({ [key]: current === value ? null : value, page: null });
  }

  const activeQuery = inSearchMode ? searchQuery : productsQuery;

  // Normalize whichever source is active into a single list + paging shape.
  let items: Item[] = [];
  let totalElements = 0;
  let totalPages = 0;
  let last = true;
  let brands: SearchFacet[] = [];
  let categories: SearchFacet[] = [];

  if (inSearchMode && searchQuery.data) {
    const d = searchQuery.data;
    items = d.content.map((s) => ({
      id: s.productId, name: s.name, brandName: s.brandName,
      categoryName: s.categoryName, packageSize: s.packageSize,
      sellingPrice: s.sellingPrice, mrp: s.mrp,
    }));
    totalElements = d.totalElements;
    totalPages = d.totalPages;
    last = d.last;
    brands = d.brands;
    categories = d.categories;
  } else if (!inSearchMode && productsQuery.data) {
    const d = productsQuery.data;
    items = d.content.map((p) => ({
      id: p.id, name: p.name, brandName: p.brandName,
      categoryName: p.categoryName, packageSize: p.packageSize,
      sellingPrice: p.sellingPrice, mrp: p.mrp,
    }));
    totalElements = d.totalElements;
    totalPages = d.totalPages;
    last = d.last;
  }

  const hasFacets = inSearchMode && (brands.length > 0 || categories.length > 0);

  return (
    <section>
      <div className="products-header">
        <h1>{inSearchMode ? `Results for “${q}”` : 'Products'}</h1>
        {activeQuery.data && (
          <span className="muted">{totalElements.toLocaleString('en-IN')} items</span>
        )}
      </div>

      <div className="products-filters">
        <SearchBox value={searchInput} onChange={setSearchInput} onSubmit={submitSearch} />
        {!inSearchMode && (
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
        )}
        <button type="button" onClick={() => updateParams({ q: searchInput, page: null, b: null, c: null })}>
          Search
        </button>
        {inSearchMode && (
          <button type="button" className="link-btn" onClick={() => { setSearchInput(''); updateParams({ q: null, b: null, c: null, page: null }); }}>
            Clear search
          </button>
        )}
      </div>

      {hasFacets && (
        <div className="facets">
          {brands.length > 0 && (
            <div className="facet-group">
              <span className="facet-label">Brand</span>
              {brands.slice(0, 12).map((f) => (
                <button
                  key={f.value}
                  type="button"
                  className={`facet-pill ${brandFacet === f.value ? 'active' : ''}`}
                  onClick={() => toggleFacet('b', f.value)}
                >
                  {f.value} <span className="facet-count">{f.count}</span>
                </button>
              ))}
            </div>
          )}
          {categories.length > 0 && (
            <div className="facet-group">
              <span className="facet-label">Category</span>
              {categories.slice(0, 12).map((f) => (
                <button
                  key={f.value}
                  type="button"
                  className={`facet-pill ${catFacet === f.value ? 'active' : ''}`}
                  onClick={() => toggleFacet('c', f.value)}
                >
                  {f.value} <span className="facet-count">{f.count}</span>
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {activeQuery.isLoading && <p>Loading…</p>}
      {activeQuery.isError && (
        <div>
          <p className="error">
            Could not load products: {(activeQuery.error as Error).message}. Is the backend running?
          </p>
          <button type="button" onClick={() => activeQuery.refetch()}>
            Retry
          </button>
        </div>
      )}

      {activeQuery.data && items.length === 0 && <p>No products match your filters.</p>}

      {items.length > 0 && (
        <>
          <ul className="product-grid">
            {items.map((p) => {
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
              disabled={page === 0 || activeQuery.isFetching}
              onClick={() => updateParams({ page: page - 1 })}
            >
              ‹ Prev
            </button>
            <span className="muted">
              Page {page + 1} of {totalPages.toLocaleString('en-IN')}
              {activeQuery.isFetching && ' · updating…'}
            </span>
            <button
              type="button"
              disabled={last || activeQuery.isFetching}
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
